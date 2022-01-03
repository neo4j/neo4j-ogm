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

/**
 * This converter is provided for convenience so that the default conversion of byte arrays ({@literal Byte[]}
 * to Base64 encoded Strings can be turned off. <strong>Be aware that this works only with the embedded or bolt transport!
 * Neo4j's HTTP endpoint does not support literal byte arrays. In that case, the values will be stored as Base64 encoded string.
 * Those Strings can be however read into a byte array literal.
 * </strong>
 *
 * @author Michael J. Simons
 */
public final class NoOpWrappedByteArrayConverter implements AttributeConverter<Byte[], byte[]> {

    @Override
    public byte[] toGraphProperty(Byte[] value) {

        if (value == null) {
            return null;
        }
        byte[] unwrapped = new byte[value.length];
        for (int i = 0; i < value.length; i++) {
            unwrapped[i] = value[i];
        }
        return unwrapped;
    }

    @Override
    public Byte[] toEntityAttribute(byte[] value) {

        if (value == null) {
            return null;
        }
        Byte[] wrapped = new Byte[value.length];
        for (int i = 0; i < value.length; i++) {
            wrapped[i] = value[i];
        }
        return wrapped;
    }
}
