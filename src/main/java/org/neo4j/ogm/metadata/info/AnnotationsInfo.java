package org.neo4j.ogm.metadata.info;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AnnotationsInfo {

    private Set<AnnotationInfo> classAnnotations = new HashSet<>();

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
                    classAnnotations.add(info);
                }
            }
            else {
                dataInputStream.skipBytes(attributeLength);
            }
        }
    }

    public Collection<AnnotationInfo> list() {
        return classAnnotations;
    }

}
