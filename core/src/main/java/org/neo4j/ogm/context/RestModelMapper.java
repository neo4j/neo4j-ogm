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
package org.neo4j.ogm.context;

import static java.util.stream.Collectors.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.RestModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultGraphModel;
import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.response.model.RelationshipModel;
import org.neo4j.ogm.session.EntityInstantiator;
import org.neo4j.ogm.session.Utils;

/**
 * Map NodeModels and RelationshipModels obtained from cypher queries to domain entities
 *
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class RestModelMapper {

    private final MappingContext mappingContext;
    private final GraphEntityMapper delegate;

    public RestModelMapper(MetaData metaData, MappingContext mappingContext,
        EntityInstantiator entityInstantiator) {
        this.mappingContext = mappingContext;

        this.delegate = new GraphEntityMapper(metaData, mappingContext, entityInstantiator);
    }

    static class ResultRowBuilder {

        private final Function<Long, Object> resolveNodeId;
        private final Function<Long, Object> resolveRelationshipId;

        Map<String, List<Long>> aliasToIdMapping1 = new HashMap<>();
        Map<String, List<Long>> aliasToIdMapping2 = new HashMap<>();
        Set<String> listEntries1 = new HashSet<>();
        Map<String, Object> resultRow = new HashMap<>();

        ResultRowBuilder(Function<Long, Object> resolveNodeId,
            Function<Long, Object> resolveRelationshipId) {
            this.resolveNodeId = resolveNodeId;
            this.resolveRelationshipId = resolveRelationshipId;
        }

        Map<String, Object> finish() {

            aliasToIdMapping1.forEach((k, v) -> {
                Object entity;
                if (!listEntries1.contains(k) && v.size() == 1) {
                    entity = resolveNodeId.apply(v.get(0));
                } else {
                    entity = v.stream().map(resolveNodeId).collect(toList());
                }
                resultRow.put(k, entity);
            });
            aliasToIdMapping2.forEach((k, v) -> {
                Object entity;
                if (!listEntries1.contains(k) && v.size() == 1) {
                    entity = resolveRelationshipId.apply(v.get(0));
                } else {
                    entity = v.stream().map(resolveRelationshipId).collect(toList());
                }
                resultRow.put(k, entity);
            });

            return resultRow;
        }
    }

    public RestStatisticsModel map(Response<RestModel> response) {

        List<GraphModel> graphModels = new ArrayList<>();
        List<ResultRowBuilder> resultRowBuilders = new ArrayList<>();

        // TODO WiP code, runs fine, needs polishing ;)
        RestModel model = null;
        while ((model = response.next()) != null) {
            List<NodeModel> nodes = new ArrayList<>();
            List<RelationshipModel> relationships = new ArrayList<>();

            DefaultGraphModel graphModel = new DefaultGraphModel();
            ResultRowBuilder resultRowBuilder = new ResultRowBuilder(
                id -> getEntityOrNodeModel(id, graphModel),
                mappingContext::getRelationshipEntity
            );

            model.getRow().forEach((key, rawValue) -> {
                List values;

                List<Long> idsOfAlias1 = new ArrayList<>();
                List<Long> idsOfAlias2 = new ArrayList<>();

                if (rawValue instanceof List) {
                    resultRowBuilder.listEntries1.add(key);
                    values = (List) rawValue;
                    if (!allElementsAreMappable(values)) {
                        resultRowBuilder.resultRow.put(key, convertListValueToArray(values));
                        values = Collections.emptyList();
                    }
                } else {
                    values = Collections.singletonList(rawValue);
                }

                for (Object thing : values) {
                    if (thing instanceof NodeModel) {
                        idsOfAlias1.add(((NodeModel) thing).getId());
                        nodes.add((NodeModel) thing);
                    } else if (thing instanceof RelationshipModel) {
                        idsOfAlias2.add(((RelationshipModel) thing).getId());
                        relationships.add((RelationshipModel) thing);
                    } else {
                        resultRowBuilder.resultRow.put(key, rawValue);
                    }
                }

                graphModel.setNodes(nodes.toArray(new NodeModel[0]));
                graphModel.setRelationships(relationships.toArray(new RelationshipModel[0]));

                if (!idsOfAlias1.isEmpty()) {
                    resultRowBuilder.aliasToIdMapping1.put(key, idsOfAlias1);
                }
                if (!idsOfAlias2.isEmpty()) {
                    resultRowBuilder.aliasToIdMapping2.put(key, idsOfAlias2);
                }
            });

            graphModels.add(graphModel);
            resultRowBuilders.add(resultRowBuilder);
        }

        delegate.map(Object.class, graphModels);

        RestStatisticsModel restStatisticsModel = new RestStatisticsModel();
        response.getStatistics().ifPresent(restStatisticsModel::setStatistics);
        restStatisticsModel.setResult(resultRowBuilders.stream().map(ResultRowBuilder::finish).collect(toList()));
        return restStatisticsModel;
    }


    private Object getEntityOrNodeModel(Long id, DefaultGraphModel graphModel) {

        return Optional.ofNullable(mappingContext.getNodeEntity(id)).orElseGet(() ->
            graphModel.findNode(id).orElseThrow(() -> new RuntimeException("Lost NodeModel for id " + id))
        );
    }

    private static Object convertListValueToArray(List entityList) {
        Class arrayClass = null;
        for (Object element : entityList) {
            Class clazz = element.getClass();
            if (arrayClass == null) {
                arrayClass = clazz;
            } else if (arrayClass != clazz) {
                arrayClass = null;
                break;
            }
        }

        Object array;
        if (arrayClass == null) {
            array = entityList.toArray();
        } else {
            array = Array.newInstance(arrayClass, entityList.size());
            for (int j = 0; j < entityList.size(); j++) {
                Array.set(array, j, Utils.coerceTypes(arrayClass, entityList.get(j)));
            }
        }
        return array;
    }

    private static boolean allElementsAreMappable(List entityList) {

        Predicate<Object> isNodeModel = NodeModel.class::isInstance;
        Predicate<Object> isRelationshipModel = RelationshipModel.class::isInstance;
        return entityList.size() > 0 && entityList.stream().allMatch(isNodeModel.or(isRelationshipModel));
    }
}
