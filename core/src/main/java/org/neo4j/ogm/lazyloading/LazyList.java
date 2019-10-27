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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.session.Session;

/**
 * @author Andreas Berger
 */
public class LazyList<T, DELEGATE extends List<T>> extends LazyCollection<T, DELEGATE> implements List<T> {

    public LazyList(Session session, FieldInfo fieldInfo, long id) {
        super(session, fieldInfo, id);
    }

    LazyList(DELEGATE delegate) {
        super(delegate);
    }

    @Override
    protected DELEGATE createDelegate(Collection<? extends T> result) {
        //noinspection unchecked
        return (DELEGATE) new ArrayList<T>(result);
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T>  c) {
        boolean changed = init().addAll(index, c);
        if (changed) {
            changed();
        }
        return changed;
    }

    @Override
    public T get(int index) {
        return init().get(index);
    }

    @Override
    public T set(int index, T element) {
        T previous = init().set(index, element);
        if (previous != element) {
            changed();
        }
        return previous;
    }

    @Override
    public void add(int index, T element) {
        init().add(index, element);
        changed();
    }

    @Override
    public T remove(int index) {
        changed();
        return init().remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return init().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return init().lastIndexOf(o);
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        return new CheckModifyListIterator(init().listIterator());
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator(int index) {
        init();
        return new CheckModifyListIterator(init().listIterator(index));
    }

    @NotNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        init();
        return new CheckModifySubList(init().subList(fromIndex, toIndex));
    }

    private class CheckModifyListIterator extends CheckModifyIterator<ListIterator<T>> implements ListIterator<T> {

        CheckModifyListIterator(ListIterator<T> iterator) {
            super(iterator);
        }

        @Override public boolean hasPrevious() {
            return iterator.hasPrevious();
        }

        @Override public T previous() {
            return iterator.previous();
        }

        @Override public int nextIndex() {
            return iterator.nextIndex();
        }

        @Override public int previousIndex() {
            return iterator.previousIndex();
        }

        @Override public void set(T t) {
            changed();
            iterator.set(t);
        }

        @Override public void add(T t) {
            changed();
            iterator.add(t);
        }
    }

    private class CheckModifySubList extends LazyList<T, List<T>> {

        CheckModifySubList(List<T> delegate) {
            super(delegate);
        }

        @Override protected void changed() {
            LazyList.this.changed();
        }
    }
}
