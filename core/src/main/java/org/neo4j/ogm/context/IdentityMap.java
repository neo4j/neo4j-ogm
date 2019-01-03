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
package org.neo4j.ogm.context;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
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
 */
class IdentityMap {

    // objects with no properties will always hash to this value.
    private static final long SEED = 0xDEADBEEF / (11 * 257);

    private final Map<Long, Long> nodeHash;

    private final Map<Long, Long> relEntityHash;

    private final Map<Long, EntitySnapshot> snapshotsOfNodeEntities;

    private final Map<Long, EntitySnapshot> snapshotsOfRelationshipEntities;

    private final MetaData metaData;

    IdentityMap(MetaData metaData) {
        this.nodeHash = new HashMap<>();
        this.relEntityHash = new HashMap<>();
        this.snapshotsOfNodeEntities = new HashMap<>();
        this.snapshotsOfRelationshipEntities = new HashMap<>();
        this.metaData = metaData;
    }

    /**
     * constructs a 64-bit hash of this object's node properties
     * and maps the object to that hash. The object must not be null
     *
     * @param object   the object whose persistable properties we want to hash
     * @param entityId
     */
    void remember(Object object, Long entityId) {
        ClassInfo classInfo = metaData.classInfo(object);
        if (metaData.isRelationshipEntity(classInfo.name())) {
            this.relEntityHash.put(entityId, hash(object, classInfo));
            this.snapshotsOfRelationshipEntities.put(entityId, EntitySnapshot.basedOn(metaData).take(object));
        } else {
            this.nodeHash.put(entityId, hash(object, classInfo));
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
     * @param entityId
     * @return true if the object hasn't changed since it was remembered, false otherwise
     */
    boolean remembered(Object object, Long entityId) {
        ClassInfo classInfo = metaData.classInfo(object);
        boolean isRelEntity = false;

        if (entityId != null) {
            if (metaData.isRelationshipEntity(classInfo.name())) {
                isRelEntity = true;
            }

            if ((!isRelEntity && !nodeHash.containsKey(entityId)) ||
                (isRelEntity && !relEntityHash.containsKey(entityId))) {
                return false;
            }

            long actual = hash(object, classInfo);
            long expected = isRelEntity ? relEntityHash.get(entityId) : nodeHash.get(entityId);

            return (actual == expected);
        }
        return false;
    }

    /**
     * Returns the snapshot for the given id. The snapshot contains the corresponding entity's dynamic labels and properties
     * as stored during initial load of the entity.
     *
     * @param entity the entity whos snapshot should be retrieved
     * @param entityId the native id of the entity
     * @return A snapshot of dynamic labels and properties or an empty optional.
     */
    Optional<EntitySnapshot> getSnapshotOf(Object entity, Long entityId) {

        EntitySnapshot entitySnapshot = null;

        ClassInfo classInfo = metaData.classInfo(entity);
        if (metaData.isRelationshipEntity(classInfo.name())) {
            entitySnapshot = this.snapshotsOfRelationshipEntities.get(entityId);
        } else {
            entitySnapshot = this.snapshotsOfNodeEntities.get(entityId);
        }

        return Optional.ofNullable(entitySnapshot);
    }

    void clear() {

        this.nodeHash.clear();
        this.relEntityHash.clear();
        this.snapshotsOfNodeEntities.clear();
        this.snapshotsOfRelationshipEntities.clear();
    }

    private long hash(Object object, ClassInfo classInfo) {
        long hash = SEED;

        List<FieldInfo> hashFields = new ArrayList<>(classInfo.propertyFields());
        if (classInfo.labelFieldOrNull() != null) {
            hashFields.add(classInfo.labelFieldOrNull());
        }

        for (FieldInfo fieldInfo : hashFields) {
            Field field = classInfo.getField(fieldInfo);
            Object value = FieldInfo.read(field, object);
            if (value != null) {

                if (value.getClass().isArray()) {
                    hash = hash * 31L + Arrays.hashCode(convertToObjectArray(value));
                } else if (value instanceof Iterable) {
                    hash = hash * 31L + value.hashCode();
                } else {
                    hash = hash * 31L + hash(value.toString());
                }
            }
        }
        return hash;
    }

    private long hash(String string) {
        long h = 1125899906842597L; // prime
        int len = string.length();

        for (int i = 0; i < len; i++) {
            h = 31 * h + string.charAt(i);
        }
        return h;
    }

    /**
     * Convert an array or objects or primitives to an array of objects
     *
     * @param array array of unknown type
     * @return array of objects
     */
    private Object[] convertToObjectArray(Object array) {
        int len = Array.getLength(array);
        Object[] out = new Object[len];
        for (int i = 0; i < len; i++) {
            out[i] = Array.get(array, i);
        }
        return Arrays.asList(out).toArray();
    }
}
