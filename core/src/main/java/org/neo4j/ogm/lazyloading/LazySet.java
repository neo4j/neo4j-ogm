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

import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.session.Session;

/**
 * @author Andreas Berger
 */
public class LazySet<T, DELEGATE extends Set<T>> extends LazyCollection<T, DELEGATE> implements Set<T> {
    public LazySet(Session session, FieldInfo fieldInfo, long id) {
        super(session, fieldInfo, id);
    }

    LazySet(DELEGATE delegate) {
        super(delegate);
    }

    @Override
    protected DELEGATE createDelegate(Collection<? extends T> result) {
        //noinspection unchecked
        return (DELEGATE) new HashSet<T>(result);
    }
}
