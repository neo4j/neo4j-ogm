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
package org.neo4j.ogm.result.adapter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.response.model.DefaultGraphModel;
import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.response.model.RelationshipModel;

/**
 * This adapter will transform an embedded response into a json response
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public abstract class GraphModelAdapter extends BaseAdapter implements ResultAdapter<Map<String, Object>, GraphModel> {

    /**
     * Parses a row from the result object and transforms it into a GraphModel
     *
     * @param data the data to transform, given as a map
     * @return the data transformed to an {@link GraphModel}
     */
    @Override
    public GraphModel adapt(Map<String, Object> data) {

        // These two sets keep track of which nodes and edges have already been built, so we don't redundantly
        // write the same node or relationship entity many times.
        final Set<Long> nodeIdentities = new HashSet<>();
        final Set<Long> edgeIdentities = new HashSet<>();

        DefaultGraphModel graphModel = new DefaultGraphModel();

        for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
            final Object value = mapEntry.getValue();

            String resultKey = mapEntry.getKey();
            boolean generatedNodes = ResultAdapter.describesGeneratedNode(resultKey);

            adaptInternal(nodeIdentities, edgeIdentities, graphModel, value, generatedNodes);
        }

        return graphModel;
    }

    private void adaptInternal(Set<Long> nodeIdentities, Set<Long> edgeIdentities, DefaultGraphModel graphModel,
        Object value, boolean generatedNodes) {
        if (isPath(value)) {
            buildPath(value, graphModel, nodeIdentities, edgeIdentities, generatedNodes);
        } else if (isNode(value)) {
            buildNode(value, graphModel, nodeIdentities, generatedNodes);
        } else if (isRelationship(value)) {
            buildRelationship(value, graphModel, edgeIdentities);
        } else if (value instanceof Iterable) {
            Iterable collection = (Iterable) value;
            for (Object element : collection) {
                adaptInternal(nodeIdentities, edgeIdentities, graphModel, element, generatedNodes);
            }
        }
    }

    void buildPath(Object path, DefaultGraphModel graphModel, Set nodeIdentities, Set edgeIdentities,
        boolean generatedNodes) {

        Iterator<Object> relIterator = relsInPath(path).iterator();
        Iterator<Object> nodeIterator = nodesInPath(path).iterator();

        while (relIterator.hasNext()) {
            buildRelationship(relIterator.next(), graphModel, edgeIdentities);
        }

        while (nodeIterator.hasNext()) {
            buildNode(nodeIterator.next(), graphModel, nodeIdentities, generatedNodes);
        }
    }

    void buildNode(Object node, DefaultGraphModel graphModel, Set<Long> nodeIdentities, boolean generatedNode) {

        long nativeId = nodeId(node);
        if (nodeIdentities.contains(nativeId)) {
            return;
        }

        nodeIdentities.add(nativeId);

        NodeModel nodeModel = new NodeModel(nativeId);
        List<String> labels = labels(node);

        nodeModel.setLabels(labels.toArray(new String[0]));
        nodeModel.setProperties(convertArrayPropertiesToCollection(properties(node)));
        nodeModel.setGeneratedNode(generatedNode);

        graphModel.addNode(nodeModel);
    }

    void buildRelationship(Object relationship, DefaultGraphModel graphModel, Set<Long> edgeIdentities) {
        if (edgeIdentities.contains(relationshipId(relationship))) {
            return;
        }

        edgeIdentities.add(relationshipId(relationship));

        RelationshipModel edgeModel = new RelationshipModel();
        edgeModel.setId(relationshipId(relationship));
        edgeModel.setType(relationshipType(relationship));
        edgeModel.setStartNode(startNodeId(relationship));
        edgeModel.setEndNode(endNodeId(relationship));

        edgeModel.setProperties(convertArrayPropertiesToCollection(properties(relationship)));

        graphModel.addRelationship(edgeModel);
    }

    public abstract boolean isPath(Object value);

    public abstract boolean isNode(Object value);

    public abstract boolean isRelationship(Object value);

    public abstract long nodeId(Object node);

    public abstract List<String> labels(Object node);

    public abstract long relationshipId(Object relationship);

    public abstract String relationshipType(Object relationship);

    public abstract Long startNodeId(Object relationship);

    public abstract Long endNodeId(Object relationship);

    public abstract Map<String, Object> properties(Object container);

    public abstract List<Object> nodesInPath(Object path);

    public abstract List<Object> relsInPath(Object path);
}
