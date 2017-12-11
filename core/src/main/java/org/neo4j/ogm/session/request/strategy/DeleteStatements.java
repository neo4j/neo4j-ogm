/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.session.request.strategy;

import java.util.Collection;

import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.query.CypherQuery;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.request.Statement;

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
