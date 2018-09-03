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

import java.time.OffsetDateTime;

/**
 * Converter to convert {@link OffsetDateTime} to {@link String}.
 * Stores values in db as string in format YYYY-MM-DDTHH-MM-SS+01:00.
 *
 * @author Frantisek Hartman
 */
public class OffsetDateTimeStringConverter implements AttributeConverter<OffsetDateTime, String> {

    @Override
    public String toGraphProperty(OffsetDateTime value) {
        if (value == null)
            return null;
        return value.toString();
    }

    @Override
    public OffsetDateTime toEntityAttribute(String value) {
        if (value == null)
            return null;
        return OffsetDateTime.parse(value);
    }
}
