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

package org.neo4j.ogm.session.request.strategy;

import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.cypher.query.GraphModelQuery;

import java.util.Collection;

public interface QueryStatements {

    /**
     * construct a query to fetch a single object with the specified id
     * @param id the id of the object to find
     * @param depth the depth to traverse for any related objects
     * @return a Cypher expression
     */
    GraphModelQuery findOne(Long id, int depth);

    /**
     * construct a query to fetch all objects with the specified ids
     * @param ids the ids of the objects to find
     * @param depth the depth to traverse for any related objects
     * @return a Cypher expression
     */
    GraphModelQuery findAll(Collection<Long> ids, int depth);

    /**
     * construct a query to fetch all objects
     * @return a Cypher expression
     */
    GraphModelQuery findAll();

    /**
     * construct a query to fetch all objects with the specified label
     * @param label the labels attached to the objects
     * @param depth the depth to traverse for related objects
     * @return a Cypher expression
     */
    GraphModelQuery findByLabel(String label, int depth);

    /**
     * construct a query to fetch all objects with the specified label and property
     * @param label the label value to filter on
     * @param property a property<K,V> value to filter on
     * @param depth the depth to traverse for related objects
     * @return a Cypher expression
     */
    GraphModelQuery findByProperty(String label, Property<String, Object> property, int depth);

}
