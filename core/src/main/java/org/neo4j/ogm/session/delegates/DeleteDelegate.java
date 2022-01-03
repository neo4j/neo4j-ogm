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
package org.neo4j.ogm.session.delegates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.ogm.context.MappingContext;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.query.CypherQuery;
import org.neo4j.ogm.cypher.query.DefaultRowModelRequest;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.MetaData;
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
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author Vince Bickers
 * @author Michael J. Simons
 */
public class DeleteDelegate extends SessionDelegate {

    public DeleteDelegate(Neo4jSession session) {
        super(session);
    }

    public <T> void delete(T object) {
        List<T> objectsForDeletion = createObjectsCollectionFromObject(object);
        delete(objectsForDeletion);
    }

    /**
     * Deletes all nodes of a given type. They will get discovered by using the matching label for that type.
     * To avoid a delete of every node in the database the method will abort the delete operation if no label
     * can be determined.
     *
     * @param type  The type of the nodes/objects to be deleted.
     * @param <T>   The type to work with
     */
    public <T> void deleteAll(Class<T> type) {
        ClassInfo classInfo = session.metaData().classInfo(type.getName());
        if (classInfo != null) {
            String entityLabel = classInfo.neo4jName();
            if (entityLabel == null) {
                session.warn("Unable to find database label for entity " + type.getName()
                    + " : no results will be returned. Make sure the class is registered, "
                    + "and not abstract without @NodeEntity annotation");
                return;
            }
            Statement request = getDeleteStatementsBasedOnType(type).delete(entityLabel);
            RowModelRequest query = new DefaultRowModelRequest(request.getStatement(), request.getParameters());
            session.notifyListeners(new PersistenceEvent(type, Event.TYPE.PRE_DELETE));
            session.doInTransaction(() -> {
                try (Response<RowModel> response = session.requestHandler().execute(query)) {
                    session.context().removeType(type);
                    if (session.eventsEnabled()) {
                        session.notifyListeners(new PersistenceEvent(type, Event.TYPE.POST_DELETE));
                    }
                }
            }, Transaction.Type.READ_WRITE);
        } else {
            session.warn(type.getName() + " is not a persistable class");
        }
    }

    public <T> Object delete(Class<T> clazz, Iterable<Filter> filters, boolean listResults) {

        ClassInfo classInfo = session.metaData().classInfo(clazz.getSimpleName());

        if (classInfo != null) {

            resolvePropertyAnnotations(clazz, filters);

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

    public void purgeDatabase() {
        Statement stmt = new NodeDeleteStatements().deleteAll();
        RowModelRequest query = new DefaultRowModelRequest(stmt.getStatement(), stmt.getParameters());
        session.doInTransaction(() -> {
            session.requestHandler().execute(query).close();
        }, Transaction.Type.READ_WRITE);
        session.context().clear();
    }

    public void clear() {
        session.context().clear();
    }

    private <T> List<T> createObjectsCollectionFromObject(T object) {
        List<T> objectCollection;
        if (object.getClass().isArray()) {
            T[] objectsAsArray = (T[]) object;
            objectCollection = Arrays.asList(objectsAsArray);
        } else if (Iterable.class.isAssignableFrom(object.getClass())) {
            objectCollection = new ArrayList<>();
            ((Iterable<T>) object).forEach(objectCollection::add);
        } else {
            objectCollection = Collections.singletonList(object);
        }
        return objectCollection;
    }

    private <T> void delete(List<T> objectsForDeletion) {
        if (objectsForDeletion.isEmpty()) {
            return;
        }

        Set<Object> allNeighbours = new HashSet<>();
        for (T element : objectsForDeletion) {
            allNeighbours.addAll(session.context().neighbours(element));
        }
        deleteOneOrMoreObjects(objectsForDeletion, allNeighbours);
    }

    // TODO : this is being done in multiple requests at the moment, one per object. Why not put them in a single request?
    private void deleteOneOrMoreObjects(List<?> objects, Set<Object> neighbours) {

        Set<Object> notified = new HashSet<>();

        if (session.eventsEnabled()) {
            for (Object affectedObject : neighbours) {
                if (!notified.contains(affectedObject)) {
                    session.notifyListeners(new PersistenceEvent(affectedObject, Event.TYPE.PRE_SAVE));
                    notified.add(affectedObject);
                }
            }
        }

        for (Object object : objects) {

            MetaData metaData = session.metaData();
            ClassInfo classInfo = metaData.classInfo(object);

            if (classInfo == null) {
                session.warn(object.getClass().getName() + " is not an instance of a persistable class");
            } else {

                MappingContext mappingContext = session.context();
                Long id = mappingContext.optionalNativeId(object)
                    .filter(possibleId -> possibleId >= 0)
                    .orElseGet(() -> {
                        session.warn(String.format(
                                "Instance of class %s has to be reloaded to be deleted. This can happen if the session has " +
                                "been cleared between loading and deleting or using an object from a different transaction.",
                                object.getClass())
                        );
                        return classInfo.getPrimaryIndexOrIdReader().apply(object)
                            .map(primaryIndexOrId -> session.load(object.getClass(), (Serializable) primaryIndexOrId))
                            .flatMap(reloadedObject -> mappingContext.optionalNativeId(reloadedObject))
                            .orElse(-1L);
                    });
                if (id >= 0) {
                    Statement request = getDeleteStatement(object, id, classInfo);
                    if (session.eventsEnabled() && !notified.contains(object)) {
                        session.notifyListeners(new PersistenceEvent(object, Event.TYPE.PRE_DELETE));
                        notified.add(object);
                    }
                    RowModelRequest query = new DefaultRowModelRequest(request.getStatement(), request.getParameters());
                    session.doInTransaction(() -> {
                        try (Response<RowModel> response = session.requestHandler().execute(query)) {

                            if (request.optimisticLockingConfig().isPresent()) {
                                List<RowModel> rowModels = response.toList();
                                session.optimisticLockingChecker().checkResultsCount(rowModels, request);
                            }

                            if (metaData.isRelationshipEntity(classInfo.name())) {
                                session.detachRelationshipEntity(id);
                            } else {
                                session.detachNodeEntity(id);
                            }

                            // This will only be true if the events have been
                            // enabled in the first place.
                            if (notified.contains(object)) {
                                session.notifyListeners(new PersistenceEvent(object, Event.TYPE.POST_DELETE));
                            }
                        }
                    }, Transaction.Type.READ_WRITE);
                }
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

    private DeleteStatements getDeleteStatementsBasedOnType(Class type) {
        if (session.metaData().isRelationshipEntity(type.getName())) {
            return new RelationshipDeleteStatements();
        }
        return new NodeDeleteStatements();
    }

    private Statement getDeleteStatement(Object object, Long identity, ClassInfo classInfo) {
        DeleteStatements deleteStatements = getDeleteStatementsBasedOnType(object.getClass());

        Statement request;
        if (classInfo.hasVersionField()) {
            request = deleteStatements.delete(identity, object, classInfo);
        } else {
            request = deleteStatements.delete(identity);
        }
        return request;
    }

    /**
     * Executes a delete query in which objects of a specific type will be deleted according to some filter criteria,
     * invoking post-delete housekeeping after the query completes, and returning a list of deleted objects to the caller.
     *
     * @param query                the CypherQuery that will delete objects according to some filter criteria
     * @param isRelationshipEntity whether the objects being deleted are relationship entities
     * @return a {@link List} of object ids that were deleted
     */
    private List<Long> list(CypherQuery query, boolean isRelationshipEntity) {
        String resultKey = isRelationshipEntity ? "ID(r0)" : "ID(n)";
        Result result = session.query(query.getStatement(), query.getParameters());
        List<Long> ids = new ArrayList<>();
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
     * @param query                the CypherQuery that will delete objects according to some filter criteria
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
     * In this scenario, we don't necessarily have references to all the deleted
     * objects in the mapping context, so we can only handle the ones we know about.
     * The two tasks to be done here are to remove the object from the mapping
     * context if we're holding a reference to it, and to raise a POST_DELETE event
     * for every such object found.
     * Note that is not possible to raise a PRE_DELETE event because we don't
     * know beforehand which objects will be deleted by the query.
     *
     * @param identity             the id of the object that was deleted
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
}
