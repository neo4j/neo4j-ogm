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

import java.util.Collection;

import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.query.CypherQuery;
import org.neo4j.ogm.metadata.ClassInfo;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public interface DeleteStatements {

    /**
     * construct a query to delete a single object with the specified id
     *
     * @param id the id of the object to find
     * @return a {@link CypherQuery}
     */
    CypherQuery delete(Long id);

    /**
     * Construct a query to delete a single object with given id, check for object's version
     *
     * @param id  the id of the object
     * @param object object
     * @param classInfo
     *
     * @return a {@link CypherQuery}
     */
    CypherQuery delete(Long id, Object object, ClassInfo classInfo);

    /**
     * construct a query to delete all objects
     *
     * @return a {@link CypherQuery}
     */
    CypherQuery deleteAll();

    /**
     * construct a query to delete all objects with the specified ids
     *
     * @param ids the ids of the objects to find
     * @return a {@link CypherQuery}
     */
    CypherQuery delete(Collection<Long> ids);

    /**
     * construct queries to delete all objects with the specified label or relationship type
     *
     * @param type the label attached to the object, or the relationship type
     * @return a {@link CypherQuery}
     */
    CypherQuery delete(String type);

    /**
     * construct queries to delete all objects with the specified label that match the specified filters
     *
     * @param type    the label value or relationship type to filter on
     * @param filters parameters to filter on
     * @return a {@link CypherQuery}
     */

    CypherQuery delete(String type, Iterable<Filter> filters);

    /**
     * construct queries to delete all objects with the specified label that match the specified filters and return a list of deleted object ids
     *
     * @param type    the label value or relationship type to filter on
     * @param filters parameters to filter on
     * @return a {@link CypherQuery}
     */

    CypherQuery deleteAndList(String type, Iterable<Filter> filters);
}
