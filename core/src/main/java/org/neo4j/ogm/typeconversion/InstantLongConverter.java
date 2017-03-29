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

import java.time.Instant;

/**
 * Converter to convert {@link Instant} to {@link Long}.
 * Since the value as represented in JSON loses type information and is just numeric, the converted type used is {@link Number}
 * Stores values in db as milliseconds from the epoch of 1970-01-01T00:00:00Z, UTC beeing used to preserve from
 * timezones problems.
 *
 * @author Nicolas Mervaillie
 */
public class InstantLongConverter implements AttributeConverter<Instant, Number> {

    @Override
    public Long toGraphProperty(Instant value) {
        if (value == null) return null;
        return value.toEpochMilli();
    }

    @Override
    public Instant toEntityAttribute(Number value) {
        if (value == null) return null;
        return Instant.ofEpochMilli(value.longValue());
    }
}
