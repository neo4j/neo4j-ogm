package org.neo4j.ogm.metadata.bytecode;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.Transient;
import org.neo4j.ogm.metadata.AnnotationInfo;
import org.neo4j.ogm.metadata.MethodInfo;
import org.neo4j.ogm.metadata.MethodsInfo;
import org.neo4j.ogm.metadata.ObjectAnnotations;

/**
 * Created by markangrish on 06/03/2017.
 */
public class MethodsInfoBuilder {

    public static MethodsInfo create(String className, DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {

        Map<String, MethodInfo> methods = new HashMap<>();

        // get the method information for this class
        int methodCount = dataInputStream.readUnsignedShort();
        for (int i = 0; i < methodCount; i++) {
            dataInputStream.skipBytes(2); // access_flags
            String methodName = constantPool.readString(dataInputStream.readUnsignedShort()); // name_index
            String descriptor = constantPool.readString(dataInputStream.readUnsignedShort()); // descriptor
            ObjectAnnotations objectAnnotations = new ObjectAnnotations();
            int attributesCount = dataInputStream.readUnsignedShort();
            String typeParameterDescriptor = null; // available as an attribute for parameterised collections
            for (int j = 0; j < attributesCount; j++) {
                String attributeName = constantPool.readString(dataInputStream.readUnsignedShort());
                int attributeLength = dataInputStream.readInt();
                if ("RuntimeVisibleAnnotations".equals(attributeName)) {
                    int annotationCount = dataInputStream.readUnsignedShort();
                    for (int m = 0; m < annotationCount; m++) {
                        AnnotationInfo info = AnnotationInfoBuilder.create(dataInputStream, constantPool);
                        // todo: maybe register just the annotations we're interested in.
                        objectAnnotations.put(info.getName(), info);
                    }
                } else if ("Signature".equals(attributeName)) {
                    String signature = constantPool.readString(dataInputStream.readUnsignedShort());
                    if (signature.contains("<")) {
                        typeParameterDescriptor = signature.substring(signature.indexOf('<') + 1, signature.indexOf('>'));
                    }
                } else {
                    dataInputStream.skipBytes(attributeLength);
                }
            }
            if (!methodName.equals("<init>") && !methodName.equals("<clinit>") && objectAnnotations.get(Transient.class) == null) {
                methods.put(methodName, new MethodInfo(className, methodName, objectAnnotations));
            }
        }

        return new MethodsInfo(methods);
    }
}
