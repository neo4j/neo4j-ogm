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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.model.QueryStatistics;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.event.Event;
import org.neo4j.ogm.session.event.EventListener;
import org.neo4j.ogm.transaction.Transaction;

/**
 * A {@link Session} serves as the main point of integration for the Neo4j OGM.  All the publicly-available capabilities of the
 * framework are defined by this interface.
 * TODO: Add documentation for all methods.
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 * @see SessionFactory
 */
public interface Session {

    <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids);

    <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, int depth);

    <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, SortOrder sortOrder);

    <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, SortOrder sortOrder, int depth);

    <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, Pagination paging);

    <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, Pagination paging, int depth);

    <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, SortOrder sortOrder, Pagination pagination);

    <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, SortOrder sortOrder, Pagination pagination, int depth);

    <T> Collection<T> loadAll(Collection<T> objects);

    <T> Collection<T> loadAll(Collection<T> objects, int depth);

    <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder);

    <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder, int depth);

    <T> Collection<T> loadAll(Collection<T> objects, Pagination pagination);

    <T> Collection<T> loadAll(Collection<T> objects, Pagination pagination, int depth);

    <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder, Pagination pagination);

    <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder, Pagination pagination, int depth);

    <T> Collection<T> loadAll(Class<T> type);

    <T> Collection<T> loadAll(Class<T> type, int depth);

    <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder);

    <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder, int depth);

    <T> Collection<T> loadAll(Class<T> type, Pagination paging);

    <T> Collection<T> loadAll(Class<T> type, Pagination paging, int depth);

    <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder, Pagination pagination);

    <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder, Pagination pagination, int depth);

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

    <T, ID extends Serializable> T load(Class<T> type, ID id);

    <T, ID extends Serializable> T load(Class<T> type, ID id, int depth);

    <T> void save(T object);

    <T> void save(T object, int depth);

    <T> void delete(T object);

    <T> void deleteAll(Class<T> type);

    <T> Object delete(Class<T> type, Iterable<Filter> filters, boolean listResults);

    void purgeDatabase();

    void clear();

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
     * Given a cypher statement this method will return a domain object that is hydrated to the
     * default level or a scalar (depending on the parametrized type).
     *
     * @param objectType The type that should be returned from the query.
     * @param cypher The parametrizable cypher to execute.
     * @param parameters Any scalar parameters to attach to the cypher.
     * @param <T> A domain object or scalar.
     * @return An instance of the objectType that matches the given cypher and parameters. Null if no object
     * is matched
     * @throws java.lang.RuntimeException If more than one object is found.
     */
    <T> T queryForObject(Class<T> objectType, String cypher, Map<String, ?> parameters);

    /**
     * Given a cypher statement this method will return a collection of domain objects that is hydrated to
     * the default level or a collection of scalars (depending on the parametrized type).
     *
     * @param objectType The type that should be returned from the query.
     * @param cypher The parametrizable cypher to execute.
     * @param parameters Any parameters to attach to the cypher.
     * @param <T> A domain object or scalar.
     * @return A collection of domain objects or scalars as prescribed by the parametrized type.
     */
    <T> Iterable<T> query(Class<T> objectType, String cypher, Map<String, ?> parameters);

    /**
     * Given a cypher statement this method will return a Result object containing a collection of Map's which represent Neo4j
     * objects as properties, along with query statistics if applicable.
     * Each element of the query result is a map which you can access by the name of the returned field
     * TODO: Are we going to use the neo4jOperations conversion method to cast the value object to its proper class?
     *
     * @param cypher The parametrisable cypher to execute.
     * @param parameters Any parameters to attach to the cypher.
     * @return A {@link Result} containing an {@link Iterable} map representing query results and {@link QueryStatistics} if applicable.
     */
    Result query(String cypher, Map<String, ?> parameters);

    /**
     * Given a cypher statement this method will return a Result object containing a collection of Map's which represent Neo4j
     * objects as properties, along with query statistics if applicable.
     * Each element of the query result is a map which you can access by the name of the returned field
     * TODO: Are we going to use the neo4jOperations conversion method to cast the value object to its proper class?
     *
     * @param cypher The parametrisable cypher to execute.
     * @param parameters Any parameters to attach to the cypher.
     * @param readOnly true if the query is readOnly, false otherwise
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

    /**
     * Counts all the <em>node</em> entities of the specified type which match the filters supplied
     *
     * @param clazz The {@link Class} denoting the type of entity to count
     * @param filters a collection of {@link Filter} objects used as additional parameters to the query
     * @return The number of entities in the database of the given type matched by the given filters
     */
    long count(Class<?> clazz, Iterable<Filter> filters);

    /**
     * Resolve the graph id for a possible entity.
     *
     * @param possibleEntity the possible entity
     * @return the value of the {@link GraphId} or null if either the object is not an entity or the id is null.
     */
    Long resolveGraphIdFor(Object possibleEntity);

    /**
     * Detach this node entity represented by the supplied Graph ID from the session.
     *
     * @param id the node id to detach.
     * @return <code>true</code> if detached successfully.
     */
    boolean detachNodeEntity(Long id);

    /**
     * Detach this relationship entity represented by the supplied Graph ID from the session.
     *
     * @param id the relationship id to detach.
     * @return <code>true</code> if detached successfully.
     */
    boolean detachRelationshipEntity(Long id);

    /**
     * Register an event listener with this session.
     *
     * @param eventListener The listener to register.
     * @return the registered event listener.
     */
    EventListener register(EventListener eventListener);

    /**
     * Remove an event listener from this session.
     *
     * @param eventListener The listener to deregister
     * @return <code>true</code> if successfully regisistered.
     */
    boolean dispose(EventListener eventListener);

    /**
     * Notifies listeners of this session of the supplied <code>Event</code>.
     *
     * @param event The event to inform listeners with.
     */
    void notifyListeners(Event event);

    /**
     * Determines if events are enabled for this session.
     *
     * @return <code>true</code> if this session allows events.
     */
    boolean eventsEnabled();

    /**
     * Retrieves the last bookmark used in this session when used in a Neo4j Causal Cluster.
     * This bookmark can be used to ensure the cluster is consistent before performing a read/write.
     *
     * @return The last used bookmark String on this session.
     */
    String getLastBookmark();

    /**
     * Sets the bookmark to use on this session. Useful when resuming a user session with a causal cluster.
     *
     * @param bookmark The last used bookmark String that this session should start from.
     */
    void withBookmark(String bookmark);
}
