package org.neo4j.ogm.metadata.info;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FieldsInfo {

    private Map<String, ObjectAnnotations> fieldInfoMap = new HashMap<>();

    FieldsInfo() {}

    public FieldsInfo(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        // get the field information for this class
        int fieldCount = dataInputStream.readUnsignedShort();
        for (int i = 0; i < fieldCount; i++) {
            dataInputStream.skipBytes(2); // access_flags
            String fieldName = constantPool.lookup(dataInputStream.readUnsignedShort()); // name_index
            dataInputStream.skipBytes(2); // descriptor_index
            int attributesCount = dataInputStream.readUnsignedShort();
            for (int j = 0; j < attributesCount; j++) {
                ObjectAnnotations objectAnnotations = new ObjectAnnotations();
                String attributeName = constantPool.lookup(dataInputStream.readUnsignedShort());
                int attributeLength = dataInputStream.readInt();
                if ("RuntimeVisibleAnnotations".equals(attributeName)) {
                    int annotationCount = dataInputStream.readUnsignedShort();
                    for (int m = 0; m < annotationCount; m++) {
                        AnnotationInfo info = AnnotationInfo.readAnnotation(dataInputStream, constantPool);
                        // todo: maybe register just the annotations we're interested in.
                        objectAnnotations.put(info.getName(), info);
                    }
                }
                else {
                    dataInputStream.skipBytes(attributeLength);
                }
                fieldInfoMap.put(fieldName, objectAnnotations);
            }
        }
    }

    public ObjectAnnotations getAnnotations(String fieldName) {
        return fieldInfoMap.get(fieldName);
    }
}
