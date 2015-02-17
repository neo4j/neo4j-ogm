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

package org.neo4j.ogm.domain.convertible.date;

import org.neo4j.ogm.typeconversion.AttributeConverter;
import org.neo4j.ogm.typeconversion.DateStringConverter;

import java.util.Date;

public class DateNumericStringConverter implements AttributeConverter<Date, String> {

    private final DateStringConverter converter = new DateStringConverter("yyyyMMddhhmmss");

    @Override
    public String toGraphProperty(Date value) {
        return converter.toGraphProperty(value);
    }

    @Override
    public Date toEntityAttribute(String value) {
        return converter.toEntityAttribute(value);
    }
}
