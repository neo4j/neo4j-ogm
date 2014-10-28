package org.neo4j.ogm.metadata.info;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AnnotationsInfo {

    private Map<String, AnnotationInfo> classAnnotations = new HashMap<>();

    AnnotationsInfo() {}

    public AnnotationsInfo(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        int attributesCount = dataInputStream.readUnsignedShort();
        for (int i = 0; i < attributesCount; i++) {
            String attributeName = constantPool.lookup(dataInputStream.readUnsignedShort());
            int attributeLength = dataInputStream.readInt();
            if ("RuntimeVisibleAnnotations".equals(attributeName)) {
                int annotationCount = dataInputStream.readUnsignedShort();
                for (int m = 0; m < annotationCount; m++) {
                    AnnotationInfo info = new AnnotationInfo(dataInputStream, constantPool);
                    // todo: maybe register just the annotations we're interested in.
                    classAnnotations.put(info.getName(), info);
                }
            }
            else {
                dataInputStream.skipBytes(attributeLength);
            }
        }
    }

    public Collection<AnnotationInfo> list() {
        return classAnnotations.values();
    }

    public AnnotationInfo get(String annotationName) {
        return classAnnotations.get(annotationName);
    }

    public void add(AnnotationInfo annotationInfo) {
        classAnnotations.put(annotationInfo.getName(), annotationInfo);
    }

    public void append(AnnotationsInfo annotationsInfo) {
        for (AnnotationInfo annotationInfo : annotationsInfo.list()) {
            add(annotationInfo);
        }
    }
}
