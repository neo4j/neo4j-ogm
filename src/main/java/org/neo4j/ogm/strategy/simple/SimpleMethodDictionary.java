package org.neo4j.ogm.strategy.simple;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SimpleMethodDictionary {

    private final Map<Class, Map<String, Method>> methodCache = new HashMap<>();

    public Method findSetter(String setterName, Object parameter, Object instance) throws Exception {

        Method m = null;
        if (parameter instanceof Collection) {
            Class elementType = ((Collection) parameter).iterator().next().getClass();
            m = findCollectionSetter(instance, parameter, elementType, setterName);
        } else {
            m = findScalarSetter(instance, parameter.getClass(), setterName);
        }
        // update partial setter names
        if (!m.getName().equals(setterName)) {
            insert(instance.getClass(), m.getName() + (parameter instanceof Collection ? "?" : ""), m);
        }
        return m;
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

    private Method findScalarSetter(Object instance, Class parameterClass, String methodName) throws NoSuchMethodException {

        Class<?> clazz = instance.getClass();

        Method m = lookup(clazz, methodName);
        if (m != null) {
            return m;
        }

        Class primitiveClass = unbox(parameterClass);

        for (Method method : clazz.getMethods()) {
            if( Modifier.isPublic(method.getModifiers()) &&
                    method.getReturnType().equals(void.class) &&
                    method.getName().startsWith(methodName) &&
                    method.getParameterTypes().length == 1 &&
                    (method.getParameterTypes()[0] == parameterClass || method.getParameterTypes()[0].isAssignableFrom(primitiveClass))) {
                return insert(clazz, methodName, method);
            }
        }
        throw new NoSuchMethodException("Cannot find method " + methodName + "(" + parameterClass.getSimpleName() + ") in class " + instance.getClass().getName());
    }

    public Method findGetter(Object instance, Object parameter, String methodName) throws NoSuchMethodException {
        Class<?> clazz = instance.getClass();
        Method m = lookup(clazz, methodName);
        if (m == null) {
            for (Method method : clazz.getMethods()) {
                if( Modifier.isPublic(method.getModifiers()) &&
                    // method return type = parameter.class
                    method.getName().equals(methodName)) {
                    return insert(clazz, methodName, method);
                }
            }
        } else {
            return m;
        }
        throw new NoSuchMethodException("Could not find method " + methodName + " returning type " + parameter.getClass().getSimpleName() + " in class " + instance.getClass().getName());
    }

    private Method findCollectionSetter(Object instance, Object collection, Class elementType, String methodName) throws NoSuchMethodException {

        Class<?> clazz = instance.getClass();
        Method method = lookup(clazz, methodName + "?"); // ? indicates a collection setter

        if (method != null) {
            return method;
        }

        for (Method m : instance.getClass().getMethods()) {
            if (Modifier.isPublic(m.getModifiers()) &&
                    m.getReturnType().equals(void.class) &&
                    m.getName().startsWith(methodName) &&
                    m.getParameterTypes().length == 1 &&
                    m.getGenericParameterTypes().length == 1) {
                // assign collection to array
                if (m.getParameterTypes()[0].getName().startsWith("[")) {
                    String parameterName = m.getParameterTypes()[0].getName();
                    if (("[L" + elementType.getName() + ";").equals(parameterName)) {
                        return insert(clazz, methodName + "?", m);
                    }
                    if (primitiveArrayName(elementType).equals(parameterName)) {
                        return insert(clazz, methodName + "?", m);
                    }
                } else if (m.getParameterTypes()[0].isAssignableFrom(collection.getClass())) {
                    Type t = m.getGenericParameterTypes()[0];
                    if (t.toString().contains(elementType.getName())) {//|| t.toString().contains(unbox(elementType).getName())) {
                        return insert(clazz, methodName + "?", m);
                    }
                    if (t.toString().contains("<?>")) {
                        return insert(clazz, methodName + "?", m);
                    }
                }
            }
        }

        throw new NoSuchMethodException("Cannot find method " + methodName + "?(" + collection.getClass().getSimpleName() + "<" + elementType.getSimpleName() + ">) in class " + instance.getClass().getName());
    }

    private String primitiveArrayName(Class clazz) {

        if (clazz == Integer.class) return "[I";
        if (clazz == Long.class) return "[J";
        if (clazz == Short.class) return "[S";
        if (clazz == Byte.class) return "[B";
        if (clazz == Character.class) return "[C";
        if (clazz == Float.class) return "[F";
        if (clazz == Double.class) return "[D";
        if (clazz == Boolean.class) return "[Z";

        return "";
    }

    private Class unbox(Class clazz) {
        if (clazz == Void.class) {
            return void.class;
        }
        if (clazz == Integer.class) {
            return int.class;
        }
        if (clazz == Long.class) {
            return long.class;
        }
        if (clazz == Short.class) {
            return short.class;
        }
        if (clazz == Byte.class) {
            return byte.class;
        }
        if (clazz == Float.class) {
            return float.class;
        }
        if (clazz == Double.class) {
            return double.class;
        }
        if (clazz == Character.class) {
            return char.class;
        }
        if (clazz == Boolean.class) {
            return boolean.class;
        }
        // arrays
        if (clazz == Void[].class) {
            return void.class; // an array of Voids is a void.
        }
        if (clazz == Integer[].class) {
            return int[].class;
        }
        if (clazz == Long.class) {
            return long[].class;
        }
        if (clazz == Short.class) {
            return short[].class;
        }
        if (clazz == Byte.class) {
            return byte[].class;
        }
        if (clazz == Float.class) {
            return float[].class;
        }
        if (clazz == Double.class) {
            return double[].class;
        }
        if (clazz == Character.class) {
            return char[].class;
        }
        if (clazz == Boolean.class) {
            return boolean[].class;
        }
        return clazz; // not a primitive, can't be unboxed.
    }

}
