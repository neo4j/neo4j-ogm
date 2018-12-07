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
package org.neo4j.ogm.model;

import java.util.Set;

/**
 * Common interface for {@link Node} and {@link Edge} to allow common query generation
 *
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public interface PropertyContainer {

    /**
     * Return current version of the node, null if the relationship entity is new
     *
     * @return version property with current version
     */
    Property<String, Long> getVersion();

    void setPreviousDynamicCompositeProperties(Set<String> previousDynamicCompositeProperties);

    void addCurrentDynamicCompositeProperties(Set<String> currentDynamicCompositeProperties);

    /**
     * Should create the Cypher fragment that removes previous composite properties that aren't in the container anymore.
     *
     * @param variable The variable of the node or relationship to defer.
     */
    String createPropertyRemovalFragment(String variable);
}
