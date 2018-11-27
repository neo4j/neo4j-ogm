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

import org.apache.commons.lang3.StringUtils;

/**
 * By default the OGM will map enum objects to and from
 * the string value returned by enum.name()
 * enum.name() is preferred to enum.ordinal() because it
 * is (slightly) safer: a persisted enum have to be renamed
 * to break its database mapping, whereas if its ordinal
 * was persisted instead, the mapping would be broken
 * simply by changing the declaration order of the enum constants.
 *
 * @author Vince Bickers
 * @author Gerrit Meier
 * @author Róbert Papp
 */
public class EnumStringConverter implements AttributeConverter<Enum, String> {

    private final Class<? extends Enum> enumClass;
    private final boolean lenient;

    public EnumStringConverter(Class<? extends Enum> enumClass) {
        this.enumClass = enumClass;
        this.lenient = false;
    }

    public EnumStringConverter(Class<? extends Enum> enumClass, boolean lenient) {
        this.enumClass = enumClass;
        this.lenient = lenient;
    }

    @Override
    public String toGraphProperty(Enum value) {
        if (value == null)
            return null;
        return value.name();
    }

    @Override
    public Enum toEntityAttribute(String value) {
        if (value == null || (lenient && StringUtils.isBlank(value))) {
            return null;
        }
        return Enum.valueOf(enumClass, value);
    }
}
