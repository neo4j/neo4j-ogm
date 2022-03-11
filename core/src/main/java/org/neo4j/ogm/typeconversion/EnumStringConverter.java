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

import org.neo4j.ogm.utils.StringUtils;

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
        if (value == null) {
            return null;
        }
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
