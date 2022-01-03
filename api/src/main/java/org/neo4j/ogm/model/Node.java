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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Vince Bickers
 * @author Mark Angrish
 * @author Michael J. Simons
 */
public interface Node extends PropertyContainer {

    String[] getLabels();

    List<Property<String, Object>> getPropertyList();

    String getPrimaryIndex();

    /**
     * Returns "label signature" for this node - added and removed labels, sorted alphabetically
     */
    String labelSignature();

    /**
     * Returns if the node has version property
     */
    boolean hasVersionProperty();

    /**
     * Return current version of the node, null if the node is new
     *
     * @return version property with current version
     */
    Property<String, Long> getVersion();

    Set<String> getPreviousDynamicLabels();

    /**
     * Turns this node object into a row. The version field is treated different, it is not included with the standard properties,
     * but only as separate value.
     *
     * @param nodeIdTarget The name under which to store this nodes id. The value is used as a backreference.
     * @return A map of values representing this node.
     */
    default Map<String, Object> toRow(String nodeIdTarget) {
        Map<String, Object> rowMap = new HashMap<>();
        rowMap.put(nodeIdTarget, this.getId());
        Map<String, Object> props = new HashMap<>();
        for (Property property : this.getPropertyList()) {
            // Don't include version property into props, it will be incremented by the query
            if (!property.equals(this.getVersion())) {
                props.put((String) property.getKey(), property.getValue());
            }
        }
        rowMap.put("props", props);
        if (this.hasVersionProperty()) {
            Property version = this.getVersion();
            rowMap.put((String) version.getKey(), version.getValue());
        }
        return rowMap;
    }
}
