/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.drivers.http.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.response.model.RelationshipModel;
import org.neo4j.ogm.result.adapter.ResultAdapter;

/**
 * Adapt HTTP rest response to a NodeModels, RelaitonshipModels, and objects
 *
 * @author Luanne Misquitta
 */
public class RestModelAdapter implements ResultAdapter<Object[], Map<String, Object>> {

    private String[] columns;

    @Override
    public Map<String, Object> adapt(Object[] result) {
        Map<String, Object> adaptedResults = new LinkedHashMap<>();
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            Object value = result[i];
            if (value instanceof Collection) {
                List<Object> adaptedValues = new ArrayList<>();
                List<Object> values = (List) value;
                for (Object element : values) {
                    adaptedValues.add(processData(element));
                }
                adaptedResults.put(column, adaptedValues);
            } else {
                adaptedResults.put(column, processData(value));
            }
        }
        return adaptedResults;
    }

    private Object processData(Object element) {
        if (element instanceof Map) {
            return buildEntity((Map) element);
        }
        return element;
    }

    private Object buildEntity(Map entity) {
        if (entity.containsKey("metadata") && ((Map) entity.get("metadata")).get("id") != null) {
            Map entityMetadata = (Map) entity.get("metadata");
            if (entityMetadata.containsKey("labels")) {
                List<String> labelList = (List<String>) (entityMetadata.get("labels"));
                String[] labels = new String[labelList.size()];
                labels = labelList.toArray(labels);
                NodeModel nodeModel = new NodeModel();
                nodeModel.setId(((Number) entityMetadata.get("id")).longValue());
                nodeModel.setProperties((Map) entity.get("data"));
                nodeModel.setLabels(labels);
                return nodeModel;
            } else if (entityMetadata.containsKey("type")) {
                String relationshipType = (String) entityMetadata.get("type");
                RelationshipModel relationshipModel = new RelationshipModel();
                relationshipModel.setId(((Number) entityMetadata.get("id")).longValue());
                relationshipModel.setProperties((Map) entity.get("data"));
                relationshipModel.setType(relationshipType);
                String startURL = (String) entity.get("start");
                String endURL = (String) entity.get("end");
                relationshipModel.setStartNode(Long.valueOf(startURL.substring(startURL.lastIndexOf("/") + 1)));
                relationshipModel.setEndNode(Long.valueOf(endURL.substring(endURL.lastIndexOf("/") + 1)));
                return relationshipModel;
            }
        }
        return entity;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }
}
