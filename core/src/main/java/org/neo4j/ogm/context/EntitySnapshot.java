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

import static java.util.stream.Collectors.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.MetaData;

/**
 * This class stores a snapshot of dynamic attributes of an entity. Currently this does happend only once, when the
 * entity is mapped initially.
 *
 * @author Michael J. Simons
 * @soundtrack Daft Punk - Random Access Memories
 * @since 3.1.6
 */
class EntitySnapshot {
    /**
     * The set of dynamic labels a node had during the time the snapshot was taken.
     */
    private final Set<String> dynamicLabels;

    /**
     * The set of all dynamic properties a node or a relationship had during the time the snapshot was taken. Dynamic
     * properties are all those retrieved from {@link org.neo4j.ogm.typeconversion.CompositeAttributeConverter CompositeAttributeConvers}.
     */
    private final Set<String> dynamicCompositeProperties;

    private EntitySnapshot(Set<String> dynamicLabels, Set<String> dynamicCompositeProperties) {
        this.dynamicLabels = dynamicLabels;
        this.dynamicCompositeProperties = dynamicCompositeProperties;
    }

    /**
     * @return Immutable snapshotted set of dynamic labels.
     */
    public Set<String> getDynamicLabels() {
        return dynamicLabels;
    }

    /**
     * @return Immutable snapshotted set of dynamic, composite properties.
     */
    public Set<String> getDynamicCompositeProperties() {
        return dynamicCompositeProperties;
    }

    /**
     * Starts a new snapshot based on the given metadata
     *
     * @param metaData The metadata for field extraction
     * @return Snapshot ready to be taken.
     */
    static Builder basedOn(MetaData metaData) {
        return new Builder(metaData);
    }

    static class Builder {
        private final MetaData metaData;

        Builder(MetaData metaData) {
            this.metaData = metaData;
        }

        /**
         * Takes a snapshot of the given entity.
         *
         * @param entity
         * @return
         */
        EntitySnapshot take(Object entity) {

            ClassInfo classInfo = metaData.classInfo(entity);

            Set<String> labels = extractLabels(classInfo, entity);
            Set<String> compositeProperties = extractCompositeProperties(classInfo, entity);

            return new EntitySnapshot(labels, compositeProperties);
        }

        private static Set<String> extractLabels(ClassInfo classInfo, Object entity) {
            return Optional.ofNullable(classInfo.labelFieldOrNull())
                .map(labelField -> labelField.read(entity))
                .map(Builder::unsafeCastLabels)
                .map(Collections::unmodifiableSet)
                .orElse(Collections.emptySet());
        }

        private static Set<String> extractCompositeProperties(ClassInfo classInfo, Object entity) {
            return classInfo.propertyFields().stream()
                .filter(FieldInfo::isComposite)
                .flatMap(f -> f.readComposite(entity).keySet().stream())
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
        }

        @SuppressWarnings("unchecked")
        private static Set<String> unsafeCastLabels(Object labels) {
            return new HashSet<>((Collection<String>) labels);
        }
    }
}
