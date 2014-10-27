package org.neo4j.ogm.strategy.simple;

import org.neo4j.ogm.metadata.ClassUtils;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.dictionary.AttributeDictionary;
import org.neo4j.ogm.metadata.dictionary.MethodDictionary;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.DomainInfo;
import org.neo4j.ogm.metadata.info.MethodsInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
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

        ClassInfo classInfo = domainInfo.getClass(instance.getClass().getName());
        MethodsInfo methodsInfo = classInfo.methodsInfo();
        if (methodsInfo.methods().contains(methodName)) {
            return getScalarSetter(methodName, parameterClass, instance);
        }
        throw new MappingException("Cannot find method " + methodName + "(" + parameterClass.getSimpleName() + ") in class " + instance.getClass().getName());
    }

    private Method getScalarSetter(String methodName, Class parameterClass, Object instance) {

        Method method;
        Class<?> clazz = instance.getClass();
        Class<?> primitiveClass = ClassUtils.unbox(parameterClass);

        // todo: the MethodInfo object should tell us exactly what to look for:
        // - scalar, collection, array, as well as primitive parameter or not.
        try {
            method = clazz.getDeclaredMethod(methodName, parameterClass) ;
        }
        catch (Exception e) {
            try {
                method = clazz.getDeclaredMethod(methodName, primitiveClass);
            } catch (Exception ee) {
                // methodInfo says this method exists, but we can't find it !
                throw new RuntimeException(ee);
            }
        }

        if( Modifier.isPublic(method.getModifiers()) &&
                method.getReturnType().equals(void.class) &&
                method.getParameterTypes().length == 1 &&
                (method.getParameterTypes()[0] == parameterClass || method.getParameterTypes()[0].isAssignableFrom(primitiveClass))) {
            return method;
        }
        return null;
    }

    private Method getCollectionSetter(Object instance, Object collection, Class<?> elementType, String methodName) {
        try {

            Class<?> clazz = instance.getClass();
            Method method = clazz.getDeclaredMethod(methodName, Collection.class);

            if (Modifier.isPublic(method.getModifiers()) &&
                    method.getReturnType().equals(void.class) &&
                    method.getName().equals(methodName) &&
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
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
        return null;
    }

    private Method getArraySetter(Object instance, Class<?> elementType, String methodName) {
        try {

            Class<?> clazz = instance.getClass();
            Method method = clazz.getDeclaredMethod(methodName, Object.class);

            if (Modifier.isPublic(method.getModifiers()) &&
                    method.getReturnType().equals(void.class) &&
                    method.getName().startsWith(methodName) &&
                    method.getParameterTypes().length == 1 &&
                    method.getGenericParameterTypes().length == 1) {

                String parameterName = method.getParameterTypes()[0].getName();
                if (("[L" + elementType.getName() + ";").equals(parameterName)) {
                    return method;
                }
                if (ClassUtils.primitiveArrayName(elementType).equals(parameterName)) {
                    return method;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            return null;
        }
        return null;
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

//        ClassInfo classInfo = domainInfo.getClass(instance.getClass().getName());
//        MethodsInfo methodsInfo = classInfo.methodsInfo();
//        Method method = null;
//
//        // todo: MethodInfo should be able to tell us exactly what we're looking for.
//        for (String m : methodsInfo.methods()) {
//            if (m.startsWith(methodName)) {
//                System.out.println(m);
//                method = getCollectionSetter(instance, collection, elementType, m);
//                if (method != null) {
//                    return method;
//                }
//            }
//        }

        // TODO:
        // we don't have enough information on the methodInfo object right now to be able to get
        // the method directly loading from the class. To do this, we must be able to
        // identify the type of the parameter the setter takes.
        // If we can't do this, we have to scan the class methods (as below) and check
        // to see if any of them take a parameter of the type we're interested in.
        for (Method method : instance.getClass().getMethods()) {

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

        throw new MappingException("Cannot find method " + methodName + "(" + collection.getClass().getSimpleName() + "<" + elementType.getSimpleName() + ">) in class " + instance.getClass().getName());
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
