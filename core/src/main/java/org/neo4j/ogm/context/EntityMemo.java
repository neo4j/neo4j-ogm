/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.context;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.entity.io.FieldWriter;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;

/**
 * @author Vince Bickers
 * @author Mark Angrish
 */
class EntityMemo {

    // objects with no properties will always hash to this value.
    private static final long SEED = 0xDEADBEEF / (11 * 257);

    private final Map<Long, Long> nodeHash;

    private final Map<Long, Long> relEntityHash;

    private final MetaData metaData;

    EntityMemo(MetaData metaData) {
        this.nodeHash = new HashMap<>();
        this.relEntityHash = new HashMap<>();
        this.metaData = metaData;
    }

    /**
     * constructs a 64-bit hash of this object's node properties
     * and maps the object to that hash. The object must not be null
     *
     * @param entityId the id of the entity
     * @param object the object whose persistable properties we want to hash
     * @param classInfo metadata about the object
     */
    public void remember(Long entityId, Object object, ClassInfo classInfo) {
        if (metaData.isRelationshipEntity(classInfo.name())) {
            relEntityHash.put(entityId, hash(object, classInfo));
        } else {
            nodeHash.put(entityId, hash(object, classInfo));
        }
    }

    /**
     * determines whether the specified has already
     * been memorised. The object must not be null. An object
     * is regarded as memorised if its hash value in the memo hash
     * is identical to a recalculation of its hash value.
     *
     * @param entityId the id of the entity
     * @param object the object whose persistable properties we want to check
     * @param classInfo metadata about the object
     * @return true if the object hasn't changed since it was remembered, false otherwise
     */
    boolean remembered(Long entityId, Object object, ClassInfo classInfo) {
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

    void clear() {
        nodeHash.clear();
        relEntityHash.clear();
    }


    private static long hash(Object object, ClassInfo classInfo) {
        long hash = SEED;

        List<FieldInfo> hashFields = new ArrayList<>(classInfo.propertyFields());
        if (classInfo.labelFieldOrNull() != null) {
            hashFields.add(classInfo.labelFieldOrNull());
        }

        for (FieldInfo fieldInfo : hashFields) {
            Field field = classInfo.getField(fieldInfo);
            Object value = FieldWriter.read(field, object);
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

    private static long hash(String string) {
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
    private static Object[] convertToObjectArray(Object array) {
        int len = Array.getLength(array);
        Object[] out = new Object[len];
        for (int i = 0; i < len; i++) {
            out[i] = Array.get(array, i);
        }
        return Arrays.asList(out).toArray();
    }
}
