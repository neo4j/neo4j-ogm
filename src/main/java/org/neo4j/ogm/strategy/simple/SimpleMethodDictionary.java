package org.neo4j.ogm.strategy.simple;

import org.neo4j.ogm.metadata.ClassUtils;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.dictionary.AttributeDictionary;
import org.neo4j.ogm.metadata.dictionary.MethodDictionary;
import org.neo4j.ogm.metadata.info.DomainInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * The {@link SimpleMethodDictionary} maintains mappings between labels in the graphModel, (Status, Item, Invoice, etc)
 * and the actual setter/getter methods on the class for that type.
 *
 * Where the Type relationship is scalar (i.e. where the object has a relationship to one instance
 * of the the Type only), the {@link SimpleMethodDictionary} assumes that a method name will exactly match
 * the Type name. For example:
 *
 * Type: Status must map to setStatus(...) - and no other method.
 *
 * Where the Type is a non-scalar (i.e. it exists as a collection of Type instances on the object),
 * the SimpleMethodDictionary relaxes the exact matching constraint and allows partial method matching, e.g.
 *
 * Type: Item -> to setItems(...) | setItemList(...), etc.
 *
 * In the general case, for Collection-based properties, the permitted mapping for any collection of Type T is
 *
 * Type: T -> setT*(Collection<T>)
 *
 * @author Vince Bickers
 *
 */
public class SimpleMethodDictionary extends MethodDictionary implements AttributeDictionary {

    public SimpleMethodDictionary(DomainInfo domainInfo) {
        super(domainInfo);
    }

    @Override
    protected Method findScalarSetter(Object instance, Class<?> parameterClass, String methodName) throws MappingException {

        Class<?> clazz = instance.getClass();
        Class<?> primitiveClass = ClassUtils.unbox(parameterClass);

        for (Method method : clazz.getMethods()) {
            if( Modifier.isPublic(method.getModifiers()) &&
                    method.getReturnType().equals(void.class) &&
                    method.getName().equals(methodName) &&
                    method.getParameterTypes().length == 1 &&
                    (method.getParameterTypes()[0] == parameterClass || method.getParameterTypes()[0].isAssignableFrom(primitiveClass))) {
                return method;
            }
        }
        throw new MappingException("Cannot find method " + methodName + "(" + parameterClass.getSimpleName() + ") in class " + instance.getClass().getName());
    }

    @Override
    protected Method findGetter(String methodName, Class<?> returnType, Object instance) throws MappingException {
        try {
            Method method = instance.getClass().getMethod(methodName);
            if (method.getReturnType().isAssignableFrom(returnType)) {
                return method;
            }
            if (Iterable.class.isAssignableFrom(returnType)
                    && (method.getReturnType().isArray() || Iterable.class.isAssignableFrom(method.getReturnType()))) {
                return method;
            }
        } catch (NoSuchMethodException | SecurityException e) {
            // fall through to mapping exception
        }
        throw new MappingException("Could not find method " + methodName + " returning type " + returnType.getSimpleName() + " in " + instance.getClass());
    }

    @Override
    protected Method findCollectionSetter(Object instance, Object collection, Class<?> elementType, String methodName) throws MappingException {
        Class<?> clazz = instance.getClass();
        for (Method method : clazz.getMethods()) {

            if (Modifier.isPublic(method.getModifiers()) &&
                    method.getReturnType().equals(void.class) &&
                    method.getName().startsWith(methodName) &&
                    method.getParameterTypes().length == 1 &&
                    method.getGenericParameterTypes().length == 1) {

                if (method.getParameterTypes()[0].isArray()) {
                    String parameterName = method.getParameterTypes()[0].getName();
                    if (("[L" + elementType.getName() + ";").equals(parameterName)) {
                        return method;
                    }
                    if (ClassUtils.primitiveArrayName(elementType).equals(parameterName)) {
                        return method;
                    }
                }

                else if (method.getParameterTypes()[0].isAssignableFrom(collection.getClass())) {
                    Type t = method.getGenericParameterTypes()[0];
                    if (t.toString().contains(elementType.getName())) {
                        return method;
                    }
                    if (t.toString().contains("<?>")) {
                        return method;
                    }
                }
            }
        }

        throw new MappingException("Cannot find method " + methodName + "?(" + collection.getClass().getSimpleName() + "<" + elementType.getSimpleName() + ">) in class " + instance.getClass().getName());
    }

    @Override
    public Set<String> lookUpCompositeEntityAttributesFromType(Class<?> typeToPersist) {
        Set<String> compositeEntityAttributes = new HashSet<>();
        Set<String> valueAttributes = lookUpValueAttributesFromType(typeToPersist);

        // assumes all getters that don't return values mappable to properties are entities
        for (Method method : typeToPersist.getMethods()) {
            if (isGetter(method)) {
                if (method.getName().equals("getClass")) {
                    continue;
                }
                String attributeName = resolveAttributeName(method);
                if (!valueAttributes.contains(attributeName)) {
                    compositeEntityAttributes.add(attributeName);
                }
            }
        }
        return compositeEntityAttributes;
    }

    @Override
    public Set<String> lookUpValueAttributesFromType(Class<?> typeToPersist) {
        Set<String> valueAttributes = new HashSet<>();
        for (Method method : typeToPersist.getMethods()) {
            if (isGetter(method) && ClassUtils.mapsToGraphProperty(method.getReturnType())) {
                valueAttributes.add(resolveAttributeName(method));
            }
        }
        return valueAttributes;
    }

    @Override
    public String lookUpRelationshipTypeForAttribute(String attributeName) {
        if (attributeName == null) {
            return null;
        }
        return "HAS_" + attributeName.toUpperCase();
    }

    @Override
    public String lookUpPropertyNameForAttribute(String attributeName) {
        // for simple implementations, the attribute name is the same as the graph entity property name
        return attributeName;
    }

    private static boolean isGetter(Method method) {
        return method.getName().startsWith("get") && !Void.class.equals(method.getReturnType()) && method.getParameterTypes().length == 0;
    }

    private static String resolveAttributeName(Method method) {
        return method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
    }

    // guesses the name of a type accessor method, based on the supplied graph attribute
    // the graph attribute can be a node property, e.g. "Name", or a relationship type e.g. "LIKES"
    //
    // A simple attribute e.g. "PrimarySchool" will be mapped to a value "[get,set]PrimarySchool"
    //
    // An attribute with elements separated by underscores will have each element processed and then
    // the parts will be elided to a camelCase name. Elements that imply structure, ("HAS", "IS", "A")
    // will be excluded from the mapping, i.e:
    //
    // "HAS_WHEELS"             => "[get,set]Wheels"
    // "IS_A_BRONZE_MEDALLIST"  => "[get,set]BronzeMedallist"
    // "CHANGED_PLACES_WITH"    => "[get,set]ChangedPlacesWith"
    //
    // the MethodInfo class should help here at some point, but its not accessible currently
    @Override
    public String resolveGraphAttribute(String name) {
        StringBuilder sb = new StringBuilder();
        if (name != null && name.length() > 0) {
            sb.append("set");
            if (!name.contains("_")) {
                sb.append(name.substring(0, 1).toUpperCase());
                sb.append(name.substring(1));
            } else {
                String[] parts = name.split("_");
                for (String part : parts) {
                    String test = part.toLowerCase();
                    if ("has|is|a".contains(test)) continue;
                    String resolved = resolveGraphAttribute(test);
                    if (resolved != null) {
                        sb.append(resolved);
                    }
                }
            }
            return sb.toString();
        } else {
            return null;
        }
    }

    @Override
    public String resolveTypeAttribute(String typeAttributeName, Class<?> owningType) {
        if (typeAttributeName == null || owningType == null) {
            return null;
        }

        try {
            Method getterMethod = owningType.getMethod(
                    "get" + typeAttributeName.substring(0, 1).toUpperCase() + typeAttributeName.substring(1));
            if (isGetter(getterMethod)) {
                return ClassUtils.mapsToGraphProperty(getterMethod.getReturnType())
                        ? lookUpPropertyNameForAttribute(typeAttributeName)
                        : lookUpRelationshipTypeForAttribute(typeAttributeName);
            }
        } catch (NoSuchMethodException | SecurityException e) {
            // fall through to mapping exception
        }
        throw new MappingException("Unable to find getter method matching attribute: " + typeAttributeName + " on " + owningType);
    }

}
