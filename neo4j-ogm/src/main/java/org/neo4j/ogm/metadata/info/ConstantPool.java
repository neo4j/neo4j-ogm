/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.metadata.info;

import java.io.DataInputStream;
import java.io.IOException;

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
                case ConstantPoolTags.FLOAT:
                    stream.skipBytes(4);
                    break;
                case ConstantPoolTags.LONG:
                case ConstantPoolTags.DOUBLE:
                    stream.skipBytes(8);
                    i++; // double slot
                    break;
                case ConstantPoolTags.CLASS:
                case ConstantPoolTags.STRING:
                    pool[i] = stream.readUnsignedShort();
                    break;
                case ConstantPoolTags.FIELD_REF:
                case ConstantPoolTags.METHOD_REF:
                case ConstantPoolTags.INTERFACE_REF:
                case ConstantPoolTags.NAME_AND_TYPE:
                    stream.skipBytes(2); // cypherReference to owning class
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

    public String lookup(int entry) {

        Object constantPoolObj = pool[entry];
        return (constantPoolObj instanceof Integer
                ? (String) pool[(Integer) constantPoolObj]
                : (String) constantPoolObj);
    }
}
