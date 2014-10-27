package org.neo4j.ogm.strategy.annotated;

import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.info.DomainInfo;
import org.neo4j.ogm.strategy.simple.SimpleMethodDictionary;

import java.lang.reflect.Method;

public class AnnotatedMethodDictionary extends SimpleMethodDictionary {

    public AnnotatedMethodDictionary(DomainInfo domainInfo) {
        super(domainInfo);
    }

    @Override
    protected Method findGetter(String methodName, Class returnType, Object instance) {
        return null;
    }

    @Override
    protected Method findCollectionSetter(Object instance, Object parameter, Class elementType, String setterName) {
        return null;
    }

    @Override
    protected Method findScalarSetter(Object instance, Class parameterClass, String setterName) {
//        Class<?> clazz = instance.getClass();
//        Class primitiveClass = ClassUtils.unbox(parameterClass);
//
//        for (Method method : clazz.getMethods()) {
//            if( Modifier.isPublic(method.getModifiers()) &&
//                    method.getReturnType().equals(void.class) &&
//                    method.isAnnotationPresent(null)) {
//
//                    method.getName().equals(setterName) &&
//                    method.getParameterTypes().length == 1 &&
//                    (method.getParameterTypes()[0] == parameterClass || method.getParameterTypes()[0].isAssignableFrom(primitiveClass))) {
//                return insert(clazz, method.getName(), method);
//            }
//        }
        throw new MappingException("Cannot find method " + setterName + "(" + parameterClass.getSimpleName() + ") in class " + instance.getClass().getName());
    }

    @Override
    public String resolveGraphAttribute(String attributeName) {
        return null;
    }

    @Override
    public String resolveTypeAttribute(String typeAttributeName, Class<?> owningType) {
        throw new UnsupportedOperationException("atg hasn't written this method yet");
    }

}
