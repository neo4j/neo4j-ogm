/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */
package org.neo4j.ogm.typeconversion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * By default the OGM will map date collections to UTC-based ISO8601 compliant
 * String collections when being stored as a node / relationship property
 *
 * Users can override this behaviour for Date objects using
 * the appropriate annotations:
 *
 * {@link org.neo4j.ogm.annotation.typeconversion.DateString#FORMAT} will convert between dates and strings
 * using a user defined date format, e.g. "yy-MM-dd"
 *
 * {@link org.neo4j.ogm.annotation.typeconversion.DateLong} will read and write dates as Long values in the database.
 *
 * @author Luanne Misquitta
 */
public class DateCollectionStringConverter implements AttributeConverter<Collection<Date>, String[]> {

    private String format;
    private SimpleDateFormat simpleDateFormat;
    private final Class<? extends Collection> collectionClass;


    public DateCollectionStringConverter(String userDefinedFormat,Class<? extends Collection> collectionClass) {
        this.format = userDefinedFormat;
        this.collectionClass = collectionClass;
        simpleDateFormat = new SimpleDateFormat(format);
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
        Date[] dates = new Date[dateValues.length];

        Collection<Date> values = null;
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
