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
package org.neo4j.ogm.lazyloading;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.session.Session;

/**
 * @author Andreas Berger
 */
public class LazyInitializer extends BaseLazyLoader {
    private final Object entity;

    private final ClassInfo classInfo;
    private Set<String> initializedFields;

    public LazyInitializer(Object entity, long id, Session session, ClassInfo classInfo) {
        super(id, session);
        this.entity = entity;
        this.classInfo = classInfo;
    }

    public void read(String fieldName) {
        operateOnField(fieldName, fieldInfo -> {
            if (fieldInfo.isIterable()) {
                return;
            }
            Collection<?> result = readRelation(fieldInfo);
            if (result.isEmpty()) {
                return;
            }
            if (result.size() > 1) {
                throw new RuntimeException("Result not of expected size. Expected 1 row but found " + result.size());
            } else {
                fieldInfo.write(entity, result.stream().findFirst().orElse(null));
            }
        });
    }

    public void write(String fieldName, Object value) {
        operateOnField(fieldName, fieldInfo -> {
            // workaround to have the relationship managed by this session
            if (fieldInfo.isIterable()) {
                Object currentValue = fieldInfo.read(entity);
                if (currentValue instanceof LazyCollection<?, ?>) {
                    ((LazyCollection<?, ?>) currentValue).init();
                }
            } else {
                readRelation(fieldInfo);
            }
        });
    }

    public void reset() {
        if (initializedFields != null) {
            initializedFields.clear();
        }
    }

    private void operateOnField(String fieldName, Consumer<FieldInfo> action) {
        if (initializedFields == null) {
            initializedFields = new HashSet<>();
        }
        if (initializedFields.contains(fieldName)) {
            return;
        }
        FieldInfo fieldInfo = classInfo.relationshipFieldByName(fieldName);
        if (fieldInfo == null) {
            throw new IllegalArgumentException("No field with name '" + fieldName + "' defined");
        }
        action.accept(fieldInfo);
        initializedFields.add(fieldName);
    }

    public boolean isInitialized(String fieldName) {
        if (initializedFields == null) {
            return false;
        }
        return initializedFields.contains(fieldName);
    }

    public void markInitialized(String name) {
        if (initializedFields == null) {
            initializedFields = new HashSet<>();
        }
        initializedFields.add(name);
    }
}
