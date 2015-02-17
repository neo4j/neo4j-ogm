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

import java.util.HashSet;
import java.util.Set;

/**
 * Direct and ancestral interfaces of a given interface.
 */
class InterfaceInfo {

    private final String interfaceName;
    private final Set<InterfaceInfo> superInterfaces = new HashSet<>();

    // what's this for?
    private final Set<InterfaceInfo> allSuperInterfaces = new HashSet<>();

    public InterfaceInfo(String name) {
        this.interfaceName = name;
    }

    public Set<InterfaceInfo> superInterfaces() {
        return superInterfaces;
    }

    public Set<InterfaceInfo> allSuperInterfaces() {
        return allSuperInterfaces;
    }

    String name() {
        return interfaceName;
    }

    public String toString() {
        return name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InterfaceInfo that = (InterfaceInfo) o;

        if (!interfaceName.equals(that.interfaceName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return interfaceName.hashCode();
    }
}
