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

package org.neo4j.ogm.context;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.model.RestModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultGraphModel;
import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.response.model.RelationshipModel;
import org.neo4j.ogm.session.Utils;

/**
 *  Map NodeModels and RelationshipModels obtained from cypher queries to domain entities
 *  @author Luanne Misquitta
 */
public class RestModelMapper implements ResponseMapper<RestModel> {

	final GraphEntityMapper graphEntityMapper;
	final MetaData metaData;

	public RestModelMapper(GraphEntityMapper graphEntityMapper, MetaData metaData) {
		this.graphEntityMapper = graphEntityMapper;
		this.metaData = metaData;
	}

	@Override
	public <T> Iterable<T> map(Class<T> type, Response<RestModel> response) {
		RestStatisticsModel restStatisticsModel = new RestStatisticsModel();
		RestModel model = response.next();
		Collection<Map<String, Object>> result = new ArrayList<>();
		Map<Long, String> relationshipEntityColumns = new HashMap<>(); //Relationship ID to column name

		restStatisticsModel.setStatistics(model.getStats());

		while (model.getRow().entrySet().size() > 0) {
			Map<String,Object> row = model.getRow();
			List<RelationshipModel> relationshipModels = new ArrayList<>();
			for (Map.Entry<String,Object> entry : row.entrySet()) {
				Object value = entry.getValue();
				if (value instanceof List) {
					List entityList = (List) value;
					if (isMappable(entityList)) {
						for (int i=0; i< entityList.size(); i++) {
							Object mapped = mapEntity(entry.getKey(), entityList.get(i), relationshipModels, relationshipEntityColumns);
							if (mapped != null) { //if null, it'll be a relationship, which we're mapping after all nodes
								entityList.set(i, mapped);
							}
						}
					}
					else {
						convertListValueToArray(entityList, entry);
					}
				}
				else {
					if (isMappable(Arrays.asList(value))) {
						Object mapped = mapEntity(entry.getKey(), value, relationshipModels, relationshipEntityColumns);
						if (mapped != null) {
							entry.setValue(mapped);
						}
					}
				}

			}
			//Map all relationships
			DefaultGraphModel graphModel = new DefaultGraphModel();
			graphModel.setRelationships(relationshipModels.toArray(new RelationshipModel[relationshipModels.size()]));
			Map<Long, Object> relationshipEntities = graphEntityMapper.mapRelationships(graphModel);
			for (Map.Entry<Long, Object> entry : relationshipEntities.entrySet()) {
				Object rels = row.get(relationshipEntityColumns.get(entry.getKey()));
				if (rels instanceof List) {
					List relsList = (List)rels;
					for (int i=0;i<relsList.size(); i++) {
						if (relsList.get(i) instanceof RelationshipModel) {
							if (((RelationshipModel) relsList.get(i)).getId().equals(entry.getKey())) {
								relsList.set(i, entry.getValue());
							}
						}
					}
				} else {
					row.put(relationshipEntityColumns.get(entry.getKey()), entry.getValue());
				}
			}


			result.add(row);
			model = response.next();
		}
		restStatisticsModel.setResult(result);
		return (Iterable<T>) Arrays.asList(restStatisticsModel);
	}

	private void convertListValueToArray(List entityList, Map.Entry<String, Object> entry) {
		Class arrayClass = null;
		for (Object element : entityList) {
			Class clazz = element.getClass();
			if (arrayClass == null) {
				arrayClass = clazz;
			}
			else {
				if (arrayClass != clazz) {
					arrayClass = null;
					break;
				}
			}
		}
		if (arrayClass == null) {
			entry.setValue(entityList.toArray());
		}
		else {
			Object array = Array.newInstance(arrayClass, entityList.size());
			for (int j = 0; j < entityList.size(); j++) {
				Array.set(array, j, Utils.coerceTypes(arrayClass, entityList.get(j)));
			}
			entry.setValue(array);
		}
	}

	private boolean isMappable(List entityList) {
		for (Object entityObj : entityList) {
			if (entityObj instanceof NodeModel || entityObj instanceof RelationshipModel) {
				continue;
			}
			return false;
		}
		return true;
	}

	private Object mapEntity(String column, Object entity, List<RelationshipModel> relationshipModels, Map<Long, String> relationshipEntityColumns) {
		if (entity instanceof NodeModel) {
			NodeModel nodeModel = (NodeModel) entity;
			DefaultGraphModel graphModel = new DefaultGraphModel();
			graphModel.setNodes(new NodeModel[]{nodeModel});
			List mapped = graphEntityMapper.map(metaData.resolve(nodeModel.getLabels()).getUnderlyingClass(), graphModel);
			return mapped.get(0);
		} else if (entity instanceof RelationshipModel) {
			RelationshipModel relationshipModel = (RelationshipModel) entity;
			relationshipModels.add(relationshipModel);
			if (relationshipModel.getPropertyList().size() > 0) {
				relationshipEntityColumns.put(relationshipModel.getId(), column);
			}
		}
		return null;
	}

}
