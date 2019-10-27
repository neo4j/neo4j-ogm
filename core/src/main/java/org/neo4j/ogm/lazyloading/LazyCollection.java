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
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.session.Session;

/**
 * @author Andreas Berger
 */
public abstract class LazyCollection<T, DELEGATE extends Collection<T>> implements Collection<T> {

    private BaseLazyLoader loader;
    private FieldInfo fieldInfo;
    protected DELEGATE delegate;
    protected DELEGATE loadedByInitialQuery;
    protected boolean modified;

    public LazyCollection(Session session, FieldInfo fieldInfo, long id) {
        loader = new BaseLazyLoader(id, session);
        this.fieldInfo = fieldInfo;
    }

    LazyCollection(DELEGATE delegate) {
        this.delegate = delegate;
    }

    public boolean isInitialized() {
        return delegate != null;
    }

    public boolean isModified() {
        return modified;
    }

    protected void changed() {
        modified = true;
    }

    public void addLoadedData(Collection<T> other) {
        if (loadedByInitialQuery == null) {
            loadedByInitialQuery = createDelegate(other);
        } else {
            loadedByInitialQuery.addAll(other);
        }
    }

    public Collection<T> getLoadedEntities() {
        if (isInitialized()) {
            return delegate;
        }
        return loadedByInitialQuery == null ? Collections.emptySet() : loadedByInitialQuery;
    }

    public void reset() {
        if (loader != null) {
            modified = false;
            delegate = null;
        }
    }

    public void validateSession(Session session) {
        if (loader != null && isInitialized()) {
            loader.validateSession(session);
        }
    }

    protected DELEGATE init() {
        if (isInitialized()) {
            return delegate;
        }
        Collection<T> result = loader.readRelation(fieldInfo);
        delegate = createDelegate(result);
        return delegate;
    }

    protected abstract DELEGATE createDelegate(Collection<? extends T> result);

    @Override
    public int size() {
        return init().size();
    }

    @Override
    public boolean isEmpty() {
        return init().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return init().contains(o);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new CheckModifyIterator<>(init().iterator());
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return init().toArray();
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        return init().toArray(a);
    }

    @Override
    public boolean add(T t) {
        boolean changed = init().add(t);
        if (changed) {
            changed();
        }
        return changed;
    }

    @Override
    public boolean remove(Object o) {
        boolean changed = init().remove(o);
        if (changed) {
            changed();
        }
        return changed;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return init().containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        boolean changed = init().addAll(c);
        if (changed) {
            changed();
        }
        return changed;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean changed = init().removeAll(c);
        if (changed) {
            changed();
        }
        return changed;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        boolean changed = init().retainAll(c);
        if (changed) {
            changed();
        }
        return changed;
    }

    @Override
    public void clear() {
        DELEGATE d = init();
        if (!d.isEmpty()) {
            changed();
        }
        d.clear();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return init().equals(o);
    }

    @Override
    public int hashCode() {
        return init().hashCode();
    }

    class CheckModifyIterator<I extends Iterator<T>> implements Iterator<T> {
        I iterator;

        CheckModifyIterator(I iterator) {
            this.iterator = iterator;
        }

        @Override public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override public T next() {
            return iterator.next();
        }

        @Override public void remove() {
            changed();
            iterator.remove();
        }

        @Override public void forEachRemaining(Consumer<? super T> action) {
            iterator.forEachRemaining(action);
        }
    }

}
