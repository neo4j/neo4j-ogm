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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.entity.io.EntityAccessManager;
import org.neo4j.ogm.entity.io.EntityAccess;
import org.neo4j.ogm.entity.io.EntityFactory;
import org.neo4j.ogm.entity.io.FieldWriter;
import org.neo4j.ogm.entity.io.PropertyReader;
import org.neo4j.ogm.entity.io.PropertyWriter;
import org.neo4j.ogm.entity.io.RelationalReader;
import org.neo4j.ogm.entity.io.RelationalWriter;
import org.neo4j.ogm.exception.BaseClassNotFoundException;
import org.neo4j.ogm.exception.MappingException;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.model.Edge;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.PropertyModel;
import org.neo4j.ogm.utils.ClassUtils;
import org.neo4j.ogm.utils.EntityUtils;
import org.neo4j.ogm.utils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class GraphEntityMapper implements ResponseMapper<GraphModel> {

	private final Logger logger = LoggerFactory.getLogger(GraphEntityMapper.class);

	private final MappingContext mappingContext;
	private final EntityFactory entityFactory;
	private final MetaData metadata;

	public GraphEntityMapper(MetaData metaData, MappingContext mappingContext) {
		this.metadata = metaData;
		this.entityFactory = new EntityFactory(metadata);
		this.mappingContext = mappingContext;
	}

    @Override
    public <T> Iterable<T> map(Class<T> type, Response<GraphModel> model) {

        List<T> objects = new ArrayList<>();
		Set<Long> objectIds = new HashSet<>();
  		/*
		 * these two lists will contain the node ids and edge ids from the response, in the order
         * they were presented to us.
         */
		Set<Long> nodeIds = new LinkedHashSet<>();
		Set<Long> edgeIds = new LinkedHashSet<>();

		GraphModel graphModel;
		while ((graphModel = model.next()) != null) {
			List<T> mappedEntities = map(type, graphModel, nodeIds, edgeIds);
			for (T entity : mappedEntities) {
				Long identity = EntityUtils.identity(entity, metadata);
				if (!objectIds.contains(identity)) {
					objects.add(entity);
					objectIds.add(identity);
				}
			}
		}

		model.close();
        return objects;
    }

	public Map<Long, Object> mapRelationships(GraphModel model) {
		Map<Long, Object> results = new HashMap<>();
		Set<Long> edgeIds = new LinkedHashSet<>();
		mapRelationships(model,edgeIds);
		for (Long id : edgeIds) {
			Object o = mappingContext.getRelationshipEntity(id);
			if (o != null) {
				results.put(id, o);
			}
		}
		return results;
	}

	public <T> List<T> map(Class<T> type, GraphModel graphModel) {
		Set<Long> nodeIds = new LinkedHashSet<>();
		Set<Long> edgeIds = new LinkedHashSet<>();
		return map(type, graphModel, nodeIds, edgeIds);
	}

	public <T> List<T> map(Class<T> type, GraphModel graphModel, Set<Long> nodeIds, Set<Long> edgeIds) {

		mapEntities(type, graphModel, nodeIds, edgeIds);
		List<T> results = new ArrayList<>();

		for (Long id : nodeIds) {
			Object o = mappingContext.getNodeEntity(id);
			if (o != null && type.isAssignableFrom(o.getClass())) {
				results.add(type.cast(o));
			}
		}

		// only look for REs if no node entities were found
		if (results.isEmpty()) {
			for (Long id : edgeIds) {
				Object o = mappingContext.getRelationshipEntity(id);
				if (o != null && type.isAssignableFrom(o.getClass())) {
					results.add(type.cast(o));
				}
			}
		}

		return results;
	}

	private <T> void mapEntities(Class<T> type, GraphModel graphModel, Set<Long> nodeIds, Set<Long> edgeIds) {
		try {
			mapNodes(graphModel, nodeIds);
			mapRelationships(graphModel, edgeIds);
		} catch (Exception e) {
			throw new MappingException("Error mapping GraphModel to instance of " + type.getName(), e);
		}
	}

	private void mapNodes(GraphModel graphModel, Set<Long> nodeIds) {

		for (Node node : graphModel.getNodes()) {
			if (!nodeIds.contains(node.getId())) {
				Object entity = mappingContext.getNodeEntity(node.getId());
				try {
					if (entity == null) {
						synchronized (this) {
							entity = entityFactory.newObject(node);
							setIdentity(entity, node.getId());
							setProperties(node, entity);
							setLabels(node, entity);
						}
					}
					mappingContext.addNodeEntity(entity, node.getId());

					nodeIds.add(node.getId());

				} catch (BaseClassNotFoundException e) {
					logger.debug(e.getMessage());
				}
			}
		}
	}

	private void setIdentity(Object instance, Long id) {
		ClassInfo classInfo = metadata.classInfo(instance);
		FieldInfo fieldInfo = classInfo.identityField();
		FieldWriter.write(classInfo.getField(fieldInfo), instance, id);
	}

    private void setProperties(Node nodeModel, Object instance) {
        List<Property<String, Object>> propertyList = nodeModel.getPropertyList();
        ClassInfo classInfo = metadata.classInfo(instance);

        Collection<FieldInfo> compositeFields = classInfo.fieldsInfo().compositeFields();
        if (compositeFields.size() > 0) {
            Map<String, ?> propertyMap = PropertyUtils.toMap(propertyList);
            for (FieldInfo field : compositeFields) {
                Object value = field.getCompositeConverter().toEntityAttribute(propertyMap);
                PropertyWriter writer = EntityAccessManager.getPropertyWriter(classInfo, field.getName());
                writer.write(instance, value);
            }
        }

        for (Property<?, ?> property : propertyList) {
            writeProperty(classInfo, instance, property);
        }
    }

	private void setProperties(Edge relationshipModel, Object instance) {
		ClassInfo classInfo = metadata.classInfo(instance);
        for (Property<?, ?> property : relationshipModel.getPropertyList()) {
            writeProperty(classInfo, instance, property);
        }
	}

	private void setLabels(Node nodeModel, Object instance) {
		ClassInfo classInfo = metadata.classInfo(instance);
		FieldInfo labelFieldInfo = classInfo.labelFieldOrNull();
		if (labelFieldInfo != null) {
			Collection<String> staticLabels = classInfo.staticLabels();
			Set<String> dynamicLabels = new HashSet<>();
			for (String label : nodeModel.getLabels()) {
				if (!staticLabels.contains(label)) {
					dynamicLabels.add(label);
				}
			}
			writeProperty(classInfo, instance, PropertyModel.with(labelFieldInfo.getName(), dynamicLabels));
		}
	}

	private void writeProperty(ClassInfo classInfo, Object instance, Property<?, ?> property) {

		PropertyWriter writer = EntityAccessManager.getPropertyWriter(classInfo, property.getKey().toString());

		if (writer == null) {
			logger.debug("Unable to find property: {} on class: {} for writing", property.getKey(), classInfo.name());
		} else {
			Object value = property.getValue();
			// merge iterable / arrays and co-erce to the correct attribute type
			if (writer.type().isArray() || Iterable.class.isAssignableFrom(writer.type())) {
				PropertyReader reader = EntityAccessManager.getPropertyReader(classInfo, property.getKey().toString());
				if (reader != null) {
					Object currentValue = reader.readProperty(instance);
					Class<?> paramType = writer.type();
					Class elementType =  underlyingElementType(classInfo, property.getKey().toString());
					if (paramType.isArray()) {
						value = EntityAccess.merge(paramType, value, (Object[]) currentValue, elementType);
					} else {
						value = EntityAccess.merge(paramType, value, (Collection) currentValue, elementType);
					}
				}
			}
			writer.write(instance, value);
		}
	}

	private boolean tryMappingAsSingleton(Object source, Object parameter, Edge edge, String relationshipDirection) {

		String edgeLabel = edge.getType();
		ClassInfo sourceInfo = metadata.classInfo(source);

		RelationalWriter writer = EntityAccessManager.getRelationalWriter(sourceInfo, edgeLabel, relationshipDirection, parameter);
		if (writer != null && writer.forScalar()) {
			writer.write(source, parameter);
			return true;
		}

		return false;
	}

	private void mapRelationships(GraphModel graphModel, Set<Long> edgeIds) {

		final List<Edge> oneToMany = new ArrayList<>();

		for (Edge edge : graphModel.getRelationships()) {
			if (!edgeIds.contains(edge.getId())) {
				Object source = mappingContext.getNodeEntity(edge.getStartNode());
				Object target = mappingContext.getNodeEntity(edge.getEndNode());

				edgeIds.add(edge.getId());

				if (source != null && target != null) {
					// check whether this edge should in fact be handled as a relationship entity
					ClassInfo relationshipEntityClassInfo = getRelationshipEntity(edge);

					if (relationshipEntityClassInfo != null) {
						mapRelationshipEntity(oneToMany, edge, source, target, relationshipEntityClassInfo);
					} else {
						mapRelationship(oneToMany, edge, source, target);
					}
				} else {
					logger.debug("Relationship {} cannot be hydrated because one or more required node types are not mapped to entity classes", edge);
				}
			}
		}
		if (oneToMany.size() > 0) {
			mapOneToMany(oneToMany);
		}
	}

	private void mapRelationship(List<Edge> oneToMany, Edge edge, Object source, Object target) {
		boolean oneToOne;
		//Since source=start node, end=end node, the direction from source->target has to be outgoing, try mapping it
		oneToOne = tryMappingAsSingleton(source, target, edge, Relationship.OUTGOING);

		//Try mapping the incoming relation on the end node, target
		oneToOne &= tryMappingAsSingleton(target, source, edge, Relationship.INCOMING);

        // if its not one->one on BOTH sides, we'll try a one->many | many->one | many->many mapping later.
        if (!oneToOne) {
			oneToMany.add(edge);
		} else {
			RelationalWriter writer = EntityAccessManager.getRelationalWriter(metadata.classInfo(source), edge.getType(), Relationship.OUTGOING, target);
			mappingContext.addRelationship(new MappedRelationship(edge.getStartNode(), edge.getType(), edge.getEndNode(), edge.getId(), source.getClass(), ClassUtils.getType(writer.typeParameterDescriptor())));
		}
	}

	private void mapRelationshipEntity(List<Edge> oneToMany, Edge edge, Object source, Object target, ClassInfo relationshipEntityClassInfo) {
		logger.debug("Found relationship type: {} to map to RelationshipEntity: {}", edge.getType(), relationshipEntityClassInfo.name());

		// look to see if this relationship already exists in the mapping context.
		Object relationshipEntity = mappingContext.getRelationshipEntity(edge.getId());

		// do we know about it?
		if (relationshipEntity == null) { // no, create a new relationship entity
			relationshipEntity = createRelationshipEntity(edge, source, target);
		}

		// If the source has a writer for an outgoing relationship for the rel entity, then write the rel entity on the source if it's a scalar writer
		ClassInfo sourceInfo = metadata.classInfo(source);
		RelationalWriter writer = EntityAccessManager.getRelationalWriter(sourceInfo, edge.getType(), Relationship.OUTGOING, relationshipEntity);
		if (writer == null) {
			logger.debug("No writer for {}", target);
		} else {
			if (writer.forScalar()) {
				writer.write(source, relationshipEntity);
				mappingContext.addRelationship(new MappedRelationship(edge.getStartNode(), edge.getType(), edge.getEndNode(), edge.getId(), source.getClass(), ClassUtils.getType(writer.typeParameterDescriptor())));
			} else {
				oneToMany.add(edge);
			}
		}

		//If the target has a writer for an incoming relationship for the rel entity, then write the rel entity on the target if it's a scalar writer
		ClassInfo targetInfo = metadata.classInfo(target);
		writer = EntityAccessManager.getRelationalWriter(targetInfo, edge.getType(), Relationship.INCOMING, relationshipEntity);

		if (writer == null) {
			logger.debug("No writer for {}", target);
		} else {
			if (writer.forScalar()) {
				writer.write(target, relationshipEntity);
			} else {
				oneToMany.add(edge);
			}
		}
	}

	private Object createRelationshipEntity(Edge edge, Object startEntity, Object endEntity) {

		// create and hydrate the new RE
		Object relationshipEntity = entityFactory.newObject(getRelationshipEntity(edge));
		setIdentity(relationshipEntity, edge.getId());

		// REs also have properties
		setProperties(edge, relationshipEntity);

		// register it in the mapping context
		mappingContext.addRelationshipEntity(relationshipEntity, edge.getId());

		// set the start and end entities
		ClassInfo relEntityInfo = metadata.classInfo(relationshipEntity);

		RelationalWriter startNodeWriter = EntityAccessManager.getRelationalEntityWriter(relEntityInfo, StartNode.class);
		if (startNodeWriter != null) {
			startNodeWriter.write(relationshipEntity, startEntity);
		} else {
			throw new RuntimeException("Cannot find a writer for the StartNode of relational entity " + relEntityInfo.name());
		}

		RelationalWriter endNodeWriter = EntityAccessManager.getRelationalEntityWriter(relEntityInfo, EndNode.class);
		if (endNodeWriter != null) {
			endNodeWriter.write(relationshipEntity, endEntity);
		} else {
			throw new RuntimeException("Cannot find a writer for the EndNode of relational entity " + relEntityInfo.name());
		}

		return relationshipEntity;
	}

	private void mapOneToMany(Collection<Edge> oneToManyRelationships) {

		EntityCollector entityCollector = new EntityCollector();
		List<MappedRelationship> relationshipsToRegister = new ArrayList<>();
        Set<Edge> registeredEdges = new HashSet<>();

		// first, build the full set of related entities of each type and direction for each source entity in the relationship
		for (Edge edge : oneToManyRelationships) {

			Object instance = mappingContext.getNodeEntity(edge.getStartNode());
			Object parameter = mappingContext.getNodeEntity(edge.getEndNode());

			// is this a relationship entity we're trying to map?
			Object relationshipEntity = mappingContext.getRelationshipEntity(edge.getId());
			if (relationshipEntity != null) {
				// establish a relationship between
				RelationalWriter outgoingWriter = findIterableWriter(instance, relationshipEntity, edge.getType(), Relationship.OUTGOING);
				if (outgoingWriter!=null) {
					entityCollector.recordTypeRelationship(edge.getStartNode(), relationshipEntity, edge.getType(), Relationship.OUTGOING);
					relationshipsToRegister.add(new MappedRelationship(edge.getStartNode(), edge.getType(), edge.getEndNode(), edge.getId(), instance.getClass(), ClassUtils.getType(outgoingWriter.typeParameterDescriptor())));
				}
				RelationalWriter incomingWriter = findIterableWriter(parameter, relationshipEntity, edge.getType(), Relationship.INCOMING);
				if (incomingWriter!=null) {
					entityCollector.recordTypeRelationship(edge.getEndNode(), relationshipEntity, edge.getType(), Relationship.INCOMING);
					relationshipsToRegister.add(new MappedRelationship(edge.getStartNode(), edge.getType(), edge.getEndNode(), edge.getId(), instance.getClass(), ClassUtils.getType(incomingWriter.typeParameterDescriptor())));
				}
                if (incomingWriter != null || outgoingWriter != null) {
                    registeredEdges.add(edge) ;
                }
			} else {
				RelationalWriter outgoingWriter = findIterableWriter(instance, parameter, edge.getType(), Relationship.OUTGOING);
				if (outgoingWriter!=null) {
					entityCollector.recordTypeRelationship(edge.getStartNode(), parameter, edge.getType(), Relationship.OUTGOING);
					relationshipsToRegister.add(new MappedRelationship(edge.getStartNode(), edge.getType(), edge.getEndNode(), edge.getId(), instance.getClass(), ClassUtils.getType(outgoingWriter.typeParameterDescriptor())));
				}
				RelationalWriter incomingWriter = findIterableWriter(parameter, instance, edge.getType(), Relationship.INCOMING);
				if (incomingWriter!=null) {
					entityCollector.recordTypeRelationship(edge.getEndNode(), instance, edge.getType(), Relationship.INCOMING);
					relationshipsToRegister.add(new MappedRelationship(edge.getStartNode(), edge.getType(), edge.getEndNode(), edge.getId(), instance.getClass(), ClassUtils.getType(incomingWriter.typeParameterDescriptor())));

				}
                if (incomingWriter != null || outgoingWriter != null) {
                    registeredEdges.add(edge) ;
                }
			}
		}

		// then set the entire collection at the same time for each owning type
		for (Long instanceId : entityCollector.getOwningTypes()) {
			//get all relationship types for which we're trying to set collections of instances
			for (String relationshipType : entityCollector.getOwningRelationshipTypes(instanceId)) {
				//for each relationship type, get all the directions for which we're trying to set collections of instances
				for (String relationshipDirection : entityCollector.getRelationshipDirectionsForOwningTypeAndRelationshipType(instanceId, relationshipType)) {
					//for each direction, get all the entity types for which we're trying to set collections of instances
					for (Class entityClass : entityCollector.getEntityClassesForOwningTypeAndRelationshipTypeAndRelationshipDirection(instanceId, relationshipType, relationshipDirection)) {
						Collection<?> entities = entityCollector.getCollectiblesForOwnerAndRelationship(instanceId, relationshipType, relationshipDirection, entityClass);
						//Class entityType = entityCollector.getCollectibleTypeForOwnerAndRelationship(instanceId, relationshipType, relationshipDirection);
						mapOneToMany(mappingContext.getNodeEntity(instanceId), entityClass, entities, relationshipType, relationshipDirection);
					}
				}
			}
		}

		// now register all the relationships we've mapped as iterable types into the mapping context
		for (MappedRelationship mappedRelationship : relationshipsToRegister) {
			mappingContext.addRelationship(mappedRelationship);
		}
        // finally, register anything left over. These will be singleton relationships that
        // were not mapped during one->one mapping, or one->many mapping.
        for (Edge edge : oneToManyRelationships) {
            if (!registeredEdges.contains(edge)) {
                Object source = mappingContext.getNodeEntity(edge.getStartNode());
                Object target = mappingContext.getNodeEntity(edge.getEndNode());
                RelationalWriter writer = EntityAccessManager.getRelationalWriter(metadata.classInfo(source), edge.getType(), Relationship.OUTGOING, target);
                // ensures its tracked in the domain
                if (writer != null) {
                    MappedRelationship mappedRelationship = new MappedRelationship(edge.getStartNode(), edge.getType(), edge.getEndNode(), edge.getId(), source.getClass(), ClassUtils.getType(writer.typeParameterDescriptor()));
                    if (!mappingContext.containsRelationship(mappedRelationship)) {
                        mappingContext.addRelationship(mappedRelationship);
                    }
                }
            }
        }
	}

	/**
	 * Return an iterable writer to map a relationship onto an entity for the given relationshipType and relationshipDirection
	 * @param instance the instance onto which the relationship is to be mapped
	 * @param parameter the value to be mapped
	 * @param relationshipType the relationship type
	 * @param relationshipDirection the relationship direction
	 * @return RelationalWriter or null if none exists
	 */
	private RelationalWriter findIterableWriter(Object instance, Object parameter, String relationshipType, String relationshipDirection) {
		ClassInfo classInfo = metadata.classInfo(instance);
		return EntityAccessManager.getIterableWriter(classInfo, parameter.getClass(), relationshipType, relationshipDirection);
	}


	/**
	 * Map many values to an instance based on the relationship type.
	 * See DATAGRAPH-637
	 *
	 * @param instance the instance to map values onto
	 * @param valueType the type of each value
	 * @param values the values to map
	 * @param relationshipType the relationship type associated with these values
	 */
	private void mapOneToMany(Object instance, Class<?> valueType, Object values, String relationshipType, String relationshipDirection) {

		ClassInfo classInfo = metadata.classInfo(instance);

		RelationalWriter writer = EntityAccessManager.getIterableWriter(classInfo, valueType, relationshipType, relationshipDirection);
		if (writer != null) {
			if (writer.type().isArray() || Iterable.class.isAssignableFrom(writer.type())) {
				RelationalReader reader = EntityAccessManager.getIterableReader(classInfo, valueType, relationshipType, relationshipDirection);
				Object currentValues;
				if (reader != null) {
					currentValues = reader.read(instance);
					if (writer.type().isArray()) {
						values = EntityAccess.merge(writer.type(), values, (Object[]) currentValues, valueType);
					} else {
						values = EntityAccess.merge(writer.type(), values, (Collection) currentValues, valueType);
					}
				}
			}
			writer.write(instance, values);
			return;
		}
		// this is not necessarily an error. but we can't tell.
		logger.debug("Unable to map iterable of type: {} onto property of {}", valueType, classInfo.name());
	}

	private ClassInfo getRelationshipEntity(Edge edge) {
		for (ClassInfo classInfo : metadata.classInfoByLabelOrType(edge.getType())) {
			if (classInfo != null) {
				//verify the start and end node types because there might be more than one RE defined with the same type and direction but different start/end nodes
				Object source = mappingContext.getNodeEntity(edge.getStartNode());
				Object target = mappingContext.getNodeEntity(edge.getEndNode());
				if (nodeTypeMatches(classInfo, source, StartNode.class.getName()) &&
						nodeTypeMatches(classInfo, target, EndNode.class.getName())) {
					return classInfo;
				}
			}
		}
		return null;
	}

	/**
	 * Checks that the class of the node matches the class defined in the class info for a given annotation
	 * @param classInfo the ClassInfo
	 * @param node the node object
	 * @param annotation the annotation to match
	 * @return true if the class of the node matches field annotated
	 */
	private boolean nodeTypeMatches(ClassInfo classInfo, Object node, String annotation) {
		List<FieldInfo> fields = classInfo.findFields(annotation);
		if(fields.size() == 1) {
			FieldInfo field = fields.get(0);
			if(field.isTypeOf(node.getClass())) {
				return true;
			}
		}
		return false;
	}

	private Class underlyingElementType(ClassInfo classInfo, String propertyName) {
		FieldInfo fieldInfo = fieldInfoForPropertyName(propertyName, classInfo);
		Class clazz = null;
		if (fieldInfo != null) {
			if (fieldInfo.getTypeDescriptor() != null) {
				clazz = ClassUtils.getType(fieldInfo.getTypeDescriptor());
			}
			if (clazz == null && fieldInfo.getTypeParameterDescriptor() != null) {
				clazz = ClassUtils.getType(fieldInfo.getTypeParameterDescriptor());
			}
			if (clazz == null && fieldInfo.getDescriptor() != null) {
				clazz = ClassUtils.getType(fieldInfo.getDescriptor());
			}
		}
		return clazz;
	}

	private FieldInfo fieldInfoForPropertyName(String propertyName, ClassInfo classInfo) {
		FieldInfo labelField = classInfo.labelFieldOrNull();
		if (labelField != null && labelField.getName().toLowerCase().equals(propertyName.toLowerCase())) {
			return labelField;
		}
		return classInfo.propertyField(propertyName);
	}

}
