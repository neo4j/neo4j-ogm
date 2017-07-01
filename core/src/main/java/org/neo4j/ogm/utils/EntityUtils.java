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
package org.neo4j.ogm.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.entity.io.EntityAccessManager;
import org.neo4j.ogm.entity.io.FieldReader;
import org.neo4j.ogm.entity.io.FieldWriter;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The utility methods here will all throw a <code>NullPointerException</code> if invoked with <code>null</code>.
 *
 * @author Luanne Misquitta
 */
public class EntityUtils {

    private static AtomicLong idSequence = new AtomicLong(0);

    public static Long identity(Object entity, MetaData metaData) {

        ClassInfo classInfo = metaData.classInfo(entity);
        Object id = EntityAccessManager.getIdentityPropertyReader(classInfo).readProperty(entity);
        if (id == null) {
            Long generated = idSequence.decrementAndGet();
            Field identityField = classInfo.getField(classInfo.identityField());
            FieldWriter.write(identityField, entity, generated);
            return generated;
        } else {
            return (Long) id;
        }
    }

    /**
     * Returns the full set of labels, both static and dynamic, if any, to apply to a node.
     */
    public static Collection<String> labels(Object entity, MetaData metaData) {
        ClassInfo classInfo = metaData.classInfo(entity);
        Collection<String> staticLabels = classInfo.staticLabels();
        FieldInfo labelFieldInfo = classInfo.labelFieldOrNull();
        if (labelFieldInfo != null) {
            FieldReader reader = new FieldReader(classInfo, labelFieldInfo);
            Collection<String> labels = (Collection<String>) reader.readProperty(entity);
            return CollectionUtils.union(staticLabels, labels);
        }
        return staticLabels;
    }
}
