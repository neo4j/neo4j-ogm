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

import java.lang.reflect.Array;

/**
 * By default the OGM will map enum arrays to and from
 * the string arrays with values returned by enum.name()
 * enum.name() is preferred to enum.ordinal() because it
 * is (slightly) safer: a persisted enum have to be renamed
 * to break its database mapping, whereas if its ordinal
 * was persisted instead, the mapping would be broken
 * simply by changing the declaration order of the enum constants.
 *
 * @author Luanne Misquitta
 * @author Róbert Papp
 */
public class EnumArrayStringConverter implements AttributeConverter<Enum[], String[]> {

    private final Class<? extends Enum> enumClass;

    public EnumArrayStringConverter(Class<? extends Enum> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public String[] toGraphProperty(Enum[] value) {
        if (value == null) {
            return null;
        }
        String[] values = new String[(value.length)];
        int i = 0;
        for (Enum e : value) {
            values[i++] = e.name();
        }
        return values;
    }

    @Override
    public Enum[] toEntityAttribute(String[] stringValues) {
        if (stringValues == null) {
            return null;
        }
        Enum[] values = (Enum[]) Array.newInstance(enumClass, stringValues.length);
        int i = 0;
        for (String value : stringValues) {
            values[i++] = Enum.valueOf(enumClass, value);
        }
        return values;
    }
}
