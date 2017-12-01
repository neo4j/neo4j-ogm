/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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
import java.time.LocalDateTime;

/**
 * Converter to convert {@link LocalDate} to {@link Long}.
 * Stores values in db as string in format YYYY-MM-DDTHH-MM-SS.
 *
 * @author Frantisek Hartman
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
