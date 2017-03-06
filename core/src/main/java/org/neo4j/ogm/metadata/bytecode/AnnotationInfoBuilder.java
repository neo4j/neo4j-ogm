package org.neo4j.ogm.metadata.bytecode;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.metadata.AnnotationInfo;

/**
 * Created by markangrish on 06/03/2017.
 */
public class AnnotationInfoBuilder {

    public static AnnotationInfo create(final DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {

        String annotationFieldDescriptor = constantPool.readString(dataInputStream.readUnsignedShort());
        String annotationClassName;
        if (annotationFieldDescriptor.charAt(0) == 'L'
                && annotationFieldDescriptor.charAt(annotationFieldDescriptor.length() - 1) == ';') {
            annotationClassName = annotationFieldDescriptor.substring(1,
                    annotationFieldDescriptor.length() - 1).replace('/', '.');
        } else {
            annotationClassName = annotationFieldDescriptor;
        }
        int numElementValuePairs = dataInputStream.readUnsignedShort();

        Map<String, String> elements = new HashMap<>();
        for (int i = 0; i < numElementValuePairs; i++) {
            String elementName = constantPool.readString(dataInputStream.readUnsignedShort());
            Object value = readAnnotationElementValue(dataInputStream, constantPool);
            if (elementName != null && value != null) {
                elements.put(elementName, value.toString());
            }
        }

        return new AnnotationInfo(annotationClassName, elements);
    }


    private static Object readAnnotationElementValue(final DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {

        int tag = dataInputStream.readUnsignedByte();

        switch (tag) {
            case 'B':
                return constantPool.readByte(dataInputStream.readUnsignedShort());
            case 'C':
                return constantPool.readChar(dataInputStream.readUnsignedShort());
            case 'D':
                return constantPool.readDouble(dataInputStream.readUnsignedShort());
            case 'F':
                return constantPool.readFloat(dataInputStream.readUnsignedShort());
            case 'I':
                return constantPool.readInteger(dataInputStream.readUnsignedShort());
            case 'J':
                return constantPool.readLong(dataInputStream.readUnsignedShort());
            case 'S':
                return constantPool.readShort(dataInputStream.readUnsignedShort());
            case 's':
                return constantPool.readString(dataInputStream.readUnsignedShort());
            case 'Z':
                return constantPool.readBoolean(dataInputStream.readUnsignedShort());
            case 'e':
                // enum_const_value (NOT HANDLED)
                dataInputStream.skipBytes(4);
                return null;
            //return constantPool.lookup(dataInputStream.);
            case 'c':
                // class_info_index
                return constantPool.readString(dataInputStream.readUnsignedShort());
            case '@':
                // Nested annotation
                return AnnotationInfoBuilder.create(dataInputStream, constantPool);
            case '[':
                // array_value
                final int count = dataInputStream.readUnsignedShort();
                Object[] values = new Object[count];
                for (int l = 0; l < count; ++l) {
                    values[l] = readAnnotationElementValue(dataInputStream, constantPool);
                }
                return values;
            default:
                throw new ClassFormatError("Invalid annotation element type tag: 0x" + Integer.toHexString(tag));
        }
    }
}
