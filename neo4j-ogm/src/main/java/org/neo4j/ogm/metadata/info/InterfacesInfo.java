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

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class InterfacesInfo {

    private final Map<String, InterfaceInfo> interfaceMap = new HashMap<>();

    InterfacesInfo() {}

    public InterfacesInfo(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        int interfaceCount = dataInputStream.readUnsignedShort();
        for (int i = 0; i < interfaceCount; i++) {
            String interfaceName = constantPool.lookup(dataInputStream.readUnsignedShort()).replace('/', '.');
            interfaceMap.put(interfaceName, new InterfaceInfo(interfaceName));
        }
    }

    public Collection<InterfaceInfo> list() {
        return interfaceMap.values();
    }

}
