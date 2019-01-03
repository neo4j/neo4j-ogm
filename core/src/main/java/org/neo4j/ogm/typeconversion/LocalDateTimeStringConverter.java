/*
 * Copyright (c) 2002-2019 "Neo4j,"
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

import java.time.LocalDateTime;

/**
 * Converter to convert between {@link LocalDateTime} and {@link String}.
 * Stores values in database as string in format specified by
 * {@link java.time.format.DateTimeFormatter#ISO_LOCAL_DATE_TIME}.
 *
 * @author Frantisek Hartman
 * @author Róbert Papp
 */
public class LocalDateTimeStringConverter implements AttributeConverter<LocalDateTime, String> {

    @Override
    public String toGraphProperty(LocalDateTime value) {
        if (value == null)
            return null;
        return value.toString();
    }

    @Override
    public LocalDateTime toEntityAttribute(String value) {
        if (value == null)
            return null;
        return LocalDateTime.parse(value);
    }
}
