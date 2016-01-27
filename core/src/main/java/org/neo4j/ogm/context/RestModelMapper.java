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
 * @author Luanne Misquitta
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
		//TODO refactor to decouple from the REST response format
		RestStatisticsModel restStatisticsModel = new RestStatisticsModel();
		RestModel model;
		Collection<Map<String, Object>> result = new ArrayList<>();
		String[] columns = response.columns();
		Map<Long, String> relationshipEntityColumns = new HashMap<>();
		model = response.next();
		restStatisticsModel.setStatistics(model.getStats());
		while (model.getValues() != null) {
			List<RelationshipModel> relationshipModels = new ArrayList<>();
			Map<String, Object> row = new HashMap<>();
			for (int i = 0; i < columns.length; i++) {
				String column = columns[i];
				Object value = model.getValues()[i];
				if (value instanceof List) {
					List entityList = (List) value;
					//If the entities in this list aren't mappable, then just add the entity directly to the resultset
					if (isMappable(entityList)) {
						List<Object> rowVals = new ArrayList<>();
						for (Object entityObj : entityList) {
							Object mapped = map(relationshipModels, relationshipEntityColumns, column, value, entityObj);
							if (mapped != null) {
								rowVals.add(mapped);
							}
						}
						row.put(column, rowVals);
					}
					else {
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
							row.put(column,entityList.toArray());
						}
						else {
							Object array = Array.newInstance(arrayClass, entityList.size());
							for (int j = 0; j < entityList.size(); j++) {
								Array.set(array, j, Utils.coerceTypes(arrayClass, entityList.get(j)));
							}
							row.put(column, array);
						}
						//row.put(column, value);
					}
				} else {
					Object mapped = map(relationshipModels, relationshipEntityColumns, column, value, value);
					row.put(column, mapped);
				}
			}

			//Map all relationships
			DefaultGraphModel graphModel = new DefaultGraphModel();
			graphModel.setRelationships(relationshipModels.toArray(new RelationshipModel[relationshipModels.size()]));
			Map<Long, Object> relationshipEntities = graphEntityMapper.mapRelationships(graphModel);
			for (Map.Entry<Long, Object> entry : relationshipEntities.entrySet()) {
				if ((row.get(relationshipEntityColumns.get(entry.getKey())) instanceof List)) {
					((List) row.get(relationshipEntityColumns.get(entry.getKey()))).add(entry.getValue());
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

	private boolean isMappable(List entityList) {
		for (Object entityObj : entityList) {
			if (!(entityObj instanceof Map)) {
				return false;
			}
		}
		return true;
	}

	private Object map(List<RelationshipModel> relationshipModels, Map<Long, String> relationshipEntityColumns, String column, Object value, Object entityObj) {
		if (entityObj instanceof Map) {
			Map entity = (Map) entityObj;
			return mapEntity(column, relationshipModels, relationshipEntityColumns, entity);
		}
		return value;
	}

	private Object mapEntity(String column, List<RelationshipModel> relationshipModels, Map<Long, String> relationshipEntityColumns, Map entity) {
		if (entity.containsKey("metadata") && ((Map) entity.get("metadata")).get("id") != null) {
			Map entityMetadata = (Map) entity.get("metadata");
			if (entityMetadata.containsKey("labels")) {
				List<String> labelList = (List<String>) (entityMetadata.get("labels"));
				String[] labels = new String[labelList.size()];
				labels = labelList.toArray(labels);
				DefaultGraphModel graphModel = new DefaultGraphModel();
				NodeModel nodeModel = new NodeModel();
				nodeModel.setId(((Number) entityMetadata.get("id")).longValue());
				nodeModel.setProperties((Map) entity.get("data"));
				nodeModel.setLabels(labels);
				graphModel.setNodes(new NodeModel[]{nodeModel});
				List mapped = graphEntityMapper.map(metaData.resolve(labels).getUnderlyingClass(), graphModel);
				return mapped.get(0);
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
				relationshipModels.add(relationshipModel);
				if (relationshipModel.getPropertyList().size() > 0) {
					relationshipEntityColumns.put(relationshipModel.getId(), column);
				}
				return null;
			}
		}
		return entity;
	}
}
