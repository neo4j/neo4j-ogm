package org.neo4j.ogm.metadata.dictionary;

import org.neo4j.ogm.metadata.MappingException;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class MethodDictionary implements MappingResolver {

    /** owning type => setter or getter method name => java.lang.reflect.Method */
    private final Map<Class<?>, Map<String, Method>> methodCache = new HashMap<>();

    public Method setter(String setterName, Object parameter, Object instance) throws MappingException {

        Class<?> clazz = instance.getClass();
        Method m = lookup(clazz, setterName + "?"); // ? indicates a collection setter
        if (m != null) return m;

        if (parameter instanceof Collection) {
            Class<?> elementType = ((Collection<?>) parameter).iterator().next().getClass();
            m= findCollectionSetter(instance, parameter, elementType, setterName);
        } else {
            m= findScalarSetter(instance, parameter.getClass(), setterName);
        }
        return insert(instance.getClass(), m.getName(), m);
    }

    public Method getter(String methodName, Class<?> returnType, Object instance) throws NoSuchMethodException {
        Class<?> clazz = instance.getClass();
        Method m = lookup(clazz, methodName);
        if (m != null) return m;
        return findGetter(methodName, returnType, instance);
    }

    private Method lookup(Class<?> clazz, String methodName) {
        Map<String, Method> methods = methodCache.get(clazz);
        if (methods != null) {
            return methods.get(methodName);
        }
        return null;
    }

    private Method insert(Class<?> clazz, String methodName, Method method) {
        Map<String, Method> methods = methodCache.get(clazz);
        if (methods == null) {
            methods = new HashMap<>();
            methodCache.put(clazz, methods);
        }
        methods.put(methodName, method);
        return method;
    }

    protected abstract Method findGetter(String methodName, Class<?> returnType, Object instance);
    protected abstract Method findCollectionSetter(Object instance, Object parameter, Class<?> elementType, String setterName);
    protected abstract Method findScalarSetter(Object instance, Class<?> parameterClass, String setterName);

}
