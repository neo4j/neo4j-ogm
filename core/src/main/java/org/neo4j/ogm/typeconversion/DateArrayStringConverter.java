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

/**
 * By default the OGM will map date arrays to UTC-based ISO8601 compliant
 * String arrays when being stored as a node / relationship property
 * Users can override this behaviour for Date objects using
 * the appropriate annotations:
 * {@link org.neo4j.ogm.annotation.typeconversion.DateString#FORMAT} will convert between dates and strings
 * using a user defined date format, e.g. "yy-MM-dd"
 * {@link org.neo4j.ogm.annotation.typeconversion.DateLong} will read and write dates as Long values in the database.
 *
 * @author Luanne Misquitta
 */
public class DateArrayStringConverter implements AttributeConverter<Date[], String[]> {

    private String format;

    public DateArrayStringConverter(String userDefinedFormat) {
        this.format = userDefinedFormat;
    }

    @Override
    public String[] toGraphProperty(Date[] value) {
        if (value == null) {
            return null;
        }
        String[] values = new String[(value.length)];

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        int i = 0;
        for (Date date : value) {
            values[i++] = simpleDateFormat.format(date);
        }
        return values;
    }

    @Override
    public Date[] toEntityAttribute(String[] dateValues) {
        if (dateValues == null) {
            return null;
        }
        Date[] dates = new Date[dateValues.length];

        int i = 0;
        try {
            for (String date : dateValues) {
                dates[i++] = new SimpleDateFormat(format).parse(date);
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return dates;
    }
}
