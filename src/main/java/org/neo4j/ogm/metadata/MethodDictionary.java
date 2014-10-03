package org.neo4j.ogm.metadata;

import java.lang.reflect.Method;

public interface MethodDictionary {

    Method findSetter(String setterName, Object parameter, Object instance) throws Exception;
    Method findGetter(String getterName, Class returnType, Object instance) throws Exception;

}
