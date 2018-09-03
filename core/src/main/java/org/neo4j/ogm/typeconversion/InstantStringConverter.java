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

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter to convert {@link Instant} to {@link String}.
 * Stores values in db as milliseconds from the epoch of 1970-01-01T00:00:00Z, UTC being used to preserve from
 * timezones problems.
 *
 * @author Nicolas Mervaillie
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
