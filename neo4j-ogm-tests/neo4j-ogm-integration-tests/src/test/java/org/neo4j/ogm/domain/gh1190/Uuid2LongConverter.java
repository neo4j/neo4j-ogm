/*
 * Copyright (c) 2002-2025 "Neo4j,"
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
package org.neo4j.ogm.domain.gh1190;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.neo4j.ogm.typeconversion.CompositeAttributeConverter;
import org.neo4j.ogm.typeconversion.ConverterInfo;

public class Uuid2LongConverter implements CompositeAttributeConverter<UUID> {

    private final ConverterInfo converterInfo;
    private final String fieldName;

    public Uuid2LongConverter(ConverterInfo converterInfo) {
        this.converterInfo = converterInfo;
        this.fieldName = converterInfo.field().getName();
    }

    @Override
    public Map<String, ?> toGraphProperties(UUID value) {

        var properties = new HashMap<String, Long>();
        if (value != null) {
            properties.put(fieldName + "_most", value.getMostSignificantBits());
            properties.put(fieldName + "_least", value.getLeastSignificantBits());
        }

        return properties;
    }

    @Override
    public UUID toEntityAttribute(Map<String, ?> value) {
        var most = (Long) value.get(fieldName + "_most");
        var least = (Long) value.get(fieldName + "_least");

        if (most != null && least != null) {
            return new UUID(most, least);
        }

        return null;
    }
}
