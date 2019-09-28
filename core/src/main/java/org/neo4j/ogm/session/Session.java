/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.ogm.session;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

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
 * A {@link Session} serves as the main point of integration for the Neo4j OGM.
 * All the publicly-available capabilities of the framework are defined by this interface.
 * Instances of Session are not thread safe.
 * In a typical application scenario there should be a Session instance per thread.
 * A broader scope may be chosen, but the access to the Session must be synchronized externally.
 * <h2>Load methods</h2>
 * Methods {@code load(java.lang.Class, java.io.Serializable)} load single instance of class by id.<br>
 * Methods {@code loadAll(java.lang.Class, java.util.Collection)} load multiple instances of same class by ids.
 * Note that if an entity with id is not found it is simply omitted from the results.<br>
 * Methods {@code loadAll(java.lang.Class, org.neo4j.ogm.cypher.Filter)} queries multiple instances of class by
 * filter<br>
 * <h2>Load and save depth parameter</h2>
 * The depth parameter tells OGM how many steps to follow when loading or saving entities.
 * Each relationship counts as one step (regardless type - simple relationship or
 * {@link org.neo4j.ogm.annotation.RelationshipEntity}).
 * When loading entities the depth parameter is reflected by the query (also see {@link LoadStrategy}).
 * When saving entities the depth parameter defines a horizon from root entity for checking for modified entities.
 * <h2>Session cache</h2>
 * When loading entities already present in Session following happens:
 * <ul>
 * <li>cached instance of the entity is returned
 * <li>all property fields are set to original values - <b>even when loaded entity is
 * updated</b></li>
 * <li>relationship fields are merged with loaded relationships - new relationships may be added to existing
 * relationship collections</li>
 * </ul>
 * If new state of the entity is required when reloading, use {@link Session#clear()} to clear current session before
 * reload.
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 * @author Frantisek Hartman
 * @see SessionFactory
 */
public interface Session {

    /**
     * Load entities of type by their ids, with default depth = 1.
     *
     * @param type type of entities
     * @param ids  ids of entities to load
     * @return collection of entities
     */
    <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids);

    /**
     * Load entities of type by their ids.
     *
     * @param type  type of entities
     * @param ids   ids of entities to load
     * @param depth depth
     * @return collection of entities
     */
    <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, int depth);

    /**
     * Load entities of type by their ids, with default depth = 1.
     *
     * @param type      type of entities
     * @param ids       ids of entities to load
     * @param sortOrder sort order
     * @return collection of entities
     */
    <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, SortOrder sortOrder);

    /**
     * Load entities of type by their ids.
     *
     * @param type      type of entities
     * @param ids       ids of entities to load
     * @param sortOrder sort order
     * @param depth     depth
     * @return collection of entities
     */
    <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, SortOrder sortOrder,
        int depth);

    /**
     * Load entities of type by their ids, with default depth = 1.
     *
     * @param type       type of entities
     * @param ids        ids of entities to load
     * @param pagination pagination
     * @return collection of entities
     */
    <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, Pagination pagination);

    /**
     * Load entities of type by their ids.
     *
     * @param type       type of entities
     * @param ids        ids of entities to load
     * @param pagination pagination
     * @param depth      depth
     * @return collection of entities
     */
    <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, Pagination pagination,
        int depth);

    /**
     * Load entities of type by their ids, with default depth = 1.
     *
     * @param type       type of entities
     * @param ids        ids of entities to load
     * @param sortOrder  sort order
     * @param pagination pagination
     * @return collection of entities
     */
    <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, SortOrder sortOrder,
        Pagination pagination);

    /**
     * Load entities of type by their ids.
     *
     * @param type       type of entities
     * @param ids        ids of entities to load
     * @param sortOrder  sort order
     * @param pagination pagination
     * @param depth      depth
     * @return collection of entities
     */
    <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, SortOrder sortOrder,
        Pagination pagination, int depth);

    /**
     * Load entities by themselves - uses id of the entity to load it again, with default depth = 1.
     * Note that standard session behaviour regarding entity loading an reloading applies.
     *
     * @param objects objects
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Collection<T> objects);

    /**
     * Load entities by themselves - uses id of the entity to load it again.
     * Note that standard session behaviour regarding entity loading an reloading applies.
     *
     * @param objects objects
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Collection<T> objects, int depth);

    /**
     * Load entities by themselves - uses id of the entity to load it again, with default depth = 1.
     * Note that standard session behaviour regarding entity loading an reloading applies.
     *
     * @param objects   objects
     * @param sortOrder sort order
     * @return collection of entities
     */

    <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder);

    /**
     * Load entities by themselves - uses id of the entity to load it again.
     * Note that standard session behaviour regarding entity loading an reloading applies.
     *
     * @param objects   objects
     * @param sortOrder sort order
     * @param depth     depth
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder, int depth);

    /**
     * Load entities by themselves - uses id of the entity to load it again, with default depth = 1.
     * Note that standard session behaviour regarding entity loading an reloading applies.
     *
     * @param objects    objects
     * @param pagination pagination
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Collection<T> objects, Pagination pagination);

    /**
     * Load entities by themselves - uses id of the entity to load it again.
     * Note that standard session behaviour regarding entity loading an reloading applies.
     *
     * @param objects    objects
     * @param pagination pagination
     * @param depth      depth
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Collection<T> objects, Pagination pagination, int depth);

    /**
     * Load entities by themselves - uses id of the entity to load it again, with default depth = 1.
     * Note that standard session behaviour regarding entity loading an reloading applies.
     *
     * @param objects    objects
     * @param sortOrder  sort order
     * @param pagination pagination
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder, Pagination pagination);

    /**
     * Load entities by themselves - uses id of the entity to load it again.
     * Note that standard session behaviour regarding entity loading an reloading applies.
     *
     * @param objects    objects
     * @param sortOrder  sort order
     * @param pagination pagination
     * @param depth      depth
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder, Pagination pagination, int depth);

    /**
     * Load all entities of type, with default depth = 1.
     *
     * @param type type of entities
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type);

    /**
     * Load all entities of type, with depth
     *
     * @param type  type of entities
     * @param depth depth
     * @return collection of entities
     */

    <T> Collection<T> loadAll(Class<T> type, int depth);

    /**
     * Load all entities of type, with default depth = 1
     *
     * @param type      type of entities
     * @param sortOrder sort order
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder);

    /**
     * Load all entities of type.
     *
     * @param type      type of entities
     * @param sortOrder sort order
     * @param depth     depth
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder, int depth);

    /**
     * Load all entities of type, with default depth = 1.
     *
     * @param type       type of entities
     * @param pagination pagination
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type, Pagination pagination);

    /**
     * Load all entities of type.
     *
     * @param type       type of entities
     * @param pagination pagination
     * @param depth      depth
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type, Pagination pagination, int depth);

    /**
     * Load all entities of type, with default depth = 1.
     *
     * @param type       type of entities
     * @param sortOrder  sort order
     * @param pagination pagination
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder, Pagination pagination);

    /**
     * Load all entities of type.
     *
     * @param type       type of entities
     * @param sortOrder  sort order
     * @param pagination pagination
     * @param depth      depth
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder, Pagination pagination, int depth);

    /**
     * Load all entities of type, filtered by filter, with default depth = 1.
     *
     * @param type   type of entities
     * @param filter filter
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type, Filter filter);

    /**
     * Load all entities of type, filtered by filter.
     *
     * @param type   type of entities
     * @param filter filter
     * @param depth  depth
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type, Filter filter, int depth);

    /**
     * Load all entities of type, filtered by filter, with default depth = 1.
     *
     * @param type      type of entities
     * @param filter    filter
     * @param sortOrder sort order
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type, Filter filter, SortOrder sortOrder);

    /**
     * Load all entities of type, filtered by filter.
     *
     * @param type      type of entities
     * @param filter    filter
     * @param sortOrder sort order
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type, Filter filter, SortOrder sortOrder, int depth);

    /**
     * Load all entities of type, filtered by filter, with default depth = 1.
     *
     * @param type       type of entities
     * @param filter     filter
     * @param pagination pagination
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type, Filter filter, Pagination pagination);

    /**
     * Load all entities of type, filtered by filter.
     *
     * @param type       type of entities
     * @param filter     filter
     * @param pagination pagination
     * @param depth      depth
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type, Filter filter, Pagination pagination, int depth);

    /**
     * Load all entities of type, filtered by filter, with default depth = 1.
     *
     * @param type       type of entities
     * @param filter     filter
     * @param sortOrder  sort order
     * @param pagination pagination
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type, Filter filter, SortOrder sortOrder, Pagination pagination);

    /**
     * Load all entities of type, filtered by filter.
     *
     * @param type       type of entities
     * @param filter     filter
     * @param sortOrder  sort order
     * @param pagination pagination
     * @param depth      depth
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type, Filter filter, SortOrder sortOrder, Pagination pagination, int depth);

    /**
     * Load all entities of type, filtered by filters, with default depth = 1.
     *
     * @param type    type of entities
     * @param filters filters
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type, Filters filters);

    /**
     * Load all entities of type, filtered by filters.
     *
     * @param type    type of entities
     * @param filters filters
     * @param depth   depth
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type, Filters filters, int depth);

    /**
     * Load all entities of type, filtered by filters, with default depth = 1.
     *
     * @param type      type of entities
     * @param filters   filters
     * @param sortOrder sort order
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type, Filters filters, SortOrder sortOrder);

    /**
     * Load all entities of type, filtered by filters.
     *
     * @param type      type of entities
     * @param filters   filters
     * @param sortOrder sort order
     * @param depth     depth
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type, Filters filters, SortOrder sortOrder, int depth);

    /**
     * Load all entities of type, filtered by filters, with default depth = 1.
     *
     * @param type       type of entities
     * @param filters    filters
     * @param pagination pagination
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type, Filters filters, Pagination pagination);

    /**
     * Load all entities of type, filtered by filters.
     *
     * @param type       type of entities
     * @param filters    filters
     * @param pagination pagination
     * @param depth      depth
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type, Filters filters, Pagination pagination, int depth);

    /**
     * Load all entities of type, filtered by filters, with default depth = 1.
     *
     * @param type       type of entities
     * @param filters    filters
     * @param sortOrder  sort order
     * @param pagination pagination
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type, Filters filters, SortOrder sortOrder, Pagination pagination);

    /**
     * Load all entities of type, filtered by filters.
     *
     * @param type       type of entities
     * @param filters    filters
     * @param sortOrder  sort order
     * @param pagination pagination
     * @param depth      depth
     * @return collection of entities
     */
    <T> Collection<T> loadAll(Class<T> type, Filters filters, SortOrder sortOrder, Pagination pagination, int depth);

    /**
     * Load single entity instance of type, with default depth = 1
     *
     * @return entity instance, null if not found
     */
    <T, ID extends Serializable> T load(Class<T> type, ID id);

    /**
     * Load single entity instance of type, with depth
     *
     * @return entity instance, null if not found
     */
    <T, ID extends Serializable> T load(Class<T> type, ID id, int depth);

    /**
     * Save entity(or entities) into the database, up to specified depth
     * The entities are either created or updated.
     * When new objects are saved and the objects have a field that is mapped with {@link org.neo4j.ogm.annotation.Id}
     * using the internal Id-generation-strategy, those fields are modified and set to the internal graph-id.
     *
     * @param object object to save, may be single entity, array of entities or {@link Iterable}
     */
    <T> void save(T object);

    /**
     * Save entity(or entities) into the database, up to specified depth
     * The objects are either created or updated.
     * When new objects are saved and the objects have a field that is mapped with {@link org.neo4j.ogm.annotation.Id}
     * using the internal Id-generation-strategy, those fields are modified and set to the internal graph-id.
     *
     * @param object object to save, may be single entity, array of entities or {@link Iterable}
     */
    <T> void save(T object, int depth);

    /**
     * Delete entity (or entities)
     *
     * @param object object to delete, may be single entity, array of entities or {@link Iterable}
     */
    <T> void delete(T object);

    /**
     * Delete all entities of type
     *
     * @param type type of the entities to delete
     */
    <T> void deleteAll(Class<T> type);

    /**
     * Delete all entities of type matching filter
     *
     * @param type        type of the entities to delete
     * @param filters     filters to match entities to delete
     * @param listResults true if ids of deleted entities should be returned, false to return count only
     * @return ids or deleted entities or count of deleted entities
     */
    <T> Object delete(Class<T> type, Iterable<Filter> filters, boolean listResults);

    /**
     * Delete all nodes in the database.
     * The delete is performed in a single transaction so it may not be suitable for large data sets.
     * NOTE: This will delete all data, not only nodes/relationships with matching metadata in the domain.
     */
    void purgeDatabase();

    /**
     * Clears the Session
     */
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
     * Begin a new transaction, passing in the required type and bookmarks
     * If an existing transaction already exists, users must
     * decide whether to commit or rollback. Only one transaction can be bound to a thread
     * at any time, so active transactions that have not been closed but are no longer bound
     * to the thread must be handled by client code.
     *
     * @param type      the {@link Transaction.Type} required for this transaction
     * @param bookmarks bookmarks that are passed to driver
     * @return a new active Transaction
     */
    Transaction beginTransaction(Transaction.Type type, Iterable<String> bookmarks);

    /**
     * a cypher statement this method will return a domain object that is hydrated to the
     * default level or a scalar (depending on the parametrized type).
     *
     * @param objectType The type that should be returned from the query.
     * @param cypher     The parameterizable cypher to execute.
     * @param parameters Any scalar parameters to attach to the cypher.
     * @param <T>        A domain object or scalar.
     * @return An instance of the objectType that matches the cypher and parameters. Null if no object
     * is matched
     * @throws java.lang.RuntimeException If more than one object is found.
     */
    <T> T queryForObject(Class<T> objectType, String cypher, Map<String, ?> parameters);

    /**
     * a cypher statement this method will return a collection of domain objects that is hydrated to
     * the default level or a collection of scalars (depending on the parametrized type).
     *
     * @param objectType The type that should be returned from the query.
     * @param cypher     The parameterizable cypher to execute.
     * @param parameters Any parameters to attach to the cypher.
     * @param <T>        A domain object or scalar.
     * @return A collection of domain objects or scalars as prescribed by the parametrized type.
     */
    <T> Iterable<T> query(Class<T> objectType, String cypher, Map<String, ?> parameters);

    /**
     * a cypher statement this method will return a Result object containing a collection of Map's which represent Neo4j
     * objects as properties, along with query statistics if applicable.
     * Each element of the query result is a map which you can access by the name of the returned field
     * TODO: Are we going to use the neo4jOperations conversion method to cast the value object to its proper class?
     *
     * @param cypher     The parameterisable cypher to execute.
     * @param parameters Any parameters to attach to the cypher.
     * @return A {@link Result} containing an {@link Iterable} map representing query results and {@link QueryStatistics} if applicable.
     */
    Result query(String cypher, Map<String, ?> parameters);

    /**
     * a cypher statement this method will return a Result object containing a collection of Map's which represent Neo4j
     * objects as properties, along with query statistics if applicable.
     * Each element of the query result is a map which you can access by the name of the returned field
     * TODO: Are we going to use the neo4jOperations conversion method to cast the value object to its proper class?
     *
     * @param cypher     The parameterisable cypher to execute.
     * @param parameters Any parameters to attach to the cypher.
     * @param readOnly   true if the query is readOnly, false otherwise
     * @return A {@link Result} of {@link Iterable}s with each entry representing a neo4j object's properties.
     */
    Result query(String cypher, Map<String, ?> parameters, boolean readOnly);

    /**
     * Counts all the <em>node</em> entities of the specified type.
     *
     * @param entity The {@link Class} denoting the type of entity to count
     * @return The number of entities in the database of the type
     */
    long countEntitiesOfType(Class<?> entity);

    /**
     * Counts all the <em>node</em> entities of the specified type which match the filters supplied
     *
     * @param clazz   The {@link Class} denoting the type of entity to count
     * @param filters a collection of {@link Filter} objects used as additional parameters to the query
     * @return The number of entities in the database of the type matched by the filters
     */
    long count(Class<?> clazz, Iterable<Filter> filters);

    /**
     * Resolve the graph id for a possible entity.
     *
     * @param possibleEntity the possible entity
     * @return the value of the internal graph id for the possible entity. Returns null if either the
     * object is not an entity or the id is null.
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
     * @return <code>true</code> if successfully registered.
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

    /**
     * Returns current load strategy
     *
     * @return load strategy
     */
    LoadStrategy getLoadStrategy();

    /**
     * Sets the LoadStrategy
     * Will be used for all subsequent queries.
     */
    void setLoadStrategy(LoadStrategy loadStrategy);
}
