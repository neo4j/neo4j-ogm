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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.MetaData;

/**
 * Maintains entity footprints for dirty checking.
 *
 * @author Vince Bickers
 * @author Mark Angrish
 * @author Michael J. Simons
 * @author Andreas Berger
 */
class IdentityMap {

    // objects with no properties will always hash to this value.
    private static final long SEED = 0xDEADBEEF / (11 * 257);

    private final Map<Long, Long> nodeHashes;

    private final Map<Long, Long> relEntityHashes;

    private final Map<Long, EntitySnapshot> snapshotsOfNodeEntities;

    private final Map<Long, EntitySnapshot> snapshotsOfRelationshipEntities;

    private final MetaData metaData;

    IdentityMap(MetaData metaData) {
        this.nodeHashes = new HashMap<>();
        this.relEntityHashes = new HashMap<>();
        this.snapshotsOfNodeEntities = new HashMap<>();
        this.snapshotsOfRelationshipEntities = new HashMap<>();
        this.metaData = metaData;
    }

    /**
     * constructs a 64-bit hash of this object's node properties
     * and maps the object to that hash. The object must not be null
     *
     * @param object   the object whose persistable properties we want to hash
     * @param entityId the native id of the entity
     */
    void remember(Object object, Long entityId) {
        ClassInfo classInfo = metaData.classInfo(object);
        if (metaData.isRelationshipEntity(classInfo.name())) {
            this.relEntityHashes.put(entityId, hash(object, classInfo));
            this.snapshotsOfRelationshipEntities.put(entityId, EntitySnapshot.basedOn(metaData).take(object));
        } else {
            this.nodeHashes.put(entityId, hash(object, classInfo));
            this.snapshotsOfNodeEntities.put(entityId, EntitySnapshot.basedOn(metaData).take(object));
        }
    }

    /**
     * determines whether the specified has already
     * been memorised. The object must not be null. An object
     * is regarded as memorised if its hash value in the memo hash
     * is identical to a recalculation of its hash value.
     *
     * @param object   the object whose persistable properties we want to check
     * @param entityId the native id of the entity
     * @return true if the object hasn't changed since it was remembered, false otherwise
     */
    boolean remembered(Object object, Long entityId) {

        // Bail out early if the native id is null...
        if (entityId == null) {
            return false;
        }

        ClassInfo classInfo = metaData.classInfo(object);
        boolean isRelEntity = metaData.isRelationshipEntity(classInfo.name());
        Map<Long, Long> hashes = isRelEntity ? relEntityHashes : nodeHashes;

        // ... or a little later when the hashes in question doesnt contain the entities id
        if (!hashes.containsKey(entityId)) {
            return false;
        }

        long actual = hash(object, classInfo);
        long expected = hashes.get(entityId);
        return actual == expected;
    }

    /**
     * Returns the snapshot for the given id. The snapshot contains the corresponding entity's dynamic labels and properties
     * as stored during initial load of the entity.
     *
     * @param entity   the entity whos snapshot should be retrieved
     * @param entityId the native id of the entity
     * @return A snapshot of dynamic labels and properties or an empty optional.
     */
    Optional<EntitySnapshot> getSnapshotOf(Object entity, Long entityId) {

        EntitySnapshot entitySnapshot;

        ClassInfo classInfo = metaData.classInfo(entity);
        if (metaData.isRelationshipEntity(classInfo.name())) {
            entitySnapshot = this.snapshotsOfRelationshipEntities.get(entityId);
        } else {
            entitySnapshot = this.snapshotsOfNodeEntities.get(entityId);
        }

        return Optional.ofNullable(entitySnapshot);
    }

    void clear() {

        this.nodeHashes.clear();
        this.relEntityHashes.clear();
        this.snapshotsOfNodeEntities.clear();
        this.snapshotsOfRelationshipEntities.clear();
    }

    private static long hash(Object object, ClassInfo classInfo) {

        List<FieldInfo> hashFields = new ArrayList<>(classInfo.propertyFields());
        if (classInfo.labelFieldOrNull() != null) {
            hashFields.add(classInfo.labelFieldOrNull());
        }

        long hash = SEED;
        for (FieldInfo fieldInfo : hashFields) {

            Object value = fieldInfo.read(object);
            if (value != null) {
                if (value.getClass().isArray()) {
                    hash = hash * 31L + hashArray(value);
                } else {
                    hash = hash * 31L + value.hashCode();
                }
            }
        }
        return hash;
    }

    /**
     * hashes an array of objects or primitives
     *
     * @param array array of unknown type
     * @return the hash of the array
     */
    private static long hashArray(Object array) {
        long result = 1;
        int len = Array.getLength(array);
        for (int i = 0; i < len; i++) {
            Object element = Array.get(array, i);
            result = 31L * result + (element == null ? 0 : element.hashCode());
        }
        return result;
    }
}
