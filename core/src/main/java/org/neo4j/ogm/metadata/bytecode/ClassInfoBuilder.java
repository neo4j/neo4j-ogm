package org.neo4j.ogm.metadata.bytecode;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.neo4j.ogm.metadata.*;

/**
 * Created by markangrish on 06/03/2017.
 */
public class ClassInfoBuilder {

    // todo move this to a factory class
    public static ClassInfo create(InputStream inputStream) throws IOException {

        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(inputStream, 1024));

        // Magic
        if (dataInputStream.readInt() == 0xCAFEBABE) {

        }

        dataInputStream.readUnsignedShort();    //minor version
        dataInputStream.readUnsignedShort();    // major version

        ConstantPool constantPool = new ConstantPool(dataInputStream);

        // Access flags
        int flags = dataInputStream.readUnsignedShort();

        boolean isInterface = (flags & 0x0200) != 0;
        boolean isAbstract = (flags & 0x0400) != 0;
        boolean isEnum = (flags & 0x4000) != 0;

        String className = constantPool.lookup(dataInputStream.readUnsignedShort()).replace('/', '.');
        String directSuperclassName = null;
        String sce = constantPool.lookup(dataInputStream.readUnsignedShort());
        if (sce != null) {
            directSuperclassName = sce.replace('/', '.');
        }
        InterfacesInfo interfacesInfo = InterfacesInfoBuilder.create(dataInputStream, constantPool);
        FieldsInfo fieldsInfo = FieldsInfoBuilder.create(dataInputStream, constantPool);
        MethodsInfo methodsInfo = MethodsInfoBuilder.create(className, dataInputStream, constantPool);
        AnnotationsInfo annotationsInfo = AnnotationsInfoBuilder.create(dataInputStream, constantPool);
        return new ClassInfo(isAbstract, isInterface, isEnum, className, directSuperclassName, interfacesInfo, annotationsInfo, fieldsInfo, methodsInfo);
    }
}
