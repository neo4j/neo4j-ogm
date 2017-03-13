package org.neo4j.ogm.metadata.builder;

import java.lang.reflect.Modifier;

import org.neo4j.ogm.metadata.*;


/**
 * Created by markangrish on 06/03/2017.
 */
public class ClassInfoBuilder {

    // todo move this to a factory class
    public static ClassInfo create(Class<?> cls) {

        final int modifiers = cls.getModifiers();
        boolean isInterface = Modifier.isInterface(modifiers);
        boolean isAbstract = Modifier.isAbstract(modifiers);
        boolean isEnum = cls.isEnum();

        String directSuperclassName = null;
        if (cls.getSuperclass() != null) {
            directSuperclassName = cls.getSuperclass().getName();
        }
        InterfacesInfo interfacesInfo = InterfacesInfoBuilder.create(cls);
        FieldsInfo fieldsInfo = FieldsInfoBuilder.create(cls);
        MethodsInfo methodsInfo = MethodsInfoBuilder.create(cls);
        AnnotationsInfo annotationsInfo = AnnotationsInfoBuilder.create(cls);
        return new ClassInfo(isAbstract, isInterface, isEnum, cls.getName(), directSuperclassName, interfacesInfo, annotationsInfo, fieldsInfo, methodsInfo);
    }
}
