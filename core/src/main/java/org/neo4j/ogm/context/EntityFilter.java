/*
 * Copyright (c) 2002-2024 "Neo4j,"
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

import java.util.Optional;

import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.response.model.DefaultGraphModel;
import org.neo4j.ogm.response.model.NodeModel;

/**
 * Filter for entities to check whether nodes/relationships should be included in the mapping result.
 *
 * @author Niels Oertel
 */
interface EntityFilter {

    /**
     * Include any entity.
     */
    EntityFilter INCLUDE_ALWAYS = (graphModel, nativeId, isNode) -> true;

    /**
     * Include all relationships but only nodes which are not generated.
     */
    EntityFilter WITHOUT_GENERATED_NODES = (graphModel, nativeId, isNode) -> {
        if (!isNode) {
            return true;
        } else {
            Optional<Node> node = ((DefaultGraphModel) graphModel).findNode(nativeId);
            if (!node.isPresent()) {
                return true; // this should actually never happen but to keep existing behaviour, we are not throwing an exception
            }
            return node.map(n -> !((NodeModel) n).isGeneratedNode()).get();
        }
    };

    /**
     * Check if an object with given native id should be included in the mapping result.
     *
     * @param graphModel
     *            The graph model.
     * @param nativeObjectId
     *            The object's native id.
     * @param isNode
     *            True if the object is a node, false if relationship.
     *
     * @return True if the object should be included.
     */
    boolean shouldIncludeModelObject(GraphModel graphModel, long nativeObjectId, boolean isNode);

}
