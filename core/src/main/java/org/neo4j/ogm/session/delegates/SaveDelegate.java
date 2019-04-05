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
package org.neo4j.ogm.session.delegates;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.neo4j.ogm.context.EntityGraphMapper;
import org.neo4j.ogm.context.WriteProtectionTarget;
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

        EntityGraphMapper entityGraphMapper = new EntityGraphMapper(
            session.metaData(),
            session.context(),
            session.isUpdateOtherSideOfRelationships());
        if (this.writeProtectionStrategy != null) {
            entityGraphMapper.addWriteProtection(this.writeProtectionStrategy.get());
        }

        Iterable<T> objects;
        if (object.getClass().isArray()) {
            int length = Array.getLength(object);
            List<T> copy = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                T arrayElement = (T) Array.get(object, i);
                copy.add(arrayElement);
            }
            objects = copy;
        } else if (Iterable.class.isAssignableFrom(object.getClass())) {
            objects = (Iterable<T>) object;
        } else if (session.metaData().classInfo(object) != null) {
            objects = Collections.singletonList(object);
        } else {
            throw new IllegalArgumentException("Class " + object.getClass() + " is not a valid entity class. "
                + "Please check the entity mapping.");
        }

        if (session.eventsEnabled()) {
            objects.forEach(item -> {
                eventsDelegate.preSave(item);
                entityGraphMapper.map(item, depth);
            });
            requestExecutor.executeSave(entityGraphMapper.compileContext());
            eventsDelegate.postSave();
        } else {
            objects.forEach(item -> entityGraphMapper.map(item, depth));
            requestExecutor.executeSave(entityGraphMapper.compileContext());
        }
    }

    public void addWriteProtection(WriteProtectionTarget target, Predicate<Object> protection) {
        if (this.writeProtectionStrategy == null) {
            this.writeProtectionStrategy = new DefaultWriteProtectionStrategyImpl();
        } else if (!(this.writeProtectionStrategy instanceof DefaultWriteProtectionStrategyImpl)) {
            throw new IllegalStateException(
                "Cannot register simple write protection for a mode on a custom strategy. Use #setWriteProtectionStrategy(null) to remove any custom strategy.");
        }

        ((DefaultWriteProtectionStrategyImpl) this.writeProtectionStrategy).addProtection(target, protection);
    }

    public void removeWriteProtection(WriteProtectionTarget target) {
        if (this.writeProtectionStrategy == null
            || !(this.writeProtectionStrategy instanceof DefaultWriteProtectionStrategyImpl)) {
            return;
        }

        final DefaultWriteProtectionStrategyImpl currentWriteProtectionStrategy = (DefaultWriteProtectionStrategyImpl) this.writeProtectionStrategy;
        currentWriteProtectionStrategy.removeProtection(target);
        if (currentWriteProtectionStrategy.isEmpty()) {
            this.writeProtectionStrategy = null;
        }
    }

    public void setWriteProtectionStrategy(WriteProtectionStrategy writeProtectionStrategy) {
        this.writeProtectionStrategy = writeProtectionStrategy;
    }
}
