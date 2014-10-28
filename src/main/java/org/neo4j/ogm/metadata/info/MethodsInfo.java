package org.neo4j.ogm.metadata.info;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MethodsInfo {

    private Map<String, MethodInfo> methods = new HashMap<>();

    MethodsInfo() {}

    public MethodsInfo(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        // get the method information for this class
        int methodCount = dataInputStream.readUnsignedShort();
        for (int i = 0; i < methodCount; i++) {
            dataInputStream.skipBytes(2); // access_flags
            String methodName = constantPool.lookup(dataInputStream.readUnsignedShort()); // name_index
            String descriptor = constantPool.lookup(dataInputStream.readUnsignedShort()); // descriptor
            ObjectAnnotations objectAnnotations = new ObjectAnnotations();
            int attributesCount = dataInputStream.readUnsignedShort();
            for (int j = 0; j < attributesCount; j++) {
                String attributeName = constantPool.lookup(dataInputStream.readUnsignedShort());
                int attributeLength = dataInputStream.readInt();
                if ("RuntimeVisibleAnnotations".equals(attributeName)) {
                    int annotationCount = dataInputStream.readUnsignedShort();
                    for (int m = 0; m < annotationCount; m++) {
                        AnnotationInfo info = new AnnotationInfo(dataInputStream, constantPool);
                        // todo: maybe register just the annotations we're interested in.
                        objectAnnotations.put(info.getName(), info);
                    }
                }
                else {
                    dataInputStream.skipBytes(attributeLength);
                }
            }
            methods.put(methodName, new MethodInfo(methodName, descriptor, objectAnnotations));
        }
    }

    public Collection<MethodInfo> methods() {
        return methods.values();
    }

    public String descriptor(String methodName) {
        return methods.get(methodName).getDescriptor();
    }

    public MethodInfo get(String methodName) {
        return methods.get(methodName);
    }

    public void append(MethodsInfo methodsInfo) {
        for (MethodInfo methodInfo : methodsInfo.methods()) {
            if (!methods.containsKey(methodInfo.getName())) {
                methods.put(methodInfo.getName(), methodInfo);
            }
        }
    }

}
