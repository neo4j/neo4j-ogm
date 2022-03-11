/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.typeconversion;

import java.util.Base64;

/**
 * By default the OGM will map Byte[] wrapped byte[] objects to Base64
 * String values when being stored as a node / relationship property
 * The conversion between the primitive byte[] class and its wrapper
 * Byte[] means that this converter is slightly slower than
 * using the ByteArray64Converter, which works with primitive
 * byte arrays directly.
 *
 * @author Vince Bickers
 */
public class ByteArrayWrapperBase64Converter implements AttributeConverter<Byte[], String> {

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    @Override
    public String toGraphProperty(Byte[] value) {
        if (value == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(toPrimitive(value));
    }

    private static byte[] toPrimitive(final Byte[] array) {
        if (array.length == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        final byte[] result = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    @Override
    public Byte[] toEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        byte[] bytes = Base64.getDecoder().decode(value);
        Byte[] wrapper = new Byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            wrapper[i] = bytes[i];  // preferable to new Byte(..) hence not using Apache toObject()
        }
        return wrapper;
    }
}
