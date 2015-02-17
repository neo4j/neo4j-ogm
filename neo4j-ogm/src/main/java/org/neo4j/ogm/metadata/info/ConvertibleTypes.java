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

package org.neo4j.ogm.metadata.info;

import org.neo4j.ogm.annotation.typeconversion.DateString;
import org.neo4j.ogm.typeconversion.*;

import java.math.BigDecimal;
import java.math.BigInteger;

public abstract class ConvertibleTypes {

    public static AttributeConverter<?, ?> getDateConverter() {
        return new DateStringConverter(DateString.ISO_8601);
    }

    public static AttributeConverter<?, ?> getEnumConverter(String enumDescriptor) {
        String className = enumDescriptor.replace("/", ".");
        try {
            Class clazz = Class.forName(className);
            return new EnumStringConverter(clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static AttributeConverter<?,?> getBigIntegerConverter() {
        return new NumberStringConverter(BigInteger.class);
    }

    public static AttributeConverter<?, ?> getBigDecimalConverter() {
        return new NumberStringConverter(BigDecimal.class);
    }

    public static AttributeConverter<?, ?> getByteArrayBase64Converter() {
        return new ByteArrayBase64Converter();
    }

    public static AttributeConverter<?, ?> getByteArrayWrapperBase64Converter() {
        return new ByteArrayWrapperBase64Converter();
    }

}
