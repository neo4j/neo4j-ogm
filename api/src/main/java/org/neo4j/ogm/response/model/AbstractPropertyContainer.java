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
package org.neo4j.ogm.response.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.neo4j.ogm.model.PropertyContainer;

/**
 * @author Michael J. Simons
 * @soundtrack Die Toten Hosen - Willkommen in Deutschland
 */
abstract class AbstractPropertyContainer implements PropertyContainer {
    /**
     * This stores the current set of dynamic properties as they have been stored into this node model from the entity
     * to graph mapping.
     */
    private final Set<String> currentDynamicCompositeProperties = ConcurrentHashMap.newKeySet();

    /**
     * Those are the previous, dynamic composite properties if any.
     */
    private Set<String> previousDynamicCompositeProperties = Collections.emptySet();

    @Override
    public void addCurrentDynamicCompositeProperties(Set<String> additionalDynamicCompositeProperties) {
        this.currentDynamicCompositeProperties.addAll(additionalDynamicCompositeProperties);
    }

    @Override
    public void setPreviousDynamicCompositeProperties(Set<String> previousDynamicCompositeProperties) {
        this.previousDynamicCompositeProperties = new HashSet<>(previousDynamicCompositeProperties);
    }

    @Override
    public String createPropertyRemovalFragment(String variable) {

        Set<String> propertiesToBeRemoved = new HashSet<>(this.previousDynamicCompositeProperties);
        propertiesToBeRemoved.removeAll(this.currentDynamicCompositeProperties);

        if (propertiesToBeRemoved.isEmpty()) {
            return "";
        }

        return propertiesToBeRemoved.stream()
            .map(s -> String.format("%s.`%s`", variable, s))
            .collect(Collectors.joining(",", " REMOVE ", " "));
    }
}
