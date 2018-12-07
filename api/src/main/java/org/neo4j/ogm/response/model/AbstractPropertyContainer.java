/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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
    public void addCurrentDynamicCompositeProperties(Set<String> currentDynamicCompositeProperties) {
        this.currentDynamicCompositeProperties.addAll(currentDynamicCompositeProperties);
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
