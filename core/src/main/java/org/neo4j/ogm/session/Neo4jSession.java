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

package org.neo4j.ogm.session;

import static java.util.Collections.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.context.MappingContext;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.SortClause;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.metadata.AnnotationInfo;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.reflect.ReflectionEntityInstantiator;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.session.delegates.DeleteDelegate;
import org.neo4j.ogm.session.delegates.ExecuteQueriesDelegate;
import org.neo4j.ogm.session.delegates.GraphIdDelegate;
import org.neo4j.ogm.session.delegates.LoadByIdsDelegate;
import org.neo4j.ogm.session.delegates.LoadByInstancesDelegate;
import org.neo4j.ogm.session.delegates.LoadByTypeDelegate;
import org.neo4j.ogm.session.delegates.LoadOneDelegate;
import org.neo4j.ogm.session.delegates.SaveDelegate;
import org.neo4j.ogm.session.event.Event;
import org.neo4j.ogm.session.event.EventListener;
import org.neo4j.ogm.session.request.OptimisticLockingChecker;
import org.neo4j.ogm.session.request.strategy.LoadClauseBuilder;
import org.neo4j.ogm.session.request.strategy.QueryStatements;
import org.neo4j.ogm.session.request.strategy.impl.NodeQueryStatements;
import org.neo4j.ogm.session.request.strategy.impl.PathNodeLoadClauseBuilder;
import org.neo4j.ogm.session.request.strategy.impl.PathRelationshipLoadClauseBuilder;
import org.neo4j.ogm.session.request.strategy.impl.RelationshipQueryStatements;
import org.neo4j.ogm.session.request.strategy.impl.SchemaNodeLoadClauseBuilder;
import org.neo4j.ogm.session.request.strategy.impl.SchemaRelationshipLoadClauseBuilder;
import org.neo4j.ogm.session.transaction.DefaultTransactionManager;
import org.neo4j.ogm.session.transaction.support.TransactionalUnitOfWork;
import org.neo4j.ogm.session.transaction.support.TransactionalUnitOfWorkWithoutResult;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.utils.RelationshipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mihai Raulea
 * @author Mark Angrish
 */
public class Neo4jSession implements Session {

    private final Logger logger = LoggerFactory.getLogger(Neo4jSession.class);

    private final MetaData metaData;
    private final MappingContext mappingContext;
    private final DefaultTransactionManager txManager;

    private final LoadOneDelegate loadOneHandler = new LoadOneDelegate(this);
    private final LoadByTypeDelegate loadByTypeHandler = new LoadByTypeDelegate(this);
    private final LoadByIdsDelegate loadByIdsHandler = new LoadByIdsDelegate(this);
    private final LoadByInstancesDelegate loadByInstancesDelegate = new LoadByInstancesDelegate(this);
    private final SaveDelegate saveDelegate = new SaveDelegate(this);
    private final DeleteDelegate deleteDelegate = new DeleteDelegate(this);
    private final ExecuteQueriesDelegate executeQueriesDelegate = new ExecuteQueriesDelegate(this);
    private final GraphIdDelegate graphIdDelegate = new GraphIdDelegate(this);

    private LoadStrategy loadStrategy;
    private EntityInstantiator entityInstantiator;

    private Driver driver;
    private String bookmark;

    private List<EventListener> registeredEventListeners = new LinkedList<>();

    public Neo4jSession(MetaData metaData, Driver driver) {

        this.metaData = metaData;
        this.driver = driver;

        this.mappingContext = new MappingContext(metaData);
        this.txManager = new DefaultTransactionManager(this, driver);
        this.loadStrategy = LoadStrategy.PATH_LOAD_STRATEGY;
        this.entityInstantiator = new ReflectionEntityInstantiator(metaData);
    }

    public Neo4jSession(MetaData metaData, Driver driver, List<EventListener> eventListeners,
        LoadStrategy loadStrategy, EntityInstantiator entityInstantiator) {
        this(metaData, driver);
        registeredEventListeners.addAll(eventListeners);

        this.loadStrategy = loadStrategy;
        this.entityInstantiator = entityInstantiator;
    }

    @Override
    public EventListener register(EventListener eventListener) {
        registeredEventListeners.add(eventListener);
        return eventListener;
    }

    @Override
    public void notifyListeners(Event event) {
        for (EventListener eventListener : registeredEventListeners) {
            switch (event.getLifeCycle()) {
                case PRE_SAVE:
                    eventListener.onPreSave(event);
                    break;
                case POST_SAVE:
                    eventListener.onPostSave(event);
                    break;
                case PRE_DELETE:
                    eventListener.onPreDelete(event);
                    break;
                case POST_DELETE:
                    eventListener.onPostDelete(event);
                    break;
                default:
                    logger.warn("Event not recognised: {}", event);
            }
        }
    }

    @Override
    public boolean eventsEnabled() {
        return registeredEventListeners.size() > 0;
    }

    @Override
    public boolean dispose(EventListener eventListener) {

        for (EventListener next : registeredEventListeners) {
            if (eventListener == next) {
                registeredEventListeners.remove(eventListener);
                return true;
            }
        }
        return false;
    }

    /*
     *----------------------------------------------------------------------------------------------------------
     * loadOneHandler
     *----------------------------------------------------------------------------------------------------------
     */
    @Override
    public <T, ID extends Serializable> T load(Class<T> type, ID id) {
        return loadOneHandler.load(type, id);
    }

    @Override
    public <T, ID extends Serializable> T load(Class<T> type, ID id, int depth) {
        return loadOneHandler.load(type, id, depth);
    }

    /*
     *----------------------------------------------------------------------------------------------------------
     * loadByTypeHandler
     *----------------------------------------------------------------------------------------------------------
     */
    @Override
    public <T> Collection<T> loadAll(Class<T> type) {
        return loadByTypeHandler.loadAll(type);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, int depth) {
        return loadByTypeHandler.loadAll(type, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Pagination paging) {
        return loadByTypeHandler.loadAll(type, paging);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Pagination paging, int depth) {
        return loadByTypeHandler.loadAll(type, paging, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder) {
        return loadByTypeHandler.loadAll(type, sortOrder);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder, int depth) {
        return loadByTypeHandler.loadAll(type, sortOrder, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder, Pagination pagination) {
        return loadByTypeHandler.loadAll(type, sortOrder, pagination);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, SortOrder sortOrder, Pagination pagination, int depth) {
        return loadByTypeHandler.loadAll(type, sortOrder, pagination, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filter filter) {
        return loadByTypeHandler.loadAll(type, filter);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filter filter, int depth) {
        return loadByTypeHandler.loadAll(type, filter, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filter filter, SortOrder sortOrder) {
        return loadByTypeHandler.loadAll(type, filter, sortOrder);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filter filter, SortOrder sortOrder, int depth) {
        return loadByTypeHandler.loadAll(type, filter, sortOrder, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filter filter, Pagination pagination) {
        return loadByTypeHandler.loadAll(type, filter, pagination);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filter filter, Pagination pagination, int depth) {
        return loadByTypeHandler.loadAll(type, filter, pagination, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filter filter, SortOrder sortOrder, Pagination pagination) {
        return loadByTypeHandler.loadAll(type, filter, sortOrder, pagination);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filter filter, SortOrder sortOrder, Pagination pagination,
        int depth) {
        return loadByTypeHandler.loadAll(type, filter, sortOrder, pagination, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filters filters) {
        return loadByTypeHandler.loadAll(type, filters);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filters filters, int depth) {
        return loadByTypeHandler.loadAll(type, filters, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filters filters, SortOrder sortOrder) {
        return loadByTypeHandler.loadAll(type, filters, sortOrder);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filters filters, SortOrder sortOrder, int depth) {
        return loadByTypeHandler.loadAll(type, filters, sortOrder, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filters filters, Pagination pagination) {
        return loadByTypeHandler.loadAll(type, filters, pagination);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filters filters, Pagination pagination, int depth) {
        return loadByTypeHandler.loadAll(type, filters, pagination, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filters filters, SortOrder sortOrder, Pagination pagination) {
        return loadByTypeHandler.loadAll(type, filters, sortOrder, pagination);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Filters filters, SortOrder sortOrder, Pagination pagination,
        int depth) {
        return loadByTypeHandler.loadAll(type, filters, sortOrder, pagination, depth);
    }

    /*
     *----------------------------------------------------------------------------------------------------------
     * loadByIdsHandler (no filters yet)
     *----------------------------------------------------------------------------------------------------------
     */
    @Override
    public <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids) {
        return loadByIdsHandler.loadAll(type, ids);
    }

    @Override
    public <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, int depth) {
        return loadByIdsHandler.loadAll(type, ids, depth);
    }

    @Override
    public <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, SortOrder sortOrder) {
        return loadByIdsHandler.loadAll(type, ids, sortOrder);
    }

    @Override
    public <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, SortOrder sortOrder,
        int depth) {
        return loadByIdsHandler.loadAll(type, ids, sortOrder, depth);
    }

    @Override
    public <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, Pagination paging) {
        return loadByIdsHandler.loadAll(type, ids, paging);
    }

    @Override
    public <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, Pagination paging,
        int depth) {
        return loadByIdsHandler.loadAll(type, ids, paging, depth);
    }

    @Override
    public <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, SortOrder sortOrder,
        Pagination pagination) {
        return loadByIdsHandler.loadAll(type, ids, sortOrder, pagination);
    }

    @Override
    public <T, ID extends Serializable> Collection<T> loadAll(Class<T> type, Collection<ID> ids, SortOrder sortOrder,
        Pagination pagination, int depth) {
        return loadByIdsHandler.loadAll(type, ids, sortOrder, pagination, depth);
    }

    /*
     *----------------------------------------------------------------------------------------------------------
     * LoadByInstances (no filters yet)
     *----------------------------------------------------------------------------------------------------------
     */
    @Override
    public <T> Collection<T> loadAll(Collection<T> objects) {
        return loadByInstancesDelegate.loadAll(objects, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Collection<T> objects, int depth) {
        return loadByInstancesDelegate.loadAll(objects, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder) {
        return loadByInstancesDelegate.loadAll(objects, sortOrder);
    }

    @Override
    public <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder, int depth) {
        return loadByInstancesDelegate.loadAll(objects, sortOrder, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Collection<T> objects, Pagination pagination) {
        return loadByInstancesDelegate.loadAll(objects, pagination);
    }

    @Override
    public <T> Collection<T> loadAll(Collection<T> objects, Pagination pagination, int depth) {
        return loadByInstancesDelegate.loadAll(objects, pagination, depth);
    }

    @Override
    public <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder, Pagination pagination) {
        return loadByInstancesDelegate.loadAll(objects, sortOrder, pagination);
    }

    @Override
    public <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder, Pagination pagination, int depth) {
        return loadByInstancesDelegate.loadAll(objects, sortOrder, pagination, depth);
    }

    /*
    *----------------------------------------------------------------------------------------------------------
    * ExecuteQueriesDelegate
    *----------------------------------------------------------------------------------------------------------
    */
    @Override
    public <T> T queryForObject(Class<T> type, String cypher, Map<String, ?> parameters) {
        return executeQueriesDelegate.queryForObject(type, cypher, parameters);
    }

    @Override
    public <T> Iterable<T> query(Class<T> type, String cypher, Map<String, ?> parameters) {
        return executeQueriesDelegate.query(type, cypher, parameters);
    }

    @Override
    public Result query(String cypher, Map<String, ?> parameters) {
        return executeQueriesDelegate.query(cypher, parameters);
    }

    @Override
    public Result query(String cypher, Map<String, ?> parameters, boolean readOnly) {
        return executeQueriesDelegate.query(cypher, parameters, readOnly);
    }

    @Override
    public long countEntitiesOfType(Class<?> entity) {
        return executeQueriesDelegate.countEntitiesOfType(entity);
    }

    @Override
    public long count(Class<?> clazz, Iterable<Filter> filters) {
        return executeQueriesDelegate.count(clazz, filters);
    }

    /*
    *----------------------------------------------------------------------------------------------------------
    * DeleteDelegate
    *----------------------------------------------------------------------------------------------------------
    */
    @Override
    public void purgeDatabase() {
        deleteDelegate.purgeDatabase();
    }

    @Override
    public <T> void delete(T object) {
        deleteDelegate.delete(object);
    }

    @Override
    public <T> void deleteAll(Class<T> type) {
        deleteDelegate.deleteAll(type);
    }

    @Override
    public <T> Object delete(Class<T> type, Iterable<Filter> filters, boolean listResults) {
        return deleteDelegate.delete(type, filters, listResults);
    }

    /*
    *----------------------------------------------------------------------------------------------------------
    * SaveDelegate
    *----------------------------------------------------------------------------------------------------------
    */
    @Override
    public <T> void save(T object) {
        saveDelegate.save(object);
    }

    @Override
    public <T> void save(T object, int depth) {
        saveDelegate.save(object, depth);
    }

    /*
    *----------------------------------------------------------------------------------------------------------
    * TransactionsDelegate
    *----------------------------------------------------------------------------------------------------------
    */
    @Override
    public Transaction beginTransaction() {
        return txManager.openTransaction();
    }

    @Override
    public Transaction beginTransaction(Transaction.Type type) {
        return txManager.openTransaction(type, emptySet());
    }

    @Override
    public Transaction beginTransaction(Transaction.Type type, Iterable<String> bookmarks) {
        return txManager.openTransaction(type, bookmarks);
    }

    @Override
    @Deprecated
    public <T> T doInTransaction(GraphCallback<T> graphCallback) {
        return graphCallback.apply(requestHandler(), getTransaction(), metaData);
    }

    /**
     * @see Neo4jSession#doInTransaction(TransactionalUnitOfWork, org.neo4j.ogm.transaction.Transaction.Type)
     * @param function The code to execute.
     * @param txType Transaction type, readonly or not.
     */
    public void doInTransaction(TransactionalUnitOfWorkWithoutResult function, Transaction.Type txType) {
        doInTransaction( () -> {
            function.doInTransaction();
            return null;
        }, txType);
    }

    public void doInTransaction(TransactionalUnitOfWorkWithoutResult function, boolean forceTx, Transaction.Type txType) {
        doInTransaction( () -> {
            function.doInTransaction();
            return null;
        }, forceTx, txType);
    }

    public <T> T doInTransaction(TransactionalUnitOfWork<T> function, Transaction.Type txType) {
        return doInTransaction(function, false, txType);
    }

    /**
     * For internal use only. Opens a new transaction if necessary before running statements
     * in case an explicit transaction does not exist. It is designed to be the central point
     * for handling exceptions coming from the DB and apply commit / rollback rules.
     * @param function The callback to execute.
     * @param <T> The result type.
     * @param txType Transaction type, readonly or not.
     * @return The result of the transaction function.
     */
    public <T> T doInTransaction(TransactionalUnitOfWork<T> function, boolean forceTx, Transaction.Type txType) {

        Transaction transaction = txManager.getCurrentTransaction();
        if (!forceTx && (!driver.requiresTransaction() || transaction != null)) {
            return function.doInTransaction();
        }

        transaction = beginTransaction(txType);
        try {
            T result = function.doInTransaction();
            if (transactionManager().canCommit()) {
                transaction.commit();
            }
            return result;
        } catch (CypherException e) {
            logger.warn("Error executing query : {} - {}. Rolling back transaction.", e.getCode(), e.getDescription());
            if (transactionManager().canRollback()) {
                transaction.rollback();
            }
            throw e;
        } catch (Throwable e) {
            logger.warn("Error executing query : {}. Rolling back transaction.", e.getMessage());
            if(transactionManager().canRollback()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            if (!transaction.status().equals(Transaction.Status.CLOSED)) {
                transaction.close();
            }
        }
    }

    @Override
    public Transaction getTransaction() {
        return txManager.getCurrentTransaction();
    }

    /*
     *----------------------------------------------------------------------------------------------------------
     * GraphIdDelegate
     *----------------------------------------------------------------------------------------------------------
    */
    @Override
    public Long resolveGraphIdFor(Object possibleEntity) {
        return graphIdDelegate.resolveGraphIdFor(possibleEntity);
    }

    @Override
    public boolean detachNodeEntity(Long id) {
        return graphIdDelegate.detachNodeEntity(id);
    }

    @Override
    public boolean detachRelationshipEntity(Long id) {
        return graphIdDelegate.detachRelationshipEntity(id);
    }

    //
    // These helper methods for the delegates are deliberately NOT defined on the Session interface
    //
    public <T, ID extends Serializable> QueryStatements<ID> queryStatementsFor(Class<T> type, int depth) {
        final FieldInfo fieldInfo = metaData.classInfo(type.getName()).primaryIndexField();
        String primaryIdName = fieldInfo != null ? fieldInfo.property() : null;
        if (metaData.isRelationshipEntity(type.getName())) {
            return new RelationshipQueryStatements<>(primaryIdName, loadRelationshipClauseBuilder(depth));
        } else {
            return new NodeQueryStatements<>(primaryIdName, loadNodeClauseBuilder(depth));
        }
    }

    public String entityType(String name) {
        return metaData.entityType(name);
    }

    public MappingContext context() {
        return mappingContext;
    }

    public OptimisticLockingChecker optimisticLockingChecker() {
        return new OptimisticLockingChecker(this);
    }

    public MetaData metaData() {
        return metaData;
    }

    @Override
    public void clear() {
        mappingContext.clear();
    }

    @Deprecated
    // TODO: Replace calls to this with a mockito test instead.
    // inject a custom driver
    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Request requestHandler() {
        return driver.request();
    }

    public DefaultTransactionManager transactionManager() {
        return txManager;
    }

    public void warn(String msg) {
        logger.warn("Thread {}: {}", Thread.currentThread().getId(), msg);
    }

    public void debug(String msg) {
        logger.debug("Thread {}: {}", Thread.currentThread().getId(), msg);
    }

    public void resolvePropertyAnnotations(Class entityType, Iterable<Filter> filters) {
        for (Filter filter : filters) {
            if (filter.getOwnerEntityType() == null) {
                filter.setOwnerEntityType(entityType);
            }
            String propertyName = resolvePropertyName(filter.getOwnerEntityType(), filter.getPropertyName());
            Filter.setNameFromProperty(filter, propertyName);

            ClassInfo classInfo = metaData().classInfo(entityType.getName());
            FieldInfo fieldInfo = classInfo.fieldsInfo().get(filter.getPropertyName());
            if (fieldInfo != null) {
                filter.setPropertyConverter(fieldInfo.getPropertyConverter());
                filter.setCompositeConverter(fieldInfo.getCompositeConverter());
            }

            if (filter.isNested()) {
                resolveRelationshipType(filter);
                ClassInfo nestedClassInfo = metaData().classInfo(filter.getNestedPropertyType().getName());
                filter.setNestedEntityTypeLabel(entityType(nestedClassInfo.name()));
                if (metaData().isRelationshipEntity(nestedClassInfo.name())) {
                    filter.setNestedRelationshipEntity(true);
                }
            }
        }
    }

    public void resolvePropertyAnnotations(Class entityType, SortOrder sortOrder) {
        final String escapedProperty = "`%s`";

        if (sortOrder != null) {
            for (SortClause sortClause : sortOrder.sortClauses()) {
                for (int i = 0; i < sortClause.getProperties().length; i++) {
                    sortClause.getProperties()[i] = String
                        .format(escapedProperty, resolvePropertyName(entityType, sortClause.getProperties()[i]));
                }
            }
        }
    }

    private String resolvePropertyName(Class entityType, String propertyName) {
        ClassInfo classInfo = metaData().classInfo(entityType.getName());
        FieldInfo fieldInfo = classInfo.propertyFieldByName(propertyName);
        if (fieldInfo != null && fieldInfo.getAnnotations() != null) {
            AnnotationInfo annotation = fieldInfo.getAnnotations().get(Property.class);
            if (annotation != null) {
                return annotation.get(Property.NAME, propertyName);
            }
        }
        //session.metaData().classInfo(entityType.getName()).propertyFieldByName(propertyName).property();
        return propertyName;
    }

    private void resolveRelationshipType(Filter parameter) {
        ClassInfo classInfo = metaData().classInfo(parameter.getOwnerEntityType().getName());
        FieldInfo fieldInfo = classInfo.relationshipFieldByName(parameter.getNestedPropertyName());

        String defaultRelationshipType = RelationshipUtils.inferRelationshipType(parameter.getNestedPropertyName());
        parameter.setRelationshipType(defaultRelationshipType);
        parameter.setRelationshipDirection(Relationship.UNDIRECTED);
        if (fieldInfo.getAnnotations() != null) {
            AnnotationInfo annotation = fieldInfo.getAnnotations().get(Relationship.class);
            if (annotation != null) {
                parameter.setRelationshipType(annotation.get(Relationship.TYPE, defaultRelationshipType));
                parameter.setRelationshipDirection(annotation.get(Relationship.DIRECTION, Relationship.UNDIRECTED));
            }
            if (fieldInfo.getAnnotations().get(StartNode.class) != null) {
                parameter.setRelationshipDirection(Relationship.OUTGOING);
            }
            if (fieldInfo.getAnnotations().get(EndNode.class) != null) {
                parameter.setRelationshipDirection(Relationship.INCOMING);
            }
        }
    }

    @Override
    public String getLastBookmark() {
        return bookmark;
    }

    @Override
    public void withBookmark(String bookmark) {
        this.bookmark = bookmark;
    }

    @Override
    public LoadStrategy getLoadStrategy() {
        return loadStrategy;
    }

    public EntityInstantiator getEntityInstantiator() {
        return entityInstantiator;
    }

    @Override
    public void setLoadStrategy(LoadStrategy loadStrategy) {
        this.loadStrategy = loadStrategy;
    }

    private LoadClauseBuilder loadNodeClauseBuilder(int depth) {
        if (depth < 0) {
            return new PathNodeLoadClauseBuilder();
        }

        switch (loadStrategy) {
            case PATH_LOAD_STRATEGY:
                return new PathNodeLoadClauseBuilder();

            case SCHEMA_LOAD_STRATEGY:
                return new SchemaNodeLoadClauseBuilder(metaData.getSchema());

            default:
                throw new IllegalStateException("Unknown loadStrategy " + loadStrategy);
        }
    }

    private LoadClauseBuilder loadRelationshipClauseBuilder(int depth) {
        if (depth < 0) {
            throw new IllegalArgumentException("Can't load unlimited depth for relationships");
        }

        switch (loadStrategy) {
            case PATH_LOAD_STRATEGY:
                return new PathRelationshipLoadClauseBuilder();

            case SCHEMA_LOAD_STRATEGY:
                return new SchemaRelationshipLoadClauseBuilder(metaData.getSchema());

            default:
                throw new IllegalStateException("Unknown loadStrategy " + loadStrategy);
        }
    }
}
