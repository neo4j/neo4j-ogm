package org.neo4j.ogm.strategy.simple;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MethodCache {

    private final Map<Class, Map<String, Method>> methodCache = new HashMap<>();

    public Method lookup(Class clazz, String methodName) {
        Map<String, Method> methods = methodCache.get(clazz);
        if (methods != null) {
            return methods.get(methodName);
        }
        return null;
    }

    public Method insert(Class clazz, String methodName, Method method) {
        Map<String, Method> methods = methodCache.get(clazz);
        if (methods == null) {
            methods = new HashMap<>();
            methodCache.put(clazz, methods);
        }
        methods.put(methodName, method);
        return method;
    }
}
