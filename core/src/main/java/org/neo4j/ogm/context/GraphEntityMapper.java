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


import static org.neo4j.ogm.annotation.Relationship.*;
import static org.neo4j.ogm.metadata.reflect.EntityAccessManager.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.exception.BaseClassNotFoundException;
import org.neo4j.ogm.exception.MappingException;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.MethodInfo;
import org.neo4j.ogm.metadata.reflect.*;
import org.neo4j.ogm.model.Edge;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.PropertyModel;
import org.neo4j.ogm.utils.ClassUtils;
import org.neo4j.ogm.utils.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class GraphEntityMapper implements ResponseMapper<GraphModel> {

    private static final Logger logger = LoggerFactory.getLogger(GraphEntityMapper.class);

    private static Map<String, ?> toMap(List<Property<String, Object>> propertyList) {

        Map map = new HashMap();

        for (Property<String, Object> property : propertyList) {
            map.put(property.getKey(), property.getValue());
        }

        return map;
    }

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
        mapRelationships(model, edgeIds);
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

            Set<Long> seenNodeIds = new HashSet<>();
            for (Long nodeId: nodeIds) {
                if (!seenNodeIds.contains(nodeId)) {
                    Object entity = mappingContext.getNodeEntity(nodeId);
                    executePostLoad(entity);
                    seenNodeIds.add(nodeId);
                }
            }

            Set<Long> seenEdgeIds = new HashSet<>();
            for (Long edgeId: edgeIds) {
                if (!seenEdgeIds.contains(edgeId)) {
                    Object entity = mappingContext.getRelationshipEntity(edgeId);
                    if (entity != null) {
                        executePostLoad(entity);
                    }
                    seenNodeIds.add(edgeId);
                }
            }

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
                        entity = entityFactory.newObject(node);
                        setIdentity(entity, node.getId());
                        setProperties(node, entity);
                        setLabels(node, entity);
                        mappingContext.addNodeEntity(entity, node.getId());
                    }
                    nodeIds.add(node.getId());
                } catch (BaseClassNotFoundException e) {
                    logger.debug(e.getMessage());
                }
            }
        }
    }

    private void executePostLoad(Object instance) {
        ClassInfo classInfo = metadata.classInfo(instance);
        MethodInfo postLoadMethod = classInfo.postLoadMethodOrNull();
        if (postLoadMethod != null) {
            final Method method = classInfo.getMethod(postLoadMethod);
            try {
                method.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error("Failed to execute post load method", e);
            }
        }
    }

    private void setIdentity(Object instance, Long id) {
        ClassInfo classInfo = metadata.classInfo(instance);
        FieldInfo fieldInfo = classInfo.identityField();
        FieldInfo.write(classInfo.getField(fieldInfo), instance, id);
    }

    private void setProperties(Node nodeModel, Object instance) {
        List<Property<String, Object>> propertyList = nodeModel.getPropertyList();
        ClassInfo classInfo = metadata.classInfo(instance);

        Collection<FieldInfo> compositeFields = classInfo.fieldsInfo().compositeFields();
        if (compositeFields.size() > 0) {
            Map<String, ?> propertyMap = toMap(propertyList);
            for (FieldInfo field : compositeFields) {
                Object value = field.getCompositeConverter().toEntityAttribute(propertyMap);
                FieldInfo writer = classInfo.getFieldInfo(field.getName());
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

        FieldInfo writer = classInfo.getFieldInfo(property.getKey().toString());

        if (writer == null) {
            logger.debug("Unable to find property: {} on class: {} for writing", property.getKey(), classInfo.name());
        } else {
            Object value = property.getValue();
            // merge iterable / arrays and co-erce to the correct attribute type
            if (writer.type().isArray() || Iterable.class.isAssignableFrom(writer.type())) {
                FieldInfo reader = classInfo.getFieldInfo(property.getKey().toString());
                if (reader != null) {
                    Object currentValue = reader.readProperty(instance);
                    Class<?> paramType = writer.type();
                    Class elementType = underlyingElementType(classInfo, property.getKey().toString());
                    if (paramType.isArray()) {
                        value = EntityAccessManager.merge(paramType, value, (Object[]) currentValue, elementType);
                    } else {
                        value = EntityAccessManager.merge(paramType, value, (Collection) currentValue, elementType);
                    }
                }
            }
            writer.write(instance, value);
        }
    }

    private boolean tryMappingAsSingleton(Object source, Object parameter, Edge edge, String relationshipDirection) {

        String edgeLabel = edge.getType();
        ClassInfo sourceInfo = metadata.classInfo(source);

        FieldInfo writer = getRelationalWriter(sourceInfo, edgeLabel, relationshipDirection, parameter);
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
        oneToOne = tryMappingAsSingleton(source, target, edge, OUTGOING);

        //Try mapping the incoming relation on the end node, target
        oneToOne &= tryMappingAsSingleton(target, source, edge, Relationship.INCOMING);

        // if its not one->one on BOTH sides, we'll try a one->many | many->one | many->many mapping later.
        if (!oneToOne) {
            oneToMany.add(edge);
        } else {
            FieldInfo writer = getRelationalWriter(metadata.classInfo(source), edge.getType(), OUTGOING, target);
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
        FieldInfo writer = getRelationalWriter(sourceInfo, edge.getType(), OUTGOING, relationshipEntity);
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
        writer = getRelationalWriter(targetInfo, edge.getType(), Relationship.INCOMING, relationshipEntity);

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

        FieldInfo startNodeWriter = relEntityInfo.getStartNodeReader();
        if (startNodeWriter != null) {
            startNodeWriter.write(relationshipEntity, startEntity);
        } else {
            throw new RuntimeException("Cannot find a writer for the StartNode of relational entity " + relEntityInfo.name());
        }

        FieldInfo endNodeWriter = relEntityInfo.getEndNodeReader();
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
                FieldInfo outgoingWriter = findIterableWriter(instance, relationshipEntity, edge.getType(), OUTGOING);
                if (outgoingWriter != null) {
                    entityCollector.recordTypeRelationship(edge.getStartNode(), relationshipEntity, edge.getType(), OUTGOING);
                    relationshipsToRegister.add(new MappedRelationship(edge.getStartNode(), edge.getType(), edge.getEndNode(), edge.getId(), instance.getClass(), ClassUtils.getType(outgoingWriter.typeParameterDescriptor())));
                }
                FieldInfo incomingWriter = findIterableWriter(parameter, relationshipEntity, edge.getType(), Relationship.INCOMING);
                if (incomingWriter != null) {
                    entityCollector.recordTypeRelationship(edge.getEndNode(), relationshipEntity, edge.getType(), Relationship.INCOMING);
                    relationshipsToRegister.add(new MappedRelationship(edge.getStartNode(), edge.getType(), edge.getEndNode(), edge.getId(), instance.getClass(), ClassUtils.getType(incomingWriter.typeParameterDescriptor())));
                }
                if (incomingWriter != null || outgoingWriter != null) {
                    registeredEdges.add(edge);
                }
            } else {
                FieldInfo outgoingWriter = findIterableWriter(instance, parameter, edge.getType(), OUTGOING);
                if (outgoingWriter != null) {
                    entityCollector.recordTypeRelationship(edge.getStartNode(), parameter, edge.getType(), OUTGOING);
                    relationshipsToRegister.add(new MappedRelationship(edge.getStartNode(), edge.getType(), edge.getEndNode(), edge.getId(), instance.getClass(), ClassUtils.getType(outgoingWriter.typeParameterDescriptor())));
                }
                FieldInfo incomingWriter = findIterableWriter(parameter, instance, edge.getType(), Relationship.INCOMING);
                if (incomingWriter != null) {
                    entityCollector.recordTypeRelationship(edge.getEndNode(), instance, edge.getType(), Relationship.INCOMING);
                    relationshipsToRegister.add(new MappedRelationship(edge.getStartNode(), edge.getType(), edge.getEndNode(), edge.getId(), instance.getClass(), ClassUtils.getType(incomingWriter.typeParameterDescriptor())));
                }
                if (incomingWriter != null || outgoingWriter != null) {
                    registeredEdges.add(edge);
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
                FieldInfo writer = getRelationalWriter(metadata.classInfo(source), edge.getType(), OUTGOING, target);
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
     *
     * @param instance the instance onto which the relationship is to be mapped
     * @param parameter the value to be mapped
     * @param relationshipType the relationship type
     * @param relationshipDirection the relationship direction
     * @return FieldWriter or null if none exists
     */
    private FieldInfo findIterableWriter(Object instance, Object parameter, String relationshipType, String relationshipDirection) {
        ClassInfo classInfo = metadata.classInfo(instance);
        return EntityAccessManager.getIterableField(classInfo, parameter.getClass(), relationshipType, relationshipDirection);
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

        FieldInfo writer = EntityAccessManager.getIterableField(classInfo, valueType, relationshipType, relationshipDirection);
        if (writer != null) {
            if (writer.type().isArray() || Iterable.class.isAssignableFrom(writer.type())) {
                FieldInfo reader = EntityAccessManager.getIterableField(classInfo, valueType, relationshipType, relationshipDirection);
                Object currentValues;
                if (reader != null) {
                    currentValues = reader.read(instance);
                    if (writer.type().isArray()) {
                        values = EntityAccessManager.merge(writer.type(), values, (Object[]) currentValues, valueType);
                    } else {
                        values = EntityAccessManager.merge(writer.type(), values, (Collection) currentValues, valueType);
                    }
                }
            }
            writer.write(instance, values);
            return;
        }
        // this is not necessarily an error. but we can't tell.
        logger.debug("Unable to map iterable of type: {} onto property of {}", valueType, classInfo.name());
    }

	// Find the correct RE associated with the edge. The edge type may be polymorphic, so we need to do a bit of work
	// to identify the correct RE to bind to. We must not cache the value when found, because the correct determination
	// depends on the runtime values of the edge in the mapping context, which may vary for the same edge pattern.
	private ClassInfo getRelationshipEntity(Edge edge) {

		Object source = mappingContext.getNodeEntity(edge.getStartNode());
		Object target = mappingContext.getNodeEntity(edge.getEndNode());

        Set<ClassInfo> classInfos = metadata.classInfoByLabelOrType(edge.getType());

        for (ClassInfo classInfo : classInfos) {

			// both source and target must be declared as START and END nodes respectively
			if (!nodeTypeMatches(classInfo, source, StartNode.class)) {
				continue;
			}

			if (!nodeTypeMatches(classInfo, target, EndNode.class)) {
				continue;
			}

			// if the source OR the target (or one of their superclasses) declares the relationship
			// back to the relationshipEntityClass, we've found a match
			Class relationshipEntityClass = classInfo.getUnderlyingClass();
			if (declaresRelationshipTo(relationshipEntityClass, source.getClass(), edge.getType(), OUTGOING)) {
				return classInfo;
			}

			if (declaresRelationshipTo(relationshipEntityClass, target.getClass(), edge.getType(), INCOMING)) {
				return classInfo;
			}

        }
        // we've made our best efforts to find the correct RE. If we've failed it means
        // that either a matching RE doesn't exist, or its start and end nodes don't declare a reference
        // to the RE. If we can find a single properly-formed matching RE for this edge we'll assume it is
        // the right one, otherwise ... give up
        if (classInfos.size() == 1) {
            ClassInfo classInfo = classInfos.iterator().next();
            if (nodeTypeMatches(classInfo, source, StartNode.class) && nodeTypeMatches(classInfo, target, EndNode.class)) {
                return classInfo;
            }
        } else {
            if (classInfos.size() == 0) {
                // not necessarily a problem, we mey be fetching edges we don't want to hydrate
                logger.debug("Unable to find a matching @RelationshipEntity for {}", edge.toString());
            } else {
                // almost definitely a user bug
                logger.error("Found more than one matching @RelationshipEntity for {} but cannot tell which one to use", edge.toString());
            }
        }
        return null;
    }

	/**
	 * Determines if a class hierarchy that possibly declares a given relationship declares it on a specific class.
	 *
	 * This method is used to resolve polymorphic relationship entity relationship types. In this case, 'to' is
	 * a concrete subclass of a relationship entity whose relationship type is declared on a superclass. In other words,
	 * the relationship type is polymorphic.  The class 'by' is a class that is either a start node or end node of that
	 * polymorphic type, but which does not necessarily refer to the relationship entity subtype referenced by 'to'.
	 *
	 * See #298
	 *
	 * Logic:
	 *
	 * Given a class 'to', a class 'by' and a relationship 'r':
	 *
	 * if the class 'by' (or one of its superclasses) declares a setter or field 'x', annotated with relationship 'r':
	 *   if the class of 'x' is the same as 'to':
	 *   	<-- true
	 *
	 * Otherwise: <-- false.
	 *
	 * @param to the Class which the named relationship must refer to
	 * @param by a Class the named relationship is declared by
	 * @param relationshipName the name of the relationship
	 * @param relationshipDirection the direction of the relationship
	 *
	 * @return true if 'by' declares the specified relationship on 'to', false otherwise
	 */
	private boolean declaresRelationshipTo(Class to, Class by, String relationshipName, String relationshipDirection) {
        return EntityAccessManager.getRelationalWriter(metadata.classInfo(by.getName()), relationshipName, relationshipDirection, to) != null;
	}

	/**
     * Checks that the class of the node matches the class defined in the class info for a given annotation
     *
     * @param classInfo the ClassInfo
     * @param node the node object
     * @param annotationClass the annotation to match
     * @return true if the class of the node matches field annotated
     */
    private boolean nodeTypeMatches(ClassInfo classInfo, Object node, Class<?> annotationClass) {
        List<FieldInfo> fields = classInfo.findFields(annotationClass.getName());
        if (fields.size() == 1) {
            FieldInfo field = fields.get(0);
            if (field.isTypeOf(node.getClass())) {
                return true;
            }
        }
        return false;
    }

    private Class underlyingElementType(ClassInfo classInfo, String propertyName) {
        FieldInfo fieldInfo = fieldInfoForPropertyName(propertyName, classInfo);
        Class clazz = null;
        if (fieldInfo != null) {
            clazz = ClassUtils.getType(fieldInfo.getTypeDescriptor());
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
