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

import java.util.Optional;
import java.util.function.BiFunction;

import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultGraphModel;
import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.session.EntityInstantiator;

/**
 * @author Michael J. Simons
 */
public class GraphRowModelMapper implements ResponseMapper<GraphModel> {

    private final GraphEntityMapper delegate;

    public GraphRowModelMapper(MetaData metaData, MappingContext mappingContext,
        EntityInstantiator entityInstantiator) {

        this.delegate = new GraphEntityMapper(metaData, mappingContext, entityInstantiator);
    }

    @Override
    public <T> Iterable<T> map(Class<T> type, Response<GraphModel> response) {

        BiFunction<GraphModel, Long, Boolean> isNotGeneratedNode = (graphModel, nativeId) -> {
            Optional<Node> node = ((DefaultGraphModel) graphModel).findNode(nativeId);
            if (!node.isPresent()) {
                return true; // Native id describes a relationship
            }
            return node.map(n -> !((NodeModel) n).isGeneratedNode()).get();
        };
        return delegate.map(type, response, isNotGeneratedNode);
    }
}
