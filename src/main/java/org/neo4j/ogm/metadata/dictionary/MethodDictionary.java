package org.neo4j.ogm.metadata.dictionary;

import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.info.DomainInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

// todo metadata implements mapping resolver, not this class
public abstract class MethodDictionary implements MappingResolver {

    protected final DomainInfo domainInfo;

    /** owning type => setter or getter method name => java.lang.reflect.Method */
    private final Map<Class<?>, Map<String, Method>> methodCache = new HashMap<>();

    public MethodDictionary(DomainInfo domainInfo) {
        this.domainInfo = domainInfo;
    }

    public Method setter(String setterName, Object parameter, Object instance) throws MappingException {

        Class<?> clazz = instance.getClass();
        Method m = lookup(clazz, setterName);
        if (m != null) {
            return m;
        }

        if (parameter instanceof Collection) {
            Class<?> elementType = ((Collection<?>) parameter).iterator().next().getClass();
            m= findCollectionSetter(instance, parameter, elementType, setterName);
        } else {
            m= findSetter(instance, parameter.getClass(), setterName);
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

    protected Method getSetter(String methodName, Class parameterClass, Object instance) {
        try {
            Method method = instance.getClass().getDeclaredMethod(methodName, parameterClass) ;
            if( Modifier.isPublic(method.getModifiers()))  {
                return method;
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    protected abstract Method findGetter(String methodName, Class<?> returnType, Object instance);
    protected abstract Method findCollectionSetter(Object instance, Object parameter, Class<?> elementType, String setterName);
    protected abstract Method findSetter(Object instance, Class<?> parameterClass, String setterName);

}
