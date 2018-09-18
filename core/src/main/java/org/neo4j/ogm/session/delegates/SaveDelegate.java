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
package org.neo4j.ogm.session.delegates;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.neo4j.ogm.context.EntityGraphMapper;
import org.neo4j.ogm.context.WriteProtectionTarget;
import org.neo4j.ogm.cypher.compiler.CompileContext;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.WriteProtectionStrategy;
import org.neo4j.ogm.session.request.RequestExecutor;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Michael J. Simons
 * @author Jared Hancock
 */
public class SaveDelegate extends SessionDelegate {

    private final RequestExecutor requestExecutor;

    private WriteProtectionStrategy writeProtectionStrategy;

    public SaveDelegate(Neo4jSession session) {
        super(session);
        requestExecutor = new RequestExecutor(session);
    }

    public <T> void save(T object) {
        save(object, -1); // default : full tree of changed objects
    }

    public <T> void save(T object, int depth) {

        SaveEventDelegate eventsDelegate = new SaveEventDelegate(session);

        EntityGraphMapper entityGraphMapper = new EntityGraphMapper(session.metaData(), session.context());
        if(this.writeProtectionStrategy != null) {
            entityGraphMapper.addWriteProtection(this.writeProtectionStrategy.get());
        }

        if (object.getClass().isArray() || Iterable.class.isAssignableFrom(object.getClass())) {
            Iterable<T> objects;
            if (object.getClass().isArray()) {
                int length = Array.getLength(object);
                List<T> copy = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    T arrayElement = (T) Array.get(object, i);
                    copy.add(arrayElement);
                }
                objects = copy;
            } else {
                objects = (Iterable<T>) object;
            }

            for (Object element : objects) {
                if (session.eventsEnabled()) {
                    eventsDelegate.preSave(object);
                }
                entityGraphMapper.map(element, depth);
            }
            requestExecutor.executeSave(entityGraphMapper.compileContext());
            if (session.eventsEnabled()) {
                eventsDelegate.postSave();
            }
        } else {
            ClassInfo classInfo = session.metaData().classInfo(object);
            if (classInfo != null) {

                if (session.eventsEnabled()) {
                    eventsDelegate.preSave(object);
                }

                CompileContext context = entityGraphMapper.map(object, depth);
                requestExecutor.executeSave(context);

                if (session.eventsEnabled()) {
                    eventsDelegate.postSave();
                }
            } else {
                throw new IllegalArgumentException("Class " + object.getClass() + " is not a valid entity class. "
                    + "Please check the entity mapping.");
            }
        }
    }

    public void addWriteProtection(WriteProtectionTarget target, Predicate<Object> protection) {
        if(this.writeProtectionStrategy == null) {
            this.writeProtectionStrategy = new DefaultWriteProtectionStrategyImpl();
        } else if(!(this.writeProtectionStrategy instanceof DefaultWriteProtectionStrategyImpl)) {
            throw new IllegalStateException("Cannot register simple write protection for a mode on a custom strategy. Use #setWriteProtectionStrategy(null) to remove any custom strategy.");
        }

        ((DefaultWriteProtectionStrategyImpl)this.writeProtectionStrategy).addProtection(target, protection);
    }

    public void removeWriteProtection(WriteProtectionTarget target) {
        if(this.writeProtectionStrategy == null || !(this.writeProtectionStrategy instanceof DefaultWriteProtectionStrategyImpl)) {
            return;
        }

        final DefaultWriteProtectionStrategyImpl writeProtectionStrategy = (DefaultWriteProtectionStrategyImpl) this.writeProtectionStrategy;
        writeProtectionStrategy.removeProtection(target);
        if(writeProtectionStrategy.isEmpty()) {
            this.writeProtectionStrategy = null;
        }
    }

    public void setWriteProtectionStrategy(WriteProtectionStrategy writeProtectionStrategy) {
        this.writeProtectionStrategy = writeProtectionStrategy;
    }
}
