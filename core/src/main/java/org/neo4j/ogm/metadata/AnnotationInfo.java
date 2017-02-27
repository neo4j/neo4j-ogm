/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.metadata;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Vince Bickers
 * @author Mark Angrish
 */
public class AnnotationInfo {

    private String annotationName;
    private final Map<String, String> elements = new HashMap<>();

    public AnnotationInfo(final DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {

        String annotationFieldDescriptor = constantPool.readString(dataInputStream.readUnsignedShort());
        String annotationClassName;
        if (annotationFieldDescriptor.charAt(0) == 'L'
                && annotationFieldDescriptor.charAt(annotationFieldDescriptor.length() - 1) == ';') {
            annotationClassName = annotationFieldDescriptor.substring(1,
                    annotationFieldDescriptor.length() - 1).replace('/', '.');
        } else {
            annotationClassName = annotationFieldDescriptor;
        }
        this.annotationName = annotationClassName;
        int numElementValuePairs = dataInputStream.readUnsignedShort();

        for (int i = 0; i < numElementValuePairs; i++) {
            String elementName = constantPool.readString(dataInputStream.readUnsignedShort());
            Object value = readAnnotationElementValue(dataInputStream, constantPool);
            if (elementName != null && value != null) {
                elements.put(elementName, value.toString());
            }
        }
    }

    public String getName() {
        return annotationName;
    }

    public String get(String key, String defaultValue) {
        if (elements.get(key) == null) {
            elements.put(key, defaultValue);
        }
        return get(key);
    }


    public String get(String key) {
        return elements.get(key);
    }

    private Object readAnnotationElementValue(final DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {

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
                return new AnnotationInfo(dataInputStream, constantPool);
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
