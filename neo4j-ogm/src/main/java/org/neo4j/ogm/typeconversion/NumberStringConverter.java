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
 * The NumberStringConverter can be used to convert any java object that extends
 * java.lang.Number to and from its String representation.
 *
 * By default, the OGM will automatically convert BigInteger and BigDecimal
 * entity attributes using this converter.
 *
 */
public class NumberStringConverter implements AttributeConverter<Number, String> {

    private final Class<? extends Number> numberClass;

    public NumberStringConverter(Class<? extends Number> numberClass) {
        this.numberClass = numberClass;
    }

    @Override
    public String toGraphProperty(Number value) {
        if (value == null) return null;
        return value.toString();
    }

    @Override
    public Number toEntityAttribute(String value) {
        if (value == null) return null;
        try {
            return numberClass.getDeclaredConstructor(String.class).newInstance(value);
        } catch (Exception e) {
            throw new RuntimeException("Conversion failed!", e);
        }
    }
}
