package org.neo4j.ogm.metadata.info;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

public class FieldsInfo {

    private Set<String> fields = new HashSet<>();
    private Map<String, ObjectAnnotations> annotations = new HashMap<>();
    private Map<String, String> descriptors = new HashMap<>();

    FieldsInfo() {}

    public FieldsInfo(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        // get the field information for this class
        int fieldCount = dataInputStream.readUnsignedShort();
        for (int i = 0; i < fieldCount; i++) {
            dataInputStream.skipBytes(2); // access_flags
            String fieldName = constantPool.lookup(dataInputStream.readUnsignedShort()); // name_index
            String descriptor = constantPool.lookup(dataInputStream.readUnsignedShort()); // descriptor_index
            int attributesCount = dataInputStream.readUnsignedShort();
            ObjectAnnotations objectAnnotations = new ObjectAnnotations();
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
            fields.add(fieldName);
            descriptors.put(fieldName, descriptor);
            annotations.put(fieldName, objectAnnotations);
        }
    }

    public ObjectAnnotations annotations(String fieldName) {
        return annotations.get(fieldName);
    }

    public Set<String> fields() {
        return fields;
    }
}
