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
package org.neo4j.ogm.session.delegates;

import java.lang.reflect.Field;
import java.util.*;

import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.query.CypherQuery;
import org.neo4j.ogm.cypher.query.DefaultRowModelRequest;
import org.neo4j.ogm.metadata.reflect.FieldAccessor;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.request.RowModelRequest;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.event.Event;
import org.neo4j.ogm.session.event.PersistenceEvent;
import org.neo4j.ogm.session.request.strategy.DeleteStatements;
import org.neo4j.ogm.session.request.strategy.impl.NodeDeleteStatements;
import org.neo4j.ogm.session.request.strategy.impl.RelationshipDeleteStatements;

/**
 * @author Vince Bickers
 */
public class DeleteDelegate  {

    private final Neo4jSession session;

    public DeleteDelegate(Neo4jSession neo4jSession) {
        this.session = neo4jSession;
    }

    private DeleteStatements getDeleteStatementsBasedOnType(Class type) {
        if (session.metaData().isRelationshipEntity(type.getName())) {
            return new RelationshipDeleteStatements();
        }
        return new NodeDeleteStatements();
    }


    private <T> void deleteAll(T object) {
        List<T> list;
        if (object.getClass().isArray()) {
            list = Collections.singletonList(object);
        } else {
            list = (List<T>) object;
        }

        if (!list.isEmpty()) {
            Set<Object> allNeighbours = new HashSet<>();
            for (T element : list) {
                allNeighbours.addAll(session.context().neighbours(element));
            }
            deleteOneOrMoreObjects(allNeighbours, list);
        }
    }

    public <T> void delete(T object) {
        if (object.getClass().isArray() || Iterable.class.isAssignableFrom(object.getClass())) {
            deleteAll(object);
        } else {
            deleteOneOrMoreObjects(session.context().neighbours(object), Collections.singletonList(object));
        }
    }

    // TODO : this is being done in multiple requests at the moment, one per object. Why not put them in a single request?
    private void deleteOneOrMoreObjects(Set<Object> neighbours, List<?> objects) {

        Set<Object> notified = new HashSet();

        if (session.eventsEnabled()) {
            for (Object affectedObject : neighbours) {
                if (!notified.contains(affectedObject)) {
                    session.notifyListeners(new PersistenceEvent(affectedObject, Event.TYPE.PRE_SAVE));
                    notified.add(affectedObject);
                }
            }
        }

        for (Object object : objects ) {

            ClassInfo classInfo = session.metaData().classInfo(object);

            if (classInfo != null) {

                Field identityField = classInfo.getField(classInfo.identityField());
                Long identity = (Long) FieldAccessor.read(identityField, object);
                if (identity != null) {
                    Statement request = getDeleteStatementsBasedOnType(object.getClass()).delete(identity);
                    if (session.eventsEnabled()) {
                        if (!notified.contains(object)) {
                            session.notifyListeners(new PersistenceEvent(object, Event.TYPE.PRE_DELETE));
                            notified.add(object);
                        }
                    }
                    RowModelRequest query = new DefaultRowModelRequest(request.getStatement(), request.getParameters());
                    try (Response<RowModel> response = session.requestHandler().execute(query)) {
                        if (session.metaData().isRelationshipEntity(classInfo.name())) {
                            session.detachRelationshipEntity(identity);
                        } else {
                            session.detachNodeEntity(identity);
                        }
                        if (session.eventsEnabled()) {
                            if (notified.contains(object)) {
                               session.notifyListeners(new PersistenceEvent(object, Event.TYPE.POST_DELETE));
                            }
                        }
                    }
                }
            } else {
                session.warn(object.getClass().getName() + " is not an instance of a persistable class");
            }
        }

        if (session.eventsEnabled()) {
            for (Object affectedObject : neighbours) {
                if (notified.contains(affectedObject)) {
                    session.notifyListeners(new PersistenceEvent(affectedObject, Event.TYPE.POST_SAVE));
                }
            }
        }

    }

    public <T> void deleteAll(Class<T> type) {
        ClassInfo classInfo = session.metaData().classInfo(type.getName());
        if (classInfo != null) {
            Statement request = getDeleteStatementsBasedOnType(type).delete(session.entityType(classInfo.name()));
            RowModelRequest query = new DefaultRowModelRequest(request.getStatement(), request.getParameters());
            session.notifyListeners(new PersistenceEvent(type, Event.TYPE.PRE_DELETE));
            try (Response<RowModel> response = session.requestHandler().execute(query)) {
                session.context().removeType(type);
                if (session.eventsEnabled()) {
                    session.notifyListeners(new PersistenceEvent(type, Event.TYPE.POST_DELETE));
                }
            }
        } else {
            session.warn(type.getName() + " is not a persistable class");
        }
    }

    public <T> Object delete(Class<T> clazz, Iterable<Filter> filters, boolean listResults) {

        ClassInfo classInfo = session.metaData().classInfo(clazz.getSimpleName());

        if (classInfo != null) {

            session.resolvePropertyAnnotations(clazz, filters);

            CypherQuery query;

            if (classInfo.isRelationshipEntity()) {
                query = new RelationshipDeleteStatements().deleteAndList(classInfo.neo4jName(), filters);
            } else {
                query = new NodeDeleteStatements().deleteAndList(classInfo.neo4jName(), filters);
            }

            if (listResults) {
                return list(query, classInfo.isRelationshipEntity());
            }
            return count(query, classInfo.isRelationshipEntity());
        }

        throw new RuntimeException(clazz.getName() + " is not a persistable class");
    }

    /**
     * Executes a delete query in which objects of a specific type will be deleted according to some filter criteria,
     * invoking post-delete housekeeping after the query completes, and returning a list of deleted objects to the caller.
     *
     * @param query the CypherQuery that will delete objects according to some filter criteria
     * @param isRelationshipEntity whether the objects being deleted are relationship entities
     * @return a {@link List} of object ids that were deleted
     */
    private List<Long> list(CypherQuery query, boolean isRelationshipEntity) {
        String resultKey = isRelationshipEntity ? "ID(r0)" : "ID(n)";
        Result result = session.query(query.getStatement(), query.getParameters());
        List<Long> ids = new ArrayList();
        for (Map<String, Object> resultEntry : result) {
            Long deletedObjectId = Long.parseLong(resultEntry.get(resultKey).toString());
            postDelete(deletedObjectId, isRelationshipEntity);
            ids.add(deletedObjectId);
        }
        return ids;
    }

    /**
     * Executes a delete query in which objects of a specific type will be deleted according to some filter criteria,
     * invoking post-delete housekeeping after the query completes, and returning a count of deleted objects to the caller.
     *
     * @param query the CypherQuery that will delete objects according to some filter criteria
     * @param isRelationshipEntity whether the objects being deleted are relationship entities
     * @return a count of objects that were deleted
     */
    private Long count(CypherQuery query, boolean isRelationshipEntity) {
        String resultKey = isRelationshipEntity ? "ID(r0)" : "ID(n)";
        Result result = session.query(query.getStatement(), query.getParameters());
        long count = 0;
        for (Map<String, Object> resultEntry : result) {
            Long deletedObjectId = Long.parseLong(resultEntry.get(resultKey).toString());
            postDelete(deletedObjectId, isRelationshipEntity);
            count++;
        }
        return count;
    }

    /**
     * Handles the post-delete phase of a delete query in which objects have been
     * deleted according to some filter criteria.
     *
     * In this scenario, we don't necessarily have references to all the deleted
     * objects in the mapping context, so we can only handle the ones we know about.
     *
     * The two tasks to be done here are to remove the object from the mapping
     * context if we're holding a reference to it, and to raise a POST_DELETE event
     * for every such object found.
     *
     * Note that is not possible to raise a PRE_DELETE event because we don't
     * know beforehand which objects will be deleted by the query.
     *
     * @param identity the id of the object that was deleted
     * @param isRelationshipEntity true if it was an edge that was deleted
     */
    private void postDelete(Long identity, boolean isRelationshipEntity) {

        Object object;

        if (isRelationshipEntity) {
            object = session.context().getRelationshipEntity(identity);
            if (object != null) {
                session.detachRelationshipEntity(identity);
            }
        } else {
            object = session.context().getNodeEntity(identity);
            if (object != null) {
                session.detachNodeEntity(identity);
            }
        }
        if (session.eventsEnabled() && object != null) {
            session.notifyListeners(new PersistenceEvent(object, Event.TYPE.POST_DELETE));
        }
    }

    public void purgeDatabase() {
        Statement stmt = new NodeDeleteStatements().deleteAll();
        RowModelRequest query = new DefaultRowModelRequest(stmt.getStatement(), stmt.getParameters());
        session.requestHandler().execute(query).close();
        session.context().clear();
    }


    public void clear() {
        session.context().clear();
    }
}
