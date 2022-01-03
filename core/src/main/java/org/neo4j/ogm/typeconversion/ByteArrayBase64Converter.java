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
 * By default the OGM will map byte[] objects to Base64
 * String values when being stored as a node / relationship property
 *
 * @author Vince Bickers
 */
public class ByteArrayBase64Converter implements AttributeConverter<byte[], String> {

    @Override
    public String toGraphProperty(byte[] value) {
        if (value == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(value);
    }

    @Override
    public byte[] toEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return Base64.getDecoder().decode(value);
    }
}
