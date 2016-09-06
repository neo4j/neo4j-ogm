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

/**
 * @author Vince Bickers
 */
class ConstantPool {

    private final Object[] pool;

    public ConstantPool(DataInputStream stream) throws IOException {

        int size = stream.readUnsignedShort();
        pool = new Object[size];

        for (int i = 1; i < size; i++) {
            final int flag = stream.readUnsignedByte();
            switch (flag) {
                case ConstantPoolTags.UTF_8:
                    pool[i] = stream.readUTF();
                    break;
                case ConstantPoolTags.INTEGER:
                    pool[i] = stream.readInt();
                    break;
                case ConstantPoolTags.FLOAT:
                    pool[i] = stream.readFloat();
                    break;
                case ConstantPoolTags.LONG:
                    pool[i] = stream.readLong();
                    i++; // double 4-byte slot
                    break;
                case ConstantPoolTags.DOUBLE:
                    pool[i] = stream.readDouble();
                    i++; // double 4-byte slot
                    break;
                case ConstantPoolTags.CLASS:
                case ConstantPoolTags.STRING:
                    pool[i] = stream.readUnsignedShort();
                    break;
                case ConstantPoolTags.FIELD_REF:
                case ConstantPoolTags.METHOD_REF:
                case ConstantPoolTags.INTERFACE_REF:
                case ConstantPoolTags.NAME_AND_TYPE:
                    stream.skipBytes(2); // reference to owning class
                    pool[i]=stream.readUnsignedShort();
                    break;
                case ConstantPoolTags.METHOD_HANDLE:
                    stream.skipBytes(3);
                    break;
                case ConstantPoolTags.METHOD_TYPE:
                    stream.skipBytes(2);
                    break;
                case ConstantPoolTags.INVOKE_DYNAMIC:
                    stream.skipBytes(4);
                    break;
                default:
                    throw new ClassFormatError("Unknown tag value for constant pool entry: " + flag);
            }
        }
    }

    // returns an indexed lookup, entry contains the index of the actual value in the pool
    public String lookup(int entry) {

        Object constantPoolObj = pool[entry];

        if (constantPoolObj instanceof Integer) {
            return String.valueOf(pool[(Integer) constantPoolObj]);
        }
        else {
            throw new RuntimeException("Not expected here!");
        }
    }

    public Boolean readBoolean(int entry) {
        return (Integer) pool[entry] == 1 ? true : false;
    }

    public Byte readByte(int entry) {
        return new Byte(String.valueOf(pool[entry]));
    }

    public Character readChar(int entry) {
        return Character.forDigit((Integer) pool[entry], 10);
    }

    public Double readDouble(int entry) {
        return (Double) pool[entry];
    }

    public Float readFloat(int entry) {
        return (Float) pool[entry];
    }

    public Integer readInteger(int entry) {
        return (Integer) pool[entry];
    }

    public Long readLong(int entry) {
        return (Long) pool[entry];
    }

    public Short readShort(int entry) {
        return Short.decode(String.valueOf(pool[entry]));
    }

    public String readString(int entry) {
        return (String) pool[entry];
    }

}
