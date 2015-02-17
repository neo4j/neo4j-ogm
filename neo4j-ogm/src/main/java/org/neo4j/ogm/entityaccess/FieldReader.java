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

package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;

public class FieldReader implements RelationalReader, PropertyReader {

    private final ClassInfo classInfo;
    private final FieldInfo fieldInfo;

    FieldReader(ClassInfo classInfo, FieldInfo fieldInfo) {
        this.classInfo = classInfo;
        this.fieldInfo = fieldInfo;
    }

    @Override
    public Object read(Object instance) {
        Object value = FieldWriter.read(classInfo.getField(fieldInfo), instance);
        if (fieldInfo.hasConverter()) {
            value = fieldInfo.converter().toGraphProperty(value);
        }
        return value;
    }

    @Override
    public String relationshipType() {
        return fieldInfo.relationship();
    }

    @Override
    public String propertyName() {
        return fieldInfo.property();
    }

    @Override
    public String relationshipDirection() {
        try {
            return fieldInfo.getAnnotations().get(Relationship.CLASS).get(Relationship.DIRECTION, Relationship.OUTGOING);
        } catch (NullPointerException npe) {
            return Relationship.OUTGOING;
        }
    }

}
