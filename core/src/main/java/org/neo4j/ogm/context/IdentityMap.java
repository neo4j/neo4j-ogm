/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.utils.EntityUtils;

/**
 * Maintains entity footprints for dirty checking.
 *
 * @author Vince Bickers
 * @author Mark Angrish
 */
class IdentityMap {

    // objects with no properties will always hash to this value.
    private static final long SEED = 0xDEADBEEF / (11 * 257);

    private final Map<Long, Long> nodeHash;

    private final Map<Long, Long> relEntityHash;

    private final Map<Long, LabelHistory> labelHistoryRegister;

    private final MetaData metaData;

    IdentityMap(MetaData metaData) {
        this.nodeHash = new HashMap<>();
        this.relEntityHash = new HashMap<>();
        labelHistoryRegister = new HashMap<>();
        this.metaData = metaData;
    }

    /**
     * constructs a 64-bit hash of this object's node properties
     * and maps the object to that hash. The object must not be null
     *
     * @param object the object whose persistable properties we want to hash
     */
    void remember(Object object) {
        ClassInfo classInfo = metaData.classInfo(object);
        Long entityId = EntityUtils.getEntityId(metaData, object);
        if (metaData.isRelationshipEntity(classInfo.name())) {
            relEntityHash.put(entityId, hash(object, classInfo));
        } else {
            nodeHash.put(entityId, hash(object, classInfo));
        }
        collectLabelHistory(object, classInfo);
    }

    /**
     * determines whether the specified has already
     * been memorised. The object must not be null. An object
     * is regarded as memorised if its hash value in the memo hash
     * is identical to a recalculation of its hash value.
     *
     * @param object the object whose persistable properties we want to check
     * @return true if the object hasn't changed since it was remembered, false otherwise
     */
    boolean remembered(Object object) {
        ClassInfo classInfo = metaData.classInfo(object);
        Long entityId = EntityUtils.getEntityId(metaData, object);
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

    private void collectLabelHistory(Object entity, ClassInfo classInfo) {
        FieldInfo fieldInfo = classInfo.labelFieldOrNull();
        if (fieldInfo != null) {
            Collection<String> labels = (Collection<String>) fieldInfo.read(entity);
            labelHistory(entity).push(labels);
        }
    }

    LabelHistory labelHistory(Object entity) {
        Long identity = EntityUtils.getEntityId(metaData, entity);
        return labelHistoryRegister.computeIfAbsent(identity, k -> new LabelHistory());
    }

    void clear() {
        nodeHash.clear();
        relEntityHash.clear();
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
                // TODO is it correct ? seems not to be based on java hashcode and not only on sub objects
                // persistent fields ?
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
