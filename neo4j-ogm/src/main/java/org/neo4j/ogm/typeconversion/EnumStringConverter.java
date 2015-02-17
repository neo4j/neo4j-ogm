/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.typeconversion;

/**
 * By default the OGM will map enum objects to and from
 * the string value returned by enum.name()
 *
 * enum.name() is preferred to enum.ordinal() because it
 * is (slightly) safer: a persisted enum have to be renamed
 * to break its database mapping, whereas if its ordinal
 * was persisted instead, the mapping would be broken
 * simply by changing the declaration order in the enum set.
 */
public class EnumStringConverter implements AttributeConverter<Enum, String> {

    private final Class<? extends Enum> enumClass;

    public EnumStringConverter(Class<? extends Enum> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public String toGraphProperty(Enum value) {
        if (value == null) return null;
        return value.name();
    }

    @Override
    public Enum toEntityAttribute(String value) {
        if (value == null) return null;
        return Enum.valueOf(enumClass, value.toString());
    }

}
