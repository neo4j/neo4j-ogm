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

package org.neo4j.ogm.annotation;

import java.lang.annotation.*;

/**
 * Identifies a domain entity as being backed by a relationship in the graph.
 *
 * This annotation is always needed for relationship-backed entities.
 *
 * The type attribute supplies the relatoionship-type in the graph, and
 * can be omitted if the domain entity's simple class name matches
 * exactly the relationship type.
 *
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface RelationshipEntity {

    static final String CLASS = "org.neo4j.ogm.annotation.RelationshipEntity";
    static final String TYPE = "type";

    String type() default "";
}
