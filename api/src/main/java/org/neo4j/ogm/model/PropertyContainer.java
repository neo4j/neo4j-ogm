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
package org.neo4j.ogm.model;

import java.util.Set;

/**
 * Common interface for {@link Node} and {@link Edge} to allow common query generation
 *
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public interface PropertyContainer {

    Long getId();

    /**
     * Return current version of the node, null if the relationship entity is new
     *
     * @return version property with current version
     */
    Property<String, Long> getVersion();

    void setPreviousDynamicCompositeProperties(Set<String> previousDynamicCompositeProperties);

    void addCurrentDynamicCompositeProperties(Set<String> additionalDynamicCompositeProperties);

    /**
     * Should create the Cypher fragment that removes previous composite properties that aren't in the container anymore.
     *
     * @param variable The variable of the node or relationship to defer.
     */
    String createPropertyRemovalFragment(String variable);
}
