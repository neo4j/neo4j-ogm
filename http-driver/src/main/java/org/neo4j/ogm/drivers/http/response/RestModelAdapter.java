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
package org.neo4j.ogm.drivers.http.response;

import static org.neo4j.ogm.result.adapter.RestModelAdapter.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.response.model.RelationshipModel;
import org.neo4j.ogm.result.adapter.ResultAdapter;

/**
 * Adapt HTTP rest response to a NodeModels, RelationshipModels, and objects
 *
 * @author Luanne Misquitta
 * @author Michael J. Simons
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
                adaptedResults.put(column, ((Collection) value).stream().map(this::processData).collect(
                    Collectors.toList()));
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

        return handlePossibleCollections(element, this::processData);
    }

    private Object buildEntity(Map entity) {
        if (entity.containsKey("metadata") && ((Map) entity.get("metadata")).get("id") != null) {
            Map entityMetadata = (Map) entity.get("metadata");
            if (entityMetadata.containsKey("labels")) {
                List<String> labelList = (List<String>) (entityMetadata.get("labels"));
                String[] labels = new String[labelList.size()];
                labels = labelList.toArray(labels);
                NodeModel nodeModel = new NodeModel(((Number) entityMetadata.get("id")).longValue());
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
