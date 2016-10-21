/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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
package org.neo4j.ogm.session;

import java.util.Collection;
import java.util.Map;

import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.model.QueryStatistics;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public interface Capability {

    interface LoadByIds  {

        // load objects of Class type with ids ...
        <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids);
        <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids, int depth);
        <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids, SortOrder sortOrder);
        <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids, SortOrder sortOrder, int depth);
        <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids, Pagination paging);
        <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids, Pagination paging, int depth);
        <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids, SortOrder sortOrder, Pagination pagination);
        <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids, SortOrder sortOrder, Pagination pagination, int depth);

    }

    interface LoadByInstances {

        // load specific objects
        <T> Collection<T> loadAll(Collection<T> objects);
        <T> Collection<T> loadAll(Collection<T> objects, int depth);
        <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder);
        <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder, int depth);
        <T> Collection<T> loadAll(Collection<T> objects, Pagination pagination);
        <T> Collection<T> loadAll(Collection<T> objects, Pagination pagination, int depth);
        <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder, Pagination pagination);
        <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder, Pagination pagination, int depth);
    }

    interface LoadByType {

        // load all objects of class type
        <T> Collection<T> loadAll(Class<T> type);
        <T> Collection<T> loadAll(Class<T> type, int depth);
        <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder);
        <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder, int depth);
        <T> Collection<T> loadAll(Class<T> type, Pagination paging);
        <T> Collection<T> loadAll(Class<T> type, Pagination paging, int depth);
        <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder, Pagination pagination);
        <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder, Pagination pagination, int depth);

        // load objects of Class type with a single property
        // TODO: remove these single property things
        <T> Collection<T> loadAll(Class<T> type, Filter filter);
        <T> Collection<T> loadAll(Class<T> type, Filter filter, int depth);
        <T> Collection<T> loadAll(Class<T> type, Filter filter, SortOrder sortOrder);
        <T> Collection<T> loadAll(Class<T> type, Filter filter, SortOrder sortOrder, int depth);
        <T> Collection<T> loadAll(Class<T> type, Filter filter, Pagination pagination);
        <T> Collection<T> loadAll(Class<T> type, Filter filter, Pagination pagination, int depth);
        <T> Collection<T> loadAll(Class<T> type, Filter filter, SortOrder sortOrder, Pagination pagination);
        <T> Collection<T> loadAll(Class<T> type, Filter filter, SortOrder sortOrder, Pagination pagination, int depth);

        <T> Collection<T> loadAll(Class<T> type, Filters filters);
        <T> Collection<T> loadAll(Class<T> type, Filters filters, int depth);
        <T> Collection<T> loadAll(Class<T> type, Filters filters, SortOrder sortOrder);
        <T> Collection<T> loadAll(Class<T> type, Filters filters, SortOrder sortOrder, int depth);
        <T> Collection<T> loadAll(Class<T> type, Filters filters, Pagination pagination);
        <T> Collection<T> loadAll(Class<T> type, Filters filters, Pagination pagination, int depth);
        <T> Collection<T> loadAll(Class<T> type, Filters filters, SortOrder sortOrder, Pagination pagination);
        <T> Collection<T> loadAll(Class<T> type, Filters filters, SortOrder sortOrder, Pagination pagination, int depth);

    }

    interface LoadOne {

        // load a single object of Class type, with id id
        <T> T load(Class<T> type, Long id);
        <T> T load(Class<T> type, Long id, int depth);
    }

    interface Save {
        <T> void save(T object);
        <T> void save(T object, int depth);
    }

    interface Delete {
        <T> void delete(T object);
        <T> void deleteAll(Class<T> type);
        void purgeDatabase();
        void clear();
    }

    interface Transactions {
        /**
         * Get the existing transaction if available
         *
         * @return an active Transaction, or null if none exists
         */
        Transaction getTransaction();

        /**
         * Begin a new READ_WRITE transaction. If an existing transaction already exists, users must
         * decide whether to commit or rollback. Only one transaction can be bound to a thread
         * at any time, so active transactions that have not been closed but are no longer bound
         * to the thread must be handled by client code.
         *
         * @return a new active Transaction
         */
        Transaction beginTransaction();

        /**
         * Begin a new transaction, passing in the required type (READ_ONLY, READ_WRITE).
         * If an existing transaction already exists, users must
         * decide whether to commit or rollback. Only one transaction can be bound to a thread
         * at any time, so active transactions that have not been closed but are no longer bound
         * to the thread must be handled by client code.
         *
         * @param type the {@link Transaction.Type} required for this transaction
         * @return a new active Transaction
         */
        Transaction beginTransaction(Transaction.Type type);
        /**
         * Applies the given {@link GraphCallback} in the scope of this {@link Session}, giving fine-grained control over
         * behaviour.
         *
         * @param <T> The type of object returned from applying this callback
         * @param graphCallback The {@link GraphCallback} to execute
         * @return The result of calling the given {@link GraphCallback}
         * @throws NullPointerException if invoked with <code>null</code>
         */
        @Deprecated
        <T> T doInTransaction(GraphCallback<T> graphCallback);

    }

    interface ExecuteQueries {
        /**
         * Given a cypher statement this method will return a domain object that is hydrated to the
         * default level or a scalar (depending on the parametrized type).
         *
         * @param objectType The type that should be returned from the query.
         * @param cypher The parametrizable cypher to execute.
         * @param parameters Any scalar parameters to attach to the cypher.
         *
         * @param <T> A domain object or scalar.
         *
         * @return An instance of the objectType that matches the given cypher and parameters. Null if no object
         * is matched
         *
         * @throws java.lang.RuntimeException If more than one object is found.
         */
        <T> T queryForObject(Class<T> objectType, String cypher,  Map<String, ?> parameters);

        /**
         * Given a cypher statement this method will return a collection of domain objects that is hydrated to
         * the default level or a collection of scalars (depending on the parametrized type).
         *
         * @param objectType The type that should be returned from the query.
         * @param cypher The parametrizable cypher to execute.
         * @param parameters Any parameters to attach to the cypher.
         *
         * @param <T> A domain object or scalar.
         *
         * @return A collection of domain objects or scalars as prescribed by the parametrized type.
         */
        <T> Iterable<T> query(Class<T> objectType, String cypher, Map<String, ?> parameters);

        /**
         * Given a cypher statement this method will return a Result object containing a collection of Map's which represent Neo4j
         * objects as properties, along with query statistics if applicable.
         *
         * Each element of the query result is a map which you can access by the name of the returned field
         *
         * TODO: Are we going to use the neo4jOperations conversion method to cast the value object to its proper class?
         *
         * @param cypher  The parametrisable cypher to execute.
         * @param parameters Any parameters to attach to the cypher.
         *
         * @return A {@link Result} containing an {@link Iterable} map representing query results and {@link QueryStatistics} if applicable.
         */
        Result query(String cypher, Map<String, ?> parameters);

        /**
         * Given a cypher statement this method will return a Result object containing a collection of Map's which represent Neo4j
         * objects as properties, along with query statistics if applicable.
         *
         * Each element of the query result is a map which you can access by the name of the returned field
         *
         * TODO: Are we going to use the neo4jOperations conversion method to cast the value object to its proper class?
         *
         * @param cypher  The parametrisable cypher to execute.
         * @param parameters Any parameters to attach to the cypher.
         * @param readOnly true if the query is readOnly, false otherwise
         *
         * @return A {@link Result} of {@link Iterable}s with each entry representing a neo4j object's properties.
         */
        Result query(String cypher, Map<String, ?> parameters, boolean readOnly);

        /**
         * Counts all the <em>node</em> entities of the specified type.
         *
         * @param entity The {@link Class} denoting the type of entity to count
         * @return The number of entities in the database of the given type
         */
        long countEntitiesOfType(Class<?> entity);

    }

    interface GraphId {

        /**
         * Resolve the graph id for a possible entity
         * @param possibleEntity the possible entity
         * @return the value of the {@link GraphId} or null if either the object is not an entity or the id is null.
         */
        Long resolveGraphIdFor(Object possibleEntity);

        boolean detachNodeEntity(Long id);

        boolean detachRelationshipEntity(Long id);
    }
}
