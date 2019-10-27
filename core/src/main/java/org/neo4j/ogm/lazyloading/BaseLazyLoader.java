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

import static org.neo4j.ogm.annotation.Relationship.*;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.neo4j.ogm.metadata.DescriptorMappings;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.session.Session;

/**
 * @author Andreas Berger
 */
class BaseLazyLoader {
    private final long id;
    private final Session session;

    BaseLazyLoader(Long id, Session session) {
        this.id = id;
        this.session = session;
    }

    public void validateSession(Session ses) {
        if (ses != this.session) {
            throw new LazyInitializationException(
                "The session the entity is to be saved with is different from the session it was loaded with.");
        }
    }

    protected <T> Collection<T> readRelation(FieldInfo fieldInfo) {
        String in = "";
        String out = "";
        String direction = fieldInfo.relationshipDirection(OUTGOING);
        if (direction.equals(INCOMING)) {
            in = "<";
        } else if (direction.equals(OUTGOING)) {
            out = ">";
        }
        String query =
            "MATCH (n)" + in + "-[r:`" + fieldInfo.relationship() + "`]-" + out + "(m) WHERE ID(n) = {n} RETURN r, m";
        //noinspection unchecked
        Class<T> entityType = (Class<T>) DescriptorMappings.getType(fieldInfo.typeParameterDescriptor());
        Iterable<T> result = session.query(entityType, query, Collections.singletonMap("n", id));
        if (result instanceof Collection<?>) {
            return (Collection<T>) result;
        } else {
            return StreamSupport
                .stream(result.spliterator(), false)
                .collect(Collectors.toList());
        }
    }
}
