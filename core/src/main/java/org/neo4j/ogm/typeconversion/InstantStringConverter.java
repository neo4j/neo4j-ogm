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
import java.time.format.DateTimeFormatter;

/**
 * Converter to convert between {@link Instant} and {@link String}.
 * Stores values in database as string in format specified by
 * {@link java.time.format.DateTimeFormatter#ISO_INSTANT}.
 * UTC time zone is being used to prevent timezones problems.
 *
 * @author Nicolas Mervaillie
 * @author Róbert Papp
 */
public class InstantStringConverter implements AttributeConverter<Instant, String> {

    private final DateTimeFormatter formatter;

    public InstantStringConverter() {
        formatter = DateTimeFormatter.ISO_INSTANT;
    }

    @Override
    public String toGraphProperty(Instant value) {
        if (value == null)
            return null;
        return formatter.format(value);
    }

    @Override
    public Instant toEntityAttribute(String value) {
        if (value == null)
            return null;
        return Instant.parse(value);
    }
}
