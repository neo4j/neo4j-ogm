package org.neo4j.ogm.strategy.simple;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.metadata.ClassUtils;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.dictionary.AttributeDictionary;
import org.neo4j.ogm.metadata.dictionary.MethodDictionary;

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
            if (method.getName().startsWith("get") && !Void.class.equals(method.getReturnType()) && method.getParameterTypes().length == 0) {
                if (method.getName().equals("getClass")) {
                    continue;
                }
                String attributeName = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
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
            // first of all, we only want getter methods
            if (method.getName().startsWith("get") && !Void.class.equals(method.getReturnType())
                    && method.getParameterTypes().length == 0) {

                Class<?> returnType = method.getReturnType();
                if (returnType.isArray() || ClassUtils.unbox(returnType).isPrimitive() || String.class.equals(returnType)) {
                    String attributeName = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
                    valueAttributes.add(attributeName);
                }
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

}
