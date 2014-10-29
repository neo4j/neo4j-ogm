package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.info.MethodInfo;

import java.lang.reflect.Method;

public class MethodAccess extends ObjectAccess {

    public static void write(MethodInfo methodInfo, Object instance, Object value) {
        Class clazz = instance.getClass();
        try {
            if (Iterable.class.isAssignableFrom(value.getClass())) {
                value = merge(value.getClass(), (Iterable<?>) read(methodInfo, instance), (Iterable<?>) value);
            }
            Method method = clazz.getDeclaredMethod(methodInfo.getName(), value.getClass());
            method.invoke(instance, value);
        } catch (Exception e) {
            throw new MappingException(e.getLocalizedMessage());
        }
    }

    public static Object read(MethodInfo methodInfo, Object instance) {
        Class clazz = instance.getClass();
        try {
            Method method = clazz.getDeclaredMethod(methodInfo.getName());
            return method.invoke(instance);
        } catch (Exception e) {
            throw new MappingException(e.getLocalizedMessage());
        }
    }
}
