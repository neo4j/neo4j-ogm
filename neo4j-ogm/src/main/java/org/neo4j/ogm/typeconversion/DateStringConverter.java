/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.typeconversion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * By default the OGM will map date objects to UTC-based ISO8601 compliant
 * String values when being stored as a node / relationship property
 *
 * Users can override this behaviour for Date objects using
 * the appropriate annotations:
 *
 * @DateString("format") will convert between dates and strings
 * using a user defined date format, e.g. "yy-MM-dd"
 *
 * @DateLong will read and write dates as Long values in the database.
 */
public class DateStringConverter implements AttributeConverter<Date, String> {

    private String format;

    public DateStringConverter(String userDefinedFormat) {
        this.format = userDefinedFormat;
    }

    @Override
    public String toGraphProperty(Date value) {
        if (value == null) return null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format((Date) value);
    }

    @Override
    public Date toEntityAttribute(String value) {
        if (value == null) return null;
        try {
            return new SimpleDateFormat(format).parse((String) value);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
