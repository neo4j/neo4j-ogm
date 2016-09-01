/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

package org.neo4j.ogm.result.adapter;

import java.util.HashMap;
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
 * @author vince
 * @author Luanne Misquitta
 */
public abstract class GraphModelAdapter implements ResultAdapter<Map<String, Object>, GraphModel> {

    /**
     * Parses a row from the result object and transforms it into a GraphModel
     *
     * @param data the data to transform, given as a map
     * @return the data transformed to an {@link GraphModel}
     */
    public GraphModel adapt(Map<String, Object> data) {

        // These two sets keep track of which nodes and edges have already been built, so we don't redundantly
        // write the same node or relationship entity many times.
        final Set<Long> nodeIdentities = new HashSet<>();
        final Set<Long> edgeIdentities = new HashSet<>();

        GraphModel graphModel = new DefaultGraphModel();

        for (Map.Entry<String,Object> mapEntry : data.entrySet()) {

            if (isPath(mapEntry.getValue())) {
                buildPath(mapEntry.getValue(), graphModel, nodeIdentities, edgeIdentities);
            }
            else if (isNode(mapEntry.getValue())) {
                buildNode(mapEntry.getValue(), graphModel, nodeIdentities);
            }
            else if (isRelationship(mapEntry.getValue())) {
                buildRelationship(mapEntry.getValue(), graphModel, nodeIdentities, edgeIdentities);
            }

            else if (mapEntry.getValue() instanceof Iterable) {
                Iterable<Object> collection = (Iterable) mapEntry.getValue();
                for (Object element : collection) {
                    if (isPath(element)) {
                        buildPath(element, graphModel, nodeIdentities, edgeIdentities);
                    } else if (isNode(element)) {
                        buildNode(element, graphModel, nodeIdentities);
                    } else if (isRelationship(element)) {
                        buildRelationship(element, graphModel, nodeIdentities, edgeIdentities);
                    } else {
                        throw new RuntimeException("Not handled:" + mapEntry.getValue().getClass());
                    }
                }
            }
        }

        return graphModel;
    }

    public void buildPath(Object path, GraphModel graphModel, Set nodeIdentities, Set edgeIdentities) {
        Iterator<Object> relIterator = relsInPath(path).iterator();
        Iterator<Object> nodeIterator =nodesInPath(path).iterator();

        while (relIterator.hasNext()) {
            buildRelationship(relIterator.next(), graphModel, nodeIdentities, edgeIdentities);
        }

        while (nodeIterator.hasNext()) {
            buildNode(nodeIterator.next(), graphModel, nodeIdentities);
        }
    }

    public void buildNode(Object node, GraphModel graphModel, Set nodeIdentities) {
        if (!nodeIdentities.contains(nodeId(node))) {

            nodeIdentities.add(nodeId(node));

            NodeModel nodeModel = new NodeModel();
            nodeModel.setId(nodeId(node));
            List<String> labelNames = labels(node);

            nodeModel.setLabels(labelNames.toArray(new String[] {}));

            nodeModel.setProperties(convertArrayPropertiesToIterable(properties(node)));

            graphModel.getNodes().add(nodeModel);
        }
    }

    public void buildRelationship(Object relationship, GraphModel graphModel, Set nodeIdentities, Set edgeIdentities) {

        if (!edgeIdentities.contains(relationshipId(relationship))) {

            edgeIdentities.add(relationshipId(relationship));

            RelationshipModel edgeModel = new RelationshipModel();
            edgeModel.setId(relationshipId(relationship));
            edgeModel.setType(relationshipType(relationship));
            edgeModel.setStartNode(startNodeId(relationship));
            edgeModel.setEndNode(endNodeId(relationship));

            edgeModel.setProperties(convertArrayPropertiesToIterable(properties(relationship)));
            graphModel.getRelationships().add(edgeModel);
        }
    }

    public Map<String, Object> convertArrayPropertiesToIterable(Map<String, Object> properties) {
        Map<String, Object> props = new HashMap<>();
        for (String k : properties.keySet()) {
            Object v = properties.get(k);
            if (v.getClass().isArray()) {
                props.put(k, AdapterUtils.convertToIterable(v));
            } else {
                props.put(k, v);
            }
        }
        return props;
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

    public abstract Map<String,Object> properties(Object container);

    public abstract List<Object> nodesInPath(Object path);

    public abstract List<Object> relsInPath(Object path);
}
