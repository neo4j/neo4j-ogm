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
package org.neo4j.ogm.context;

import org.neo4j.ogm.annotation.Relationship.Direction;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Utility to help group elements of a common type into a single collection (by relationship type and direction) to be set on an owning object.
 * The ability to set a collection of instances on an owning entity based on the type of instance is insufficient as described in DATAGRAPH-637, DATAGRAPH-636 and Issue 161.
 * The relationship type and direction as well as the type of entity to be mapped are required to be able to correctly determine which instances are to be set for which property of the node entity.
 *
 * @author Adam George
 * @author Luanne Misquitta
 */
class EntityCollector {

    // node id -> relationship -> target type -> (relationshipId, targetGraphId, target)
    private final Map<Long, Map<DirectedRelationship, Map<Class, Collection<TargetTriple>>>> collected = new HashMap<>();

    /**
     * Adds the given collectible target into a collection based on relationship type and direction ready to be set on
     * the given owning entity.
     *
     * @param sourceId              The id of the instance on which the collection is to be set
     * @param relationshipType      The relationship type that this collection corresponds to
     * @param relationshipDirection The relationship direction
     * @param relationshipId        id of collected relationship
     * @param targetId              id of target element
     * @param target                The element to add to the collection that will eventually be set on the owning type
     */
    public void collectRelationship(Long sourceId, Class startPropertyType, String relationshipType,
        Direction relationshipDirection, long relationshipId, long targetId, Object target) {
        record(sourceId, startPropertyType, relationshipType, relationshipDirection,
            new TargetTriple(relationshipId, targetId, target));
    }

    /**
     * Adds the given collectible target into a collection based on relationship type and direction ready to be set on
     * the given owning entity.
     *
     * @param sourceId              The id of the instance on which the collection is to be set
     * @param relationshipType      The relationship type that this collection corresponds to
     * @param relationshipDirection The relationship direction
     * @param targetId              id of target element
     * @param target                The element to add to the collection that will eventually be set on the owning type
     */
    public void collectRelationship(Long sourceId, Class startPropertyType, String relationshipType,
        Direction relationshipDirection, long targetId, Object target) {
        record(sourceId, startPropertyType, relationshipType, relationshipDirection,
            new TargetTriple(targetId, target));
    }

    private void record(Long owningEntityId, Class startPropertyType, String relationshipType,
        Direction relationshipDirection, TargetTriple triple) {
        this.collected.computeIfAbsent(owningEntityId, k -> new HashMap<>());
        DirectedRelationship directedRelationship = new DirectedRelationship(relationshipType, relationshipDirection);
        this.collected.get(owningEntityId).computeIfAbsent(directedRelationship, k -> new HashMap<>());
        this.collected.get(owningEntityId).get(directedRelationship)
            .computeIfAbsent(startPropertyType, k -> new HashSet<>());
        this.collected.get(owningEntityId).get(directedRelationship).get(startPropertyType).add(triple);
    }

    public void forCollectedEntities(CollectedHandler handler) {

        collected.forEach((sourceId, relationshipMap) -> {

            relationshipMap.forEach((relationship, targetTypeMap) -> {
                String type = relationship.type();
                Direction direction = relationship.direction();

                targetTypeMap.forEach((targetType, entityTriples) -> {

                    List<Object> entities = entityTriples.stream().map(TargetTriple::getTarget).collect(toList());

                    handler.handle(sourceId, type, direction, targetType, entities);
                });

            });

        });

    }

    interface CollectedHandler {

        void handle(Long sourceId, String type, Direction direction, Class targetType, Collection<Object> entities);
    }

    /**
     * This is (relationshipId, targetGraphId, target) triple that keeps track of relationships.
     * Relationship id and object id are used for equality - target equality is intentionally ignored.
     * Relationship id is used when relationship has a corresponding RelationshipEntity.
     * For simple relationships only target object id is used because we don't distinguish between simple relationships
     * to same node.
     */
    private static class TargetTriple {

        private final long relationshipId;
        private final long targetGraphId;
        private final Object target;

        TargetTriple(long targetGraphId, Object target) {
            this.relationshipId = -1;
            this.targetGraphId = targetGraphId;
            this.target = target;
        }

        TargetTriple(long relationshipId, long targetGraphId, Object target) {
            this.relationshipId = relationshipId;
            this.targetGraphId = targetGraphId;
            this.target = requireNonNull(target);
        }

        public long getRelationshipId() {
            return relationshipId;
        }

        public Object getTarget() {
            return target;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            TargetTriple that = (TargetTriple) o;

            if (relationshipId != that.relationshipId) {
                return false;
            }
            return targetGraphId == that.targetGraphId;
        }

        @Override
        public int hashCode() {
            int result = (int) (relationshipId ^ (relationshipId >>> 32));
            result = 31 * result + (int) (targetGraphId ^ (targetGraphId >>> 32));
            return result;
        }
    }
}
