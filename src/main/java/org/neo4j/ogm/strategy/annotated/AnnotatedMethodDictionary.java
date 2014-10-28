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
    protected Method findSetter(Object instance, Class parameterClass, String methodName) {
//        ClassInfo classInfo = domainInfo.getClass(instance.getClass().getName());
//        //for (String method: classInfo.methodsInfo().)
//        if (methodsInfo.methods().contains(methodName)) {
//            String descriptor = methodsInfo.descriptor(methodName);
//            if (descriptor.endsWith(")V")) {
//                if (!descriptor.startsWith("(L") && !descriptor.startsWith("([L")) {
//                    parameterClass = ClassUtils.unbox(parameterClass);
//                }
//                return getSetter(methodName, parameterClass, instance);
//            }
//        }
        throw new MappingException("Cannot find method " + methodName + "(" + parameterClass.getSimpleName() + ") in class " + instance.getClass().getName());
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
