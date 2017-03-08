package org.neo4j.ogm.metadata.bytecode;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.Transient;
import org.neo4j.ogm.metadata.AnnotationInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.FieldsInfo;
import org.neo4j.ogm.metadata.ObjectAnnotations;

/**
 * Created by markangrish on 06/03/2017.
 */
public class FieldsInfoBuilder {


    private static final int STATIC_FIELD = 0x0008;
    private static final int FINAL_FIELD = 0x0010;
    private static final int TRANSIENT_FIELD = 0x0080;

    public static FieldsInfo create(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        Map<String, FieldInfo> fields = new HashMap<>();

        // get the field information for this class
        int fieldCount = dataInputStream.readUnsignedShort();
        for (int i = 0; i < fieldCount; i++) {
            int accessFlags = dataInputStream.readUnsignedShort();
            String fieldName = constantPool.readString(dataInputStream.readUnsignedShort()); // name_index
            String descriptor = constantPool.readString(dataInputStream.readUnsignedShort()); // descriptor_index
            int attributesCount = dataInputStream.readUnsignedShort();
            ObjectAnnotations objectAnnotations = new ObjectAnnotations();
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
            if ((accessFlags & (STATIC_FIELD | FINAL_FIELD | TRANSIENT_FIELD)) == 0 && objectAnnotations.get(Transient.class) == null) {
                //fields.put(fieldName, new FieldInfo(fieldName, descriptor, typeParameterDescriptor, objectAnnotations));
            }
        }

        return new FieldsInfo(fields);
    }
}
