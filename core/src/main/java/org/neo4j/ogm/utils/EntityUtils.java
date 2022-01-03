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
package org.neo4j.ogm.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
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
        } else if (identity == null) {
            // Reset any generated field if the new value is null in case the generated values is not an internal id.
            classInfo.fieldsInfo().fields().stream().filter(f -> f.getAnnotations().has(Id.class) &&
                f.getAnnotations().has(GeneratedValue.class)).findFirst()
                .ifPresent(generatedField -> generatedField.write(entity, null));
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
