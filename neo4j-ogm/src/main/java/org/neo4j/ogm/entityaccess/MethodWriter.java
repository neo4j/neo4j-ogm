package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.ClassUtils;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.MethodInfo;

import java.lang.reflect.Method;

public class MethodWriter extends EntityAccess {

    private final MethodInfo setterMethodInfo;
    private final Class<?> parameterType;
    private final Method method;

    MethodWriter(ClassInfo classInfo, MethodInfo methodInfo) {
        this.setterMethodInfo = methodInfo;
        this.parameterType = ClassUtils.getType(setterMethodInfo.getDescriptor());
        this.method = classInfo.getMethod(setterMethodInfo, parameterType);
    }

    private static void write(Method method, Object instance, Object value) {
        try {
            method.invoke(instance, value);
        } catch (IllegalArgumentException iae) {
            throw new EntityAccessException("Failed to invoke method '" + method.getName() + "'. Expected argument type: " + method.getParameterTypes()[0] + " actual argument type: " + value.getClass(), iae);
        } catch (Exception e) {
            throw new EntityAccessException("Failed to invoke method '" + method.getName() + "'", e);
        }

    }

    public static Object read(Method method, Object instance) {
        try {
            return method.invoke(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(Object instance, Object value) {
        if (setterMethodInfo.hasConverter()) {
            value = setterMethodInfo.converter().toEntityAttribute(value);
        }
        MethodWriter.write(method, instance, value);
    }

    @Override
    public Class<?> type() {
        return parameterType;
    }

    @Override
    public String relationshipName() {
        return this.setterMethodInfo.relationship();
    }

    @Override
    public String relationshipDirection() {
        return setterMethodInfo.relationshipDirection();
    }

}
