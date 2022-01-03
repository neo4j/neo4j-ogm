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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

/**
 * By default the OGM will map date collections to UTC-based ISO8601 compliant
 * String collections when being stored as a node / relationship property
 * Users can override this behaviour for Date objects using
 * the appropriate annotations:
 * {@link org.neo4j.ogm.annotation.typeconversion.DateString#FORMAT} will convert between dates and strings
 * using a user defined date format, e.g. "yy-MM-dd"
 * {@link org.neo4j.ogm.annotation.typeconversion.DateLong} will read and write dates as Long values in the database.
 *
 * @author Luanne Misquitta
 */
public class DateCollectionStringConverter implements AttributeConverter<Collection<Date>, String[]> {

    private SimpleDateFormat simpleDateFormat;
    private final Class<? extends Collection> collectionClass;

    public DateCollectionStringConverter(String userDefinedFormat, Class<? extends Collection> collectionClass) {
        this.collectionClass = collectionClass;
        simpleDateFormat = new SimpleDateFormat(userDefinedFormat);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public String[] toGraphProperty(Collection<Date> value) {
        if (value == null) {
            return null;
        }
        String[] values = new String[(value.size())];
        int i = 0;
        for (Date date : value) {
            values[i++] = simpleDateFormat.format(date);
        }
        return values;
    }

    @Override
    public Collection<Date> toEntityAttribute(String[] dateValues) {
        if (dateValues == null) {
            return null;
        }

        Collection<Date> values;
        if (List.class.isAssignableFrom(collectionClass)) {
            values = new ArrayList<>(dateValues.length);
        } else if (Vector.class.isAssignableFrom(collectionClass)) {
            values = new Vector<>(dateValues.length);
        } else if (Set.class.isAssignableFrom(collectionClass)) {
            values = new HashSet<>(dateValues.length);
        } else {
            return null;
        }
        try {
            for (String value : dateValues) {
                values.add(simpleDateFormat.parse(value));
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return values;
    }
}
