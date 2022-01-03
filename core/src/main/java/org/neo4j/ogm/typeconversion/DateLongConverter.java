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

import java.util.Date;

/**
 * Converter to convert between {@link Date} and {@link Long}.
 * Since the value as represented in JSON loses type information and is just numeric,
 * the converted type used is {@link java.lang.Number}.
 * Stores values in database as milliseconds from the epoch of {@code 1970-01-01T00:00:00Z}.
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class DateLongConverter implements AttributeConverter<Date, Number> {

    @Override
    public Long toGraphProperty(Date value) {
        if (value == null) {
            return null;
        }
        return value.getTime();
    }

    @Override
    public Date toEntityAttribute(Number value) {
        if (value == null) {
            return null;
        }
        return new Date(value.longValue());
    }
}
