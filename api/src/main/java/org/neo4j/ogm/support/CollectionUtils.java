/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
package org.neo4j.ogm.support;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Utilities around collections.
 *
 * @author Michael J. Simons
 */
public final class CollectionUtils {

    /**
     * Turns {@code thingToIterable} into an iterable. In case of {@literal null}, it returns an empty collection.
     * Returns the iterable itself when the thing is already iterable. An array will be turned into an iterable.
     * A single object will be returned as a singleton list.
     *
     * @param thingToIterable A thing that should be iterated over. Can be null.
     * @return An iterable.
     */
    public static Iterable<Object> iterableOf(Object thingToIterable) {

        if (thingToIterable == null) {
            return Collections.emptyList();
        } else if (thingToIterable instanceof Iterable) {
            return ((Iterable) thingToIterable);
        } else if (thingToIterable.getClass().isArray()) {
            return () -> new ArrayIterator(thingToIterable);
        } else {
            return Collections.singletonList(thingToIterable);
        }
    }

    /**
     * @param entryValue A value that is needed as a collection.
     * @return Returns a collection if {@code entryValue} is iterable but not already a collection.
     */
    public static Object materializeIterableIf(Object entryValue) {

        if (entryValue instanceof Iterable && !(entryValue instanceof Collection)) {
            List hlp = new ArrayList<>();
            ((Iterable) entryValue).forEach(hlp::add);
            entryValue = hlp;
        }
        return entryValue;
    }

    private CollectionUtils() {
    }

    private static class ArrayIterator implements Iterator<Object> {
        private final Object source;
        private final int length;

        private int index = 0;

        private ArrayIterator(Object source) {
            this.source = source;
            this.length = Array.getLength(source);
        }

        @Override
        public boolean hasNext() {
            return index < length;
        }

        @Override
        public Object next() {
            return Array.get(source, index++);
        }
    }
}
