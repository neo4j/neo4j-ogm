/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

import java.time.LocalDate;

/**
 * Converter to convert between {@link java.time.LocalDate} and {@link String}.
 * Stores values in database as string in format specified by
 * {@link java.time.format.DateTimeFormatter#ISO_LOCAL_DATE}:
 * {@code yyyy-MM-dd}.
 *
 * @author Nicolas Mervaillie
 */
public class LocalDateStringConverter implements AttributeConverter<LocalDate, String> {

    @Override
    public String toGraphProperty(LocalDate value) {
        if (value == null)
            return null;
        return value.toString();
    }

    @Override
    public LocalDate toEntityAttribute(String value) {
        if (value == null)
            return null;
        return LocalDate.parse(value);
    }
}
