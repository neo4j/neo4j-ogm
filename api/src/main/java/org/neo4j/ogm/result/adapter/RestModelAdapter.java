/*
 * Copyright (c) 2002-2019 "Neo4j,"
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.response.model.RelationshipModel;

/**
 * Adapt embedded response to a NodeModels, RelationshipModels, and objects
 *
 * @author Luanne Misquitta
 */
public abstract class RestModelAdapter extends BaseAdapter
    implements ResultAdapter<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> adapt(Map<String, Object> result) {
        Map<String, Object> adaptedResults = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Collection) {
                Collection<Object> adaptedValues = new ArrayList<>();
                Collection<Object> values = (List) value;
                for (Object element : values) {
                    adaptedValues.add(processData(element));
                }
                adaptedResults.put(entry.getKey(), adaptedValues);
            } else {
                adaptedResults.put(entry.getKey(), processData(value));
            }
        }

        return adaptedResults;
    }

    private Object processData(Object element) {
        if (isNode(element)) {
            return buildNode(element);
        }
        if (isRelationship(element)) {
            return buildRelationship(element);
        }
        return element;
    }

    private NodeModel buildNode(Object node) {
        NodeModel nodeModel = new NodeModel(nodeId(node));
        List<String> labels = labels(node);
        nodeModel.setLabels(labels.toArray(new String[labels.size()]));
        nodeModel.setProperties(convertArrayPropertiesToIterable(properties(node)));
        return nodeModel;
    }

    private RelationshipModel buildRelationship(Object relationship) {
        RelationshipModel relationshipModel = new RelationshipModel();
        relationshipModel.setId(relationshipId(relationship));
        relationshipModel.setStartNode(startNodeId(relationship));
        relationshipModel.setEndNode(endNodeId(relationship));
        relationshipModel.setType(relationshipType(relationship));
        relationshipModel.setProperties(properties(relationship));
        return relationshipModel;
    }

    public abstract boolean isNode(Object value);

    public abstract boolean isRelationship(Object value);

    public abstract long nodeId(Object node);

    public abstract List<String> labels(Object node);

    public abstract long relationshipId(Object relationship);

    public abstract String relationshipType(Object relationship);

    public abstract Long startNodeId(Object relationship);

    public abstract Long endNodeId(Object relationship);

    public abstract Map<String, Object> properties(Object container);
}
