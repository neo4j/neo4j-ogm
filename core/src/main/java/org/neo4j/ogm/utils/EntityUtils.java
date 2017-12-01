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
package org.neo4j.ogm.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.MetaData;

/**
 * The utility methods here will all throw a <code>NullPointerException</code> if invoked with <code>null</code>.
 *
 * @author Luanne Misquitta
 */
public class EntityUtils {

    private static AtomicLong idSequence = new AtomicLong(0);

    public static Long nextRef() {
        return idSequence.decrementAndGet();
    }

    /**
     * Return native id of given node or relationship entity.
     * If the id field is null the field is set to unique negative refId, which is then returned.
     * You most likely want to use {@link org.neo4j.ogm.context.MappingContext#nativeId(Object)}
     *
     * @param entity   entity
     * @param metaData metadata
     * @return native id or refId
     * @throws org.neo4j.ogm.exception.MetadataException when there is no native id field on the entity class
     */
    public static Long identity(Object entity, MetaData metaData) {

        ClassInfo classInfo = metaData.classInfo(entity);
        FieldInfo identityField = classInfo.identityField();
        Object id = identityField.readProperty(entity);
        if (id == null) {
            Long generated = idSequence.decrementAndGet();
            identityField.write(entity, generated);
            return generated;
        } else {
            return (Long) id;
        }
    }

    public static void setIdentity(Object entity, Long identity, MetaData metaData) {
        ClassInfo classInfo = metaData.classInfo(entity);
        if (classInfo.hasIdentityField()) {
            FieldInfo identityField = classInfo.identityField();
            identityField.write(entity, identity);
        }
    }

    /**
     * Returns the full set of labels, both static and dynamic, if any, to apply to a node.
     *
     * @param entity   entity to get the labels for
     * @param metaData metadata
     * @return collection of labels
     */
    public static Collection<String> labels(Object entity, MetaData metaData) {
        ClassInfo classInfo = metaData.classInfo(entity);
        Collection<String> labels = new ArrayList<>(classInfo.staticLabels());
        FieldInfo labelFieldInfo = classInfo.labelFieldOrNull();
        if (labelFieldInfo != null) {
            Collection<String> dynamicLabels = (Collection<String>) labelFieldInfo.readProperty(entity);
            if (dynamicLabels != null) {
                labels.addAll(dynamicLabels);
            }
        }
        return labels;
    }

}
