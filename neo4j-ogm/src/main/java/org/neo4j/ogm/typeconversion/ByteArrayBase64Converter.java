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

import org.apache.commons.codec.binary.Base64;

/**
 * By default the OGM will map byte[] objects to Base64
 * String values when being stored as a node / relationship property
 */
public class ByteArrayBase64Converter implements AttributeConverter<byte[], String> {

    @Override
    public String toGraphProperty(byte[] value) {
        if (value == null) return null;
        return Base64.encodeBase64String(value);
    }

    @Override
    public byte[] toEntityAttribute(String value) {
        if (value == null) return null;
        return Base64.decodeBase64(value);
    }

}
