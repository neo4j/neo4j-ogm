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

import java.lang.reflect.Array;
import java.util.*;

import org.neo4j.ogm.compiler.CompileContext;
import org.neo4j.ogm.context.EntityGraphMapper;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.session.Capability;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.event.SaveEvent;
import org.neo4j.ogm.session.request.RequestExecutor;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class SaveDelegate implements Capability.Save {

    private final Neo4jSession session;
    private final RequestExecutor requestExecutor;

    public SaveDelegate(Neo4jSession neo4jSession) {
        this.session = neo4jSession;
        requestExecutor = new RequestExecutor(neo4jSession);
    }

    @Override
    public <T> void save(T object) {
        save(object, -1); // default : full tree of changed objects
    }

    @Override
    public <T> void save(T object, int depth) {
        if (object.getClass().isArray() || Iterable.class.isAssignableFrom(object.getClass())) {
            Collection<T> objects;
            if (object.getClass().isArray()) {
                int length = Array.getLength(object);
                objects = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    T arrayElement = (T) Array.get(object, i);
                    objects.add(arrayElement);
                }
            } else {
                objects = (Collection<T>) object;
            }
            List<CompileContext> contexts = new ArrayList<>();
            for (Object element : objects) {
                contexts.add(new EntityGraphMapper(session.metaData(), session.context()).map(element, depth));
            }
            notifySave(contexts, SaveEvent.PRE);
            requestExecutor.executeSave(contexts);
            notifySave(contexts, SaveEvent.POST);
        }
        else {
            ClassInfo classInfo = session.metaData().classInfo(object);
            if (classInfo != null) {
                CompileContext context = new EntityGraphMapper(session.metaData(), session.context()).map(object, depth);
                notifySave(context, SaveEvent.PRE);
                requestExecutor.executeSave(context);
                notifySave(context, SaveEvent.POST);
            } else {
                session.warn(object.getClass().getName() + " is not an instance of a persistable class");
            }
        }
    }


    private void notifySave(List<CompileContext> contexts, String lifecycle) {
        List<Object> affectedObjects = new LinkedList<>();
        Iterator<CompileContext> compileContextIterator = contexts.iterator();
        while(compileContextIterator.hasNext()) {
        CompileContext context = compileContextIterator.next();
        Iterator<Object> affectedObjectsIterator = context.registry().iterator();
        while(affectedObjectsIterator.hasNext()) {
            // should i do something, if it is a TransientRelationship ?
            Object affectedObject = affectedObjectsIterator.next();
            ClassInfo classInfo = this.session.metaData().classInfo(affectedObject) ;//metaData.classInfo(entity);
            if(!affectedObjects.contains(affectedObject)) affectedObjects.add(affectedObject);
        }
        }
        for(Object object : affectedObjects) {
            SaveEvent saveEvent = new SaveEvent();
            saveEvent.LIFECYCLE = lifecycle;
            // should i do something, if it is a TransientRelationship ?
            saveEvent.affectedObject = object;
            session.notifyListeners(saveEvent);
        }
    }

    private void notifySave(CompileContext context, String lifecycle) {
        Iterator<Object> affectedObjectsIterator = context.registry().iterator();
        while(affectedObjectsIterator.hasNext()) {
            SaveEvent saveEvent = new SaveEvent();
            saveEvent.LIFECYCLE = lifecycle;
            // should i do something, if it is a TransientRelationship ?
            Object affectedObject = affectedObjectsIterator.next();
            ClassInfo classInfo = this.session.metaData().classInfo(affectedObject) ;//metaData.classInfo(entity);
            // TransientRelationship does not have ClassInfo
            saveEvent.affectedObject = affectedObject;
            session.notifyListeners(saveEvent);
        }
    }

}
