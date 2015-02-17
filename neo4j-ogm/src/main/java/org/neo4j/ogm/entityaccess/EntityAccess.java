/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.entityaccess;

import java.lang.reflect.Array;
import java.util.*;

public abstract class EntityAccess implements PropertyWriter, RelationalWriter {


    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Object merge(Class<?> parameterType, Iterable<?> newValues, Object[] currentValues) {
        if (currentValues != null) {
            return merge(parameterType, newValues, Arrays.asList(currentValues));
        } else {
            return merge(parameterType, newValues, new ArrayList());
        }
    }


    /**
     * Merges the contents of <em>collection</em> with <em>hydrated</em> ensuring no duplicates and returns the result as an
     * instance of the given parameter type.
     *
     * @param parameterType The type of Iterable or array to return
     * @param newValues The objects to merge into a collection of the given parameter type, which may not necessarily be of a
     *        type assignable from <em>parameterType</em> already
     * @param currentValues The Iterable to merge into, which may be <code>null</code> if a new collection needs creating
     * @return The result of the merge, as an instance of the specified parameter type
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Object merge(Class<?> parameterType, Iterable<?> newValues, Iterable<?> currentValues) {

        if (parameterType.isArray()) {
            Class type = parameterType.getComponentType();
            List<Object> objects = new ArrayList<>(union(newValues, currentValues));

            Object array = Array.newInstance(type, objects.size());
            for (int i = 0; i < objects.size(); i++) {
                Array.set(array, i, objects.get(i));
            }
            return array;
        }

        // we don't know how to make the requested parameter type, so let's just try to work with what we've got
        if (currentValues != null && parameterType.isAssignableFrom(currentValues.getClass())) {
            if (Collection.class.isAssignableFrom(currentValues.getClass())) {
                Collection col = (Collection) currentValues;
                for (Object object : newValues) {
                    if (!col.contains(object)) {
                        col.add(object);
                    }
                }
                return currentValues;
            }
        }

        // hydrated is unusable at this point so we can just set the other collection if it's compatible
        if (parameterType.isAssignableFrom(newValues.getClass())) {
            return newValues;
        }

        // create the desired type of collection and use it for the merge
        Collection newCollection = createCollection(parameterType, newValues, currentValues);
        if (newCollection != null) {
            return newCollection;
        }

        throw new RuntimeException("Unsupported: " + parameterType.getName());
    }

    private static Collection<?> createCollection(Class<?> parameterType, Iterable<?> collection, Iterable<?> hydrated) {
        if (Vector.class.isAssignableFrom(parameterType)) {
            return new Vector<>(union(collection, hydrated));
        }
        if (List.class.isAssignableFrom(parameterType)) {
            return new ArrayList<>(union(collection, hydrated));
        }
        if (Set.class.isAssignableFrom(parameterType)) {
            return union(collection, hydrated);
        }
        return null;
    }

    private static Set<Object> union(Iterable<?> collection, Iterable<?> hydrated) {
        Set<Object> set = new HashSet<>();
        for (Object object : collection) {
            set.add(object);
        }
        if (hydrated != null) {
            for (Object object : hydrated) {
                set.add(object);
            }
        }
        return set;
    }

}
