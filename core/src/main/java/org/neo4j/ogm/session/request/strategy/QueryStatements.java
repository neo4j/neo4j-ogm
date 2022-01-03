/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.session.request.strategy;

import java.io.Serializable;
import java.util.Collection;

import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.PagingAndSortingQuery;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
public interface QueryStatements<ID extends Serializable> {

    /**
     * construct a query to fetch a single object with the specified id
     *
     * @param id    the id of the object to find
     * @param depth the depth to traverse for any related objects
     * @return a {@link PagingAndSortingQuery}
     */
    PagingAndSortingQuery findOne(ID id, int depth);

    /**
     * construct a query to fetch a single object with the specified id of a specific type
     *
     * @param label the label attached to the object or relationship type
     * @param id    the id of the object to find
     * @param depth the depth to traverse for any related objects
     * @return a {@link PagingAndSortingQuery}
     */
    PagingAndSortingQuery findOneByType(String label, ID id, int depth);

    /**
     * construct a query to fetch all objects with the specified ids
     *
     * @param type  the label attached to the object, or the relationship type
     * @param ids   the ids of the objects to find
     * @param depth the depth to traverse for any related objects
     * @return a {@link PagingAndSortingQuery}
     */
    PagingAndSortingQuery findAllByType(String type, Collection<ID> ids, int depth);

    /**
     * construct queries to fetch all objects with the specified label or relationship type
     *
     * @param type  the label attached to the object, or the relationship type
     * @param depth the depth to traverse for related objects
     * @return a {@link PagingAndSortingQuery}
     */
    PagingAndSortingQuery findByType(String type, int depth);

    /**
     * construct queries to fetch all objects with the specified label that match the specified filters
     *
     * @param type    the label value or relationship type to filter on
     * @param filters parameters to filter on
     * @param depth   the depth to traverse for related objects
     * @return a {@link PagingAndSortingQuery}
     */

    PagingAndSortingQuery findByType(String type, Filters filters, int depth);
}
