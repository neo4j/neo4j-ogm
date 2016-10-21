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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.neo4j.ogm.cypher.query.DefaultRowModelRequest;
import org.neo4j.ogm.entity.io.FieldWriter;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.request.RowModelRequest;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.Capability;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.event.Event;
import org.neo4j.ogm.session.event.PersistenceEvent;
import org.neo4j.ogm.session.request.strategy.impl.NodeDeleteStatements;
import org.neo4j.ogm.session.request.strategy.impl.RelationshipDeleteStatements;
import org.neo4j.ogm.session.request.strategy.DeleteStatements;

/**
 * @author Vince Bickers
 */
public class DeleteDelegate implements Capability.Delete {

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

    @Override
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
                Long identity = (Long) FieldWriter.read(identityField, object);
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

    @Override
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


    @Override
    public void purgeDatabase() {
        Statement stmt = new NodeDeleteStatements().deleteAll();
        RowModelRequest query = new DefaultRowModelRequest(stmt.getStatement(), stmt.getParameters());
        session.requestHandler().execute(query).close();
        session.context().clear();
    }


    @Override
    public void clear() {
        session.context().clear();
    }
}
