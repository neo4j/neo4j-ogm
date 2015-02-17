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

package org.neo4j.ogm.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utility to help group elements of a common type into a single collection to be set on an owning object.
 */
class EntityCollector {

    private final Logger logger = LoggerFactory.getLogger(EntityCollector.class);
    private final Map<Object, Map<Class<?>, Set<Object>>> typeRelationships = new HashMap<>();

    /**
     * Adds the given collectible element into a collection ready to be set on the given owning type.
     *
     * @param owningEntity The type on which the collection is to be set
     * @param collectibleElement The element to add to the collection that will eventually be set on the owning type
     */
    public void recordTypeRelationship(Object owningEntity, Object collectibleElement) {
        Map<Class<?>, Set<Object>> handled = this.typeRelationships.get(owningEntity);
        if (handled == null) {
            this.typeRelationships.put(owningEntity, handled = new HashMap<>());
        }
        Class<?> type = collectibleElement.getClass();
        Set<Object> objects = handled.get(type);
        if (objects == null) {
            handled.put(type, objects = new HashSet<>());
        }
        objects.add(collectibleElement);
    }

    /**
     * @return All the owning types that have been added to this {@link EntityCollector}
     */
    public Iterable<Object> getOwningTypes() {
        return this.typeRelationships.keySet();
    }

    /**
     * Retrieves the type map that corresponds to the given owning object, which is a mapping between a type and
     * a collection of instances of that type to set on the owning object.
     *
     * @param owningObject The object for which to retrieve the type map
     * @return The type map for the given object or an empty map if it's unknown, never <code>null</code>
     */
    public Map<Class<?>, Set<Object>> getTypeCollectionMapping(Object owningObject) {
        Map<Class<?>, Set<Object>> handled = this.typeRelationships.get(owningObject);
        return handled != null ? handled : Collections.<Class<?>, Set<Object>> emptyMap();
    }

}
