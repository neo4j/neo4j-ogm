package org.neo4j.ogm.strategy.simple;

import org.neo4j.ogm.metadata.ClassUtils;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.dictionary.MethodDictionary;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The SimpleMethodDictionary maintains mappings between labels in the graphModel, (Status, Item, Invoice, etc)
 * and the actual setter/getter methods on the class for that type.
 *
 * Where the Type relationship is scalar (i.e. where the object has a relationship to one instance
 * of the the Type only), the SimpleMethodDictionary assumes that a method name will exactly match
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
public class SimpleMethodDictionary implements MethodDictionary {

    private final Map<Class, Map<String, Method>> methodCache = new HashMap<>();

    public Method findSetter(String setterName, Object parameter, Object instance) throws MappingException {

        if (parameter instanceof Collection) {
            Class elementType = ((Collection) parameter).iterator().next().getClass();
            return findCollectionSetter(instance, parameter, elementType, setterName);
        } else {
            return findScalarSetter(instance, parameter.getClass(), setterName);
        }
    }

    private Method lookup(Class clazz, String methodName) {
        Map<String, Method> methods = methodCache.get(clazz);
        if (methods != null) {
            return methods.get(methodName);
        }
        return null;
    }

    private Method insert(Class clazz, String methodName, Method method) {
        Map<String, Method> methods = methodCache.get(clazz);
        if (methods == null) {
            methods = new HashMap<>();
            methodCache.put(clazz, methods);
        }
        methods.put(methodName, method);
        return method;
    }

    private Method findScalarSetter(Object instance, Class parameterClass, String methodName) throws MappingException {

        Class<?> clazz = instance.getClass();

        Method m = lookup(clazz, methodName);
        if (m != null) {
            return m;
        }

        Class primitiveClass = ClassUtils.unbox(parameterClass);

        for (Method method : clazz.getMethods()) {
            if( Modifier.isPublic(method.getModifiers()) &&
                    method.getReturnType().equals(void.class) &&
                    method.getName().equals(methodName) &&
                    method.getParameterTypes().length == 1 &&
                    (method.getParameterTypes()[0] == parameterClass || method.getParameterTypes()[0].isAssignableFrom(primitiveClass))) {
                return insert(clazz, method.getName(), method);
            }
        }
        throw new MappingException("Cannot find method " + methodName + "(" + parameterClass.getSimpleName() + ") in class " + instance.getClass().getName());
    }

    public Method findGetter(String methodName, Class returnType, Object instance) throws NoSuchMethodException {
        Class<?> clazz = instance.getClass();
        Method m = lookup(clazz, methodName);
        if (m == null) {
            for (Method method : clazz.getMethods()) {
                if( Modifier.isPublic(method.getModifiers()) &&
                    // method return type = parameter.class
                    method.getName().equals(methodName)) {
                    return insert(clazz, method.getName(), method);
                }
            }
        } else {
            return m;
        }
        throw new NoSuchMethodException("Could not find method " + methodName + " returning type " + returnType.getClass().getSimpleName() + " in class " + instance.getClass().getName());
    }

    private Method findCollectionSetter(Object instance, Object collection, Class elementType, String methodName) throws MappingException {

        Class<?> clazz = instance.getClass();
        Method m = lookup(clazz, methodName + "?"); // ? indicates a collection setter

        if (m != null) {
            return m;
        }

        for (Method method : instance.getClass().getMethods()) {
            if (Modifier.isPublic(method.getModifiers()) &&
                    method.getReturnType().equals(void.class) &&
                    method.getName().startsWith(methodName) &&
                    method.getParameterTypes().length == 1 &&
                    method.getGenericParameterTypes().length == 1) {
                // assign collection to array
                if (method.getParameterTypes()[0].isArray()) {
                    //    method.getParameterTypes()[0].getName().startsWith("[")) {
                    String parameterName = method.getParameterTypes()[0].getName();
                    if (("[L" + elementType.getName() + ";").equals(parameterName)) {
                        return insert(clazz, method.getName() + "?", method);
                    }
                    if (ClassUtils.primitiveArrayName(elementType).equals(parameterName)) {
                        return insert(clazz, method.getName() + "?", method);
                    }
                } else if (method.getParameterTypes()[0].isAssignableFrom(collection.getClass())) {
                    Type t = method.getGenericParameterTypes()[0];
                    if (t.toString().contains(elementType.getName())) {
                        return insert(clazz, method.getName() + "?", method);
                    }
                    if (t.toString().contains("<?>")) {
                        return insert(clazz, method.getName() + "?", method);
                    }
                }
            }
        }

        throw new MappingException("Cannot find method " + methodName + "?(" + collection.getClass().getSimpleName() + "<" + elementType.getSimpleName() + ">) in class " + instance.getClass().getName());
    }

}
