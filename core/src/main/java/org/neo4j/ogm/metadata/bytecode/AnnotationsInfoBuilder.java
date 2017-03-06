package org.neo4j.ogm.metadata.bytecode;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.metadata.AnnotationInfo;
import org.neo4j.ogm.metadata.AnnotationsInfo;

/**
 * Created by markangrish on 06/03/2017.
 */
public class AnnotationsInfoBuilder {

    public static AnnotationsInfo create(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        Map<String, AnnotationInfo> classAnnotations = new HashMap<>();
        int attributesCount = dataInputStream.readUnsignedShort();
        for (int i = 0; i < attributesCount; i++) {
            String attributeName = constantPool.readString(dataInputStream.readUnsignedShort());
            int attributeLength = dataInputStream.readInt();
            if ("RuntimeVisibleAnnotations".equals(attributeName)) {
                int annotationCount = dataInputStream.readUnsignedShort();
                for (int m = 0; m < annotationCount; m++) {
                    AnnotationInfo info = AnnotationInfoBuilder.create(dataInputStream, constantPool);
                    // todo: maybe register just the annotations we're interested in.
                    classAnnotations.put(info.getName(), info);
                }
            }
            else {
                dataInputStream.skipBytes(attributeLength);
            }
        }

        return new AnnotationsInfo(classAnnotations);
    }
}
