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
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.session.Session;

/**
 * @author Andreas Berger
 */
public class LazySortedSet<T, DELEGATE extends SortedSet<T>> extends LazySet<T, DELEGATE> implements SortedSet<T> {

    public LazySortedSet(Session session, FieldInfo fieldInfo, long id) {
        super(session, fieldInfo, id);
    }

    LazySortedSet(DELEGATE delegate) {
        super(delegate);
    }

    @Override
    protected DELEGATE createDelegate(Collection<? extends T> result) {
        //noinspection unchecked
        return (DELEGATE) new TreeSet<T>(result);
    }

    @Override
    public Comparator<? super T> comparator() {
        return init().comparator();
    }

    @NotNull
    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return new CheckModifySortedSet(init().subSet(fromElement, toElement));
    }

    @NotNull
    @Override
    public SortedSet<T> headSet(T toElement) {
        return new CheckModifySortedSet(init().headSet(toElement));
    }

    @NotNull
    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return new CheckModifySortedSet(init().tailSet(fromElement));
    }

    @Override
    public T first() {
        return init().first();
    }

    @Override
    public T last() {
        return init().last();
    }

    private class CheckModifySortedSet extends LazySortedSet<T, SortedSet<T>> implements SortedSet<T> {

        CheckModifySortedSet(SortedSet<T> delegate) {
            super(delegate);
        }

        @Override
        protected void changed() {
            LazySortedSet.this.changed();
        }

    }
}
