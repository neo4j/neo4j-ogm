package org.neo4j.spring.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeReflector {

    public Type getGenericParameterType(Object object, int index) {

        Class<?> clazz = object.getClass();
        Type superType = clazz.getGenericSuperclass();
        if (!(superType instanceof Class<?>)) {
            ParameterizedType parameterizedType = (ParameterizedType) superType;
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (index < typeArguments.length) {
                return typeArguments[index];
            }
        } else {
            Type[] interfaces = clazz.getGenericInterfaces();
            for (Type type : interfaces) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                if (index < typeArguments.length) {
                    return typeArguments[index];
                }
            }
        }
        return null;
    }

}
