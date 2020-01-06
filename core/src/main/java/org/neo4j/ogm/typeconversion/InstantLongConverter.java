/*
 * Copyright (c) 2002-2020 "Neo4j,"
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

import java.time.Instant;

/**
 * Converter to convert {@link Instant} to {@link Long}.
 * Since the value as represented in JSON loses type information and is just numeric, the converted type used is {@link Number}
 * Stores values in db as milliseconds from the epoch of 1970-01-01T00:00:00Z, UTC beeing used to preserve from
 * timezones problems.
 *
 * @author Nicolas Mervaillie
 */
public class InstantLongConverter implements AttributeConverter<Instant, Number> {

    @Override
    public Long toGraphProperty(Instant value) {
        if (value == null)
            return null;
        return value.toEpochMilli();
    }

    @Override
    public Instant toEntityAttribute(Number value) {
        if (value == null)
            return null;
        return Instant.ofEpochMilli(value.longValue());
    }
}
