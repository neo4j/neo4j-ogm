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

import java.util.Date;

/**
 * Converter to convert between {@link java.util.Date} and {@link Long}.
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
        if (value == null)
            return null;
        return value.getTime();
    }

    @Override
    public Date toEntityAttribute(Number value) {
        if (value == null)
            return null;
        return new Date(value.longValue());
    }
}
