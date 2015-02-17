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

interface ConstantPoolTags {

    static final int UTF_8          = 1;
    static final int INTEGER        = 3;
    static final int FLOAT          = 4;
    static final int LONG           = 5;
    static final int DOUBLE         = 6;
    static final int CLASS          = 7;
    static final int STRING         = 8;
    static final int FIELD_REF      = 9;
    static final int METHOD_REF     =10;
    static final int INTERFACE_REF  =11;
    static final int NAME_AND_TYPE  =12;
    static final int METHOD_HANDLE  =15;
    static final int METHOD_TYPE    =16;
    static final int INVOKE_DYNAMIC =18;

}
