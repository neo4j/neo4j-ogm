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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.neo4j.ogm.utils.StringUtils;

/**
 * By default the OGM will map date objects to UTC-based ISO8601 compliant
 * String values when being stored as a node / relationship property
 * Users can override this behaviour for Date objects using
 * the appropriate annotations:
 * {@link org.neo4j.ogm.annotation.typeconversion.DateString#FORMAT} will convert between dates and strings
 * using a user defined date format, e.g. "yy-MM-dd"
 * {@link org.neo4j.ogm.annotation.typeconversion.DateLong} will read and write dates as Long values in the database.
 *
 * @author Vince Bickers
 * @author Gerrit Meier
 */
public class DateStringConverter implements AttributeConverter<Date, String> {

    private final String format;
    private final boolean lenient;

    public DateStringConverter(String userDefinedFormat) {
        this.format = userDefinedFormat;
        this.lenient = false;
    }

    public DateStringConverter(String userDefinedFormat, boolean lenient) {
        this.format = userDefinedFormat;
        this.lenient = lenient;
    }

    @Override
    public String toGraphProperty(Date value) {
        if (value == null) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(value);
    }

    @Override
    public Date toEntityAttribute(String value) {
        if (value == null || (lenient && StringUtils.isBlank(value))) {
            return null;
        }
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return simpleDateFormat.parse(value);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
