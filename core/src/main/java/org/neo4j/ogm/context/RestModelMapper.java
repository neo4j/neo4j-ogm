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

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.PropertyContainer;
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

    public RestStatisticsModel map(Response<RestModel> response) {

        List<ResultRowBuilder> resultRowBuilders = new ArrayList<>();
        Response<GraphModel> graphModelResponse = new Response<GraphModel>() {

            @Override
            public GraphModel next() {
                RestModel model = response.next();
                if (model == null) {
                    return null;
                }

                ResultRowBuilder resultRowBuilder = new ResultRowBuilder(
                    RestModelMapper.this::getEntityOrNodeModel,
                    mappingContext::getRelationshipEntity
                );
                resultRowBuilders.add(resultRowBuilder);
                model.getRow().forEach(resultRowBuilder::handle);
                return resultRowBuilder.buildGraphModel();
            }

            @Override
            public void close() {
                response.close();
            }

            @Override
            public String[] columns() {
                return response.columns();
            }
        };

        // Run the actual mapping
        delegate.map(Object.class, graphModelResponse);
        // Recreate the original structure
        RestStatisticsModel restStatisticsModel = new RestStatisticsModel();
        response.getStatistics().ifPresent(restStatisticsModel::setStatistics);
        restStatisticsModel.setResult(resultRowBuilders.stream().map(ResultRowBuilder::finish).collect(Collectors.toList()));
        return restStatisticsModel;
    }

    /**
     * Retrieves a mapped entity from this sessions mapping context after a call to {@link GraphEntityMapper#map(Class, Response)}.
     * If there's no mapped entity, this method tries to find the {@link NodeModel} with the same id in the {@link GraphModel} which
     * has been the base of the mapping process.
     *
     * @param id         The id of the entity to retrieve
     * @param graphModel The graph model that has been used for mapping
     * @return An entity or a NodeModel
     * @throws RuntimeException When neither entity nor {@link NodeModel} can be found
     */
    private Object getEntityOrNodeModel(Long id, DefaultGraphModel graphModel) {

        return Optional.ofNullable(mappingContext.getNodeEntity(id)).orElseGet(() ->
            graphModel.findNode(id).orElseThrow(() -> new RuntimeException("Lost NodeModel for id " + id))
        );
    }

    static class ResultRowBuilder {

        /**
         * Stores all nodes extracted from result.
         */
        private final List<NodeModel> nodeModels = new ArrayList<>();

        /**
         * Stores all relationships extracted from result.
         */
        private final Map<Long, RelationshipModel> relationshipModels = new LinkedHashMap<>();

        /**
         * The graph model build from the above.
         */
        private DefaultGraphModel graphModel;

        /**
         * Used to resolve mapped entities.
         */
        private final BiFunction<Long, DefaultGraphModel, Object> resolveNodeId;

        /**
         * Used to resolved mapped relationship entities.
         */
        private final Function<Long, Object> resolveRelationshipId;

        /**
         * Contains the aliases mapped to one or more node entities.
         */
        private Map<String, List<Long>> aliasToNodeIdMapping = new HashMap<>();

        /**
         * Contains the aliases mapped to one or more relationship entities.
         */
        private Map<String, List<Long>> aliasToRelationshipIdMapping = new HashMap<>();

        /**
         * Contains the aliases mapped to lists of things.
         */
        private Set<String> aliasesOfListResults = new HashSet<>();

        /**
         * A tree pointing to lists of lists.
         */
        private Map<String, List<String>> nestedListsPointers = new TreeMap<>(Comparator.reverseOrder());

        /**
         * The result row being build.
         */
        private Map<String, Object> resultRow = new HashMap<>();

        ResultRowBuilder(BiFunction<Long, DefaultGraphModel, Object> resolveNodeId,
            Function<Long, Object> resolveRelationshipId) {
            this.resolveNodeId = resolveNodeId;
            this.resolveRelationshipId = id -> {
                Object result = resolveRelationshipId.apply(id);
                if (result == null) {
                    return relationshipModels.get(id);
                } else {
                    return result;
                }
            };
        }

        void handle(String alias, Object resultObject) {
            handle(alias, resultObject, 0, null);
        }

        /**
         * Handles one result object.
         *
         * @param alias        The alias in the result set
         * @param resultObject The result object
         */
        void handle(String alias, Object resultObject, int level, Integer index) {
            if (!canBeMapped(resultObject)) {
                // The entity mapper can only deal with models
                this.resultRow.put(alias, convertToTargetContainer(resultObject));
            } else if (resultObject instanceof List) {

                // Mark that result object as list, as it needs to be reconstructed later
                String next = alias;
                if (level > 0) { // This caters for hierarchies (levels)
                    next += ("-" + level);
                }
                if (index != null) { // This caters for siblings (index in lists of lists)
                    next += ("-" + index);
                }
                if (!next.equals(alias)) {
                    next += ("-" + ThreadLocalRandom.current().nextInt()); // Make sure we have unique generated aliases
                    nestedListsPointers.computeIfAbsent(alias, k -> new ArrayList<>()).add(next);
                    alias = next;
                    resultRow.put(alias, new ArrayList<>());
                }

                this.aliasesOfListResults.add(alias);

                int idx = 0;
                for (Object o : (List<Object>) resultObject) {
                    handle(alias, o, level + 1, idx++);
                }
            } else {
                // Just add the model as is to the graph model
                this.addToGraphModel(alias, resultObject);
            }
        }

        /**
         * Adds an individual result object to the nodes and relationship of the graph model being build
         *
         * @param alias The alias inside the original response
         * @param item  The individual object being handled.
         */
        private void addToGraphModel(String alias, Object item) {
            List<Long> ids;

            if (item instanceof NodeModel) {
                ids = aliasToNodeIdMapping.computeIfAbsent(alias, key -> new ArrayList<>());
                this.nodeModels.add((NodeModel) item);
            } else if (item instanceof RelationshipModel) {
                ids = aliasToRelationshipIdMapping.computeIfAbsent(alias, key -> new ArrayList<>());
                RelationshipModel relationshipModel = (RelationshipModel) item;
                this.relationshipModels.put(relationshipModel.getId(), relationshipModel);
            } else {
                throw new IllegalArgumentException(item + " is not a mappable object!");
            }

            // Both model types are property containers
            ids.add(((PropertyContainer) item).getId());
        }

        DefaultGraphModel buildGraphModel() {

            if (this.graphModel != null) {
                throw new IllegalStateException("GraphModel already built!");
            }

            this.graphModel = new DefaultGraphModel();
            graphModel.addNodes(nodeModels.toArray(new NodeModel[0]));
            graphModel.addRelationships(relationshipModels.values().toArray(new RelationshipModel[0]));
            return graphModel;
        }

        Map<String, Object> finish() {

            if (this.graphModel == null) {
                throw new IllegalStateException("GraphModel not built yet!");
            }
            aliasToNodeIdMapping.forEach((k, v) -> {
                if (!aliasesOfListResults.contains(k) && v.size() == 1) {
                    Object entity = resolveNodeId.apply(v.get(0), graphModel);
                    resultRow.put(k, entity);
                } else {
                    List<Object> entities = v.stream().map(id -> resolveNodeId.apply(id, graphModel)).collect(
                        Collectors.toList());
                    List<Object> entityList = (List<Object>) resultRow.computeIfAbsent(k, key -> new ArrayList<>());
                    entityList.addAll(entities);
                }
            });
            aliasToRelationshipIdMapping.forEach((k, v) -> {
                if (!aliasesOfListResults.contains(k) && v.size() == 1) {
                    Object entity = resolveRelationshipId.apply(v.get(0));
                    resultRow.put(k, entity);
                } else {
                    List<Object> entities = v.stream().map(resolveRelationshipId).collect(Collectors.toList());
                    List<Object> entityList = (List<Object>) resultRow.computeIfAbsent(k, key -> new ArrayList<>());
                    entityList.addAll(entities);
                }
            });

            // Reconstruct nested lists. The map has a stable order, lists will be constructed from the outmost to the innermost
            nestedListsPointers.forEach((k, pointers) -> {
                if (pointers.size() == 1) {
                    String pointer = pointers.get(0);
                    resultRow.put(k, Collections.singletonList(resultRow.get(pointer)));
                    resultRow.remove(pointer);
                } else {
                    List<Object> values = new ArrayList<>();
                    for (String pointer : pointers) {
                        values.add(resultRow.get(pointer));
                        resultRow.remove(pointer);
                    }
                    resultRow.put(k, Collections.unmodifiableList(values));
                }
            });

            return resultRow;
        }

        /**
         * Checks whether {@code resultObject} can be mapped. This is the case if its a {@link NodeModel node-} or
         * {@link RelationshipModel relationshipmodels} or a list of thereof.
         *
         * @param resultObject The result object to check.
         * @return True, if all elements are a model type.
         */
        private static boolean canBeMapped(Object resultObject) {

            Predicate<Object> isNodeModel = NodeModel.class::isInstance;
            Predicate<Object> isRelationshipModel = RelationshipModel.class::isInstance;
            Predicate<Object> isNodeOrRelationshipModel = isNodeModel.or(isRelationshipModel);
            Predicate<Object> isListAndEachMemberCanBeMapped = o -> o instanceof List && ((List<?>) o).stream()
                .allMatch(isNodeOrRelationshipModel);

            if (isNodeOrRelationshipModel.test(resultObject)) {
                return true;
            } else if (resultObject instanceof List) {
                List<?> listOfResultObjects = (List<?>) resultObject;
                return listOfResultObjects.size() > 0 && listOfResultObjects.stream()
                    .allMatch(isNodeOrRelationshipModel.or(isListAndEachMemberCanBeMapped).or(ResultRowBuilder::canBeMapped));
            } else {
                return false;
            }
        }

        /**
         * Converts an unmappable object into a target container. Returns the {@code resultObject} itself it the object is a single values,
         * otherwise returns an {@link Object} array if result object is a list containing a mixed set of things,
         * otherwise an array of the one elements class.
         *
         * @param in The object to convert
         * @return The original object or an array.
         */
        private static Object convertToTargetContainer(Object in) {

            if (!(in instanceof List)) {
                return in;
            }

            Class arrayClass = Void.class;
            List hlp = new ArrayList();
            for (Object element : ((List) in)) {
                Object convertedElement = convertToTargetContainer(element);
                hlp.add(convertedElement);

                Class clazz = convertedElement.getClass();
                if (arrayClass == Void.class) {
                    arrayClass = clazz;
                } else if (arrayClass != clazz) {
                    arrayClass = null;
                }
            }

            Object array;
            if (arrayClass == null) {
                array = hlp.toArray();
            } else {
                array = Array.newInstance(arrayClass, hlp.size());
                for (int i = 0; i < hlp.size(); i++) {
                    Array.set(array, i, Utils.coerceTypes(arrayClass, hlp.get(i)));
                }
            }
            return array;
        }
    }
}
