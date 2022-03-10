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

import static java.util.stream.Collectors.*;
import static org.neo4j.ogm.metadata.reflect.EntityAccessManager.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Relationship.Direction;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.DescriptorMappings;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.MethodInfo;
import org.neo4j.ogm.metadata.reflect.EntityAccessManager;
import org.neo4j.ogm.metadata.reflect.EntityFactory;
import org.neo4j.ogm.model.Edge;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.PropertyModel;
import org.neo4j.ogm.session.EntityInstantiator;
import org.neo4j.ogm.typeconversion.CompositeAttributeConverter;
import org.neo4j.ogm.utils.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class GraphEntityMapper {

    private static final Logger logger = LoggerFactory.getLogger(GraphEntityMapper.class);

    private static Map<String, ?> toMap(List<Property<String, Object>> propertyList) {

        Map<String, Object> map = new HashMap<>();

        for (Property<String, Object> property : propertyList) {
            map.put(property.getKey(), property.getValue());
        }

        return map;
    }

    private final MappingContext mappingContext;
    private final EntityFactory entityFactory;
    private final MetaData metadata;

    public GraphEntityMapper(MetaData metaData, MappingContext mappingContext, EntityInstantiator entityInstantiator) {
        this.metadata = metaData;
        this.entityFactory = new EntityFactory(metadata, entityInstantiator);
        this.mappingContext = mappingContext;
    }

    <T> List<T> map(Class<T> type, Response<GraphModel> listOfGraphModels) {
        return map(type, listOfGraphModels, (m, n) -> true);
    }

    /**
     * @param type                 the type of the entities to return
     * @param graphModelResponse   The response of graph models to work on
     * @param additionalNodeFilter An optional filter to exclude entities based on some nodes from the result
     * @param <T>                  The type of the class of the entities to return
     * @return The list of entities represented by the list of graph models.
     */
    <T> List<T> map(Class<T> type, Response<GraphModel> graphModelResponse,
        BiFunction<GraphModel, Long, Boolean> additionalNodeFilter) {

        // Those are the ids of all mapped nodes.
        Set<Long> mappedNodeIds = new LinkedHashSet<>();

        Set<Long> returnedNodeIds = new LinkedHashSet<>();
        Set<Long> mappedRelationshipIds = new LinkedHashSet<>();
        Set<Long> returnedRelationshipIds = new LinkedHashSet<>();

        // Execute mapping for each individual model
        Consumer<GraphModel> mapContentOfIndividualModel =
            graphModel -> mapContentOf(graphModel, additionalNodeFilter, returnedNodeIds, mappedRelationshipIds,
                returnedRelationshipIds, mappedNodeIds);

        GraphModel graphModel = null;
        while ((graphModel = graphModelResponse.next()) != null) {
            mapContentOfIndividualModel.accept(graphModel);
        }
        graphModelResponse.close();

        // Execute postload after all models and only for new ids
        executePostLoad(mappedNodeIds, mappedRelationshipIds);

        // Collect result
        Predicate<Object> entityPresentAndCompatible = entity -> entity != null && type
            .isAssignableFrom(entity.getClass());
        List<T> results = returnedNodeIds.stream()
            .map(mappingContext::getNodeEntity)
            .filter(entityPresentAndCompatible)
            .map(type::cast)
            .collect(toList());

        // only look for REs if no node entities were found
        if (results.isEmpty()) {

            results = returnedRelationshipIds.stream()
                .map(mappingContext::getRelationshipEntity)
                .filter(entityPresentAndCompatible)
                .map(type::cast)
                .collect(toList());
        }

        return results;
    }

    private void mapContentOf(
        GraphModel graphModel,
        BiFunction<GraphModel, Long, Boolean> additionalNodeFilter,
        Set<Long> returnedNodeIds,
        Set<Long> mappedRelationshipIds,
        Set<Long> returnedRelationshipIds,
        Set<Long> mappedNodeIds
    ) {
        Predicate<Long> includeInResult = id -> additionalNodeFilter.apply(graphModel, id);
        try {
            Set<Long> newNodeIds = mapNodes(graphModel);
            returnedNodeIds.addAll(newNodeIds.stream().filter(includeInResult).collect(toList()));
            mappedNodeIds.addAll(newNodeIds);

            newNodeIds = mapRelationships(graphModel);
            returnedRelationshipIds.addAll(newNodeIds.stream().filter(includeInResult).collect(toList()));
            mappedRelationshipIds.addAll(newNodeIds);
        } catch (MappingException e) {
            throw e;
        } catch (Exception e) {
            throw new MappingException("Error mapping GraphModel", e);
        }
    }

    /**
     * Executes method annotated with @PostLoad on node and relationship entities with given ids
     * This method must be executed after processing all result rows:
     * - post load must be called after the entity is fully hydrated
     * - to avoid executing post load multiple times on same entity
     *
     * @param nodeIds nodeIds
     * @param edgeIds edgeIds
     */
    private void executePostLoad(Set<Long> nodeIds, Set<Long> edgeIds) {
        for (Long id : nodeIds) {
            Object o = mappingContext.getNodeEntity(id);
            executePostLoad(o);
        }

        for (Long id : edgeIds) {
            Object o = mappingContext.getRelationshipEntity(id);
            if (o != null) {
                executePostLoad(o);
            }
        }
    }

    private void executePostLoad(Object instance) {

        ClassInfo classInfo = metadata.classInfo(instance);
        MethodInfo postLoadMethod = classInfo.postLoadMethodOrNull();
        if (postLoadMethod == null) {
            return;
        }

        try {
            postLoadMethod.invoke(instance);
        } catch (SecurityException e) {
            logger.warn("Cannot call PostLoad annotated method {} on class {}, "
                + "security manager denied access.", postLoadMethod.getMethod().getName(), classInfo.name(), e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.warn("Cannot call PostLoad annotated method {} on class {}. "
                + "Make sure it is public and has no arguments", postLoadMethod.getMethod().getName(), classInfo.name(), e);
        }
    }

    private Set<Long> mapNodes(GraphModel graphModel) {

        Set<Long> mappedNodeIds = new LinkedHashSet<>();
        for (Node node : graphModel.getNodes()) {
            Object entity = mappingContext.getNodeEntity(node.getId());
            if (entity == null) {
                ClassInfo clsi = metadata.resolve(node.getLabels());
                if (clsi == null) {
                    logger.debug("Could not find a class to map for labels " + Arrays.toString(node.getLabels()));
                    continue;
                }
                Map<String, Object> allProps = new HashMap<>(toMap(node.getPropertyList()));
                getCompositeProperties(node.getPropertyList(), clsi).forEach((k, v) -> {
                    allProps.put(k.getName(), v);
                });

                entity = entityFactory.newObject(clsi.getUnderlyingClass(), allProps);
                EntityUtils.setIdentity(entity, node.getId(), metadata);
                setProperties(node.getPropertyList(), entity);
                setLabels(node, entity);
                mappingContext.addNodeEntity(entity, node.getId());
            }
            mappedNodeIds.add(node.getId());
        }

        return mappedNodeIds;
    }

    /**
     * Finds the composite properties of an entity type and build their values using a property list.
     *
     * @param propertyList The properties to convert from.
     * @param classInfo    The class to inspect for composite attributes.
     * @return a map containing the values of the converted attributes, indexed by field object. Never null.
     */
    private Map<FieldInfo, Object> getCompositeProperties(List<Property<String, Object>> propertyList,
        ClassInfo classInfo) {

        Map<FieldInfo, Object> compositeValues = new HashMap<>();

        Collection<FieldInfo> compositeFields = classInfo.fieldsInfo().compositeFields();
        if (compositeFields.size() > 0) {
            Map<String, ?> propertyMap = toMap(propertyList);
            for (FieldInfo field : compositeFields) {
                CompositeAttributeConverter<?> converter = field.getCompositeConverter();
                compositeValues.put(field, converter.toEntityAttribute(propertyMap));
            }
        }

        return compositeValues;
    }

    private void setProperties(List<Property<String, Object>> propertyList, Object instance) {
        ClassInfo classInfo = metadata.classInfo(instance);

        getCompositeProperties(propertyList, classInfo).forEach((field, v) -> field.write(instance, v));

        for (Property<?, ?> property : propertyList) {
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
                Class<?> paramType = writer.type();
                Class elementType = underlyingElementType(classInfo, property.getKey().toString());
                if (paramType.isArray()) {
                    value = EntityAccessManager.merge(paramType, value, new Object[] {}, elementType);
                } else {
                    value = EntityAccessManager.merge(paramType, value, Collections.emptyList(), elementType);
                }
            }
            writer.write(instance, value);
        }
    }

    private Set<Long> mapRelationships(GraphModel graphModel) {

        Set<Long> mappedRelationshipIds = new HashSet<>();
        List<Edge> oneToMany = new ArrayList<>();

        for (Edge edge : graphModel.getRelationships()) {

            Object source = mappingContext.getNodeEntity(edge.getStartNode());
            Object target = mappingContext.getNodeEntity(edge.getEndNode());

            if (source == null || target == null) {
                String messageFormat
                    = "Relationship {} cannot be fully hydrated because one or more required node entities have not been part of the result set.";
                logger.warn(messageFormat, edge);
            } else {
                mappedRelationshipIds.add(edge.getId());

                // check whether this edge should in fact be handled as a relationship entity
                ClassInfo relationshipEntityClassInfo = getRelationshipEntity(edge);

                if (relationshipEntityClassInfo != null) {
                    mapRelationshipEntity(oneToMany, edge, source, target, relationshipEntityClassInfo);
                } else {
                    oneToMany.add(edge);
                }
            }
        }

        if (!oneToMany.isEmpty()) {
            mapOneToMany(oneToMany);
        }

        return mappedRelationshipIds;
    }

    private void mapRelationshipEntity(List<Edge> oneToMany, Edge edge, Object source, Object target,
        ClassInfo relationshipEntityClassInfo) {
        logger.debug("Found relationship type: {} to map to RelationshipEntity: {}", edge.getType(),
            relationshipEntityClassInfo.name());

        // look to see if this relationship already exists in the mapping context.
        Object relationshipEntity = mappingContext.getRelationshipEntity(edge.getId());

        // do we know about it?
        if (relationshipEntity == null) { // no, create a new relationship entity
            relationshipEntity = createRelationshipEntity(edge, source, target);
        }

        // If the source has a writer for an outgoing relationship for the rel entity, then write the rel entity on the source if it's a scalar writer
        ClassInfo sourceInfo = metadata.classInfo(source);
        FieldInfo writer = getRelationalWriter(sourceInfo, edge.getType(), Direction.OUTGOING, relationshipEntity);
        if (writer == null) {
            logger.debug("No writer for {}", target);
        } else {
            if (writer.forScalar()) {
                writer.write(source, relationshipEntity);
                mappingContext.addRelationship(
                    new MappedRelationship(edge.getStartNode(), edge.getType(), edge.getEndNode(), edge.getId(),
                        source.getClass(), DescriptorMappings.getType(writer.getTypeDescriptor())));
            } else {
                oneToMany.add(edge);
            }
        }

        //If the target has a writer for an incoming relationship for the rel entity, then write the rel entity on the target if it's a scalar writer
        ClassInfo targetInfo = metadata.classInfo(target);
        writer = getRelationalWriter(targetInfo, edge.getType(), Direction.INCOMING, relationshipEntity);

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

        ClassInfo relationClassInfo = getRelationshipEntity(edge);
        if (relationClassInfo == null) {
            throw new MappingException("Could not find a class to map for relation " + edge);
        }

        Map<String, Object> allProps = new HashMap<>(toMap(edge.getPropertyList()));
        getCompositeProperties(edge.getPropertyList(), relationClassInfo).forEach((k, v) -> {
            allProps.put(k.getName(), v);
        });
        // also add start and end node as valid constructor values
        allProps.put(relationClassInfo.getStartNodeReader().getName(), startEntity);
        allProps.put(relationClassInfo.getEndNodeReader().getName(), endEntity);

        // create and hydrate the new RE
        Object relationshipEntity = entityFactory
            .newObject(relationClassInfo.getUnderlyingClass(), allProps);
        EntityUtils.setIdentity(relationshipEntity, edge.getId(), metadata);

        // REs also have properties
        setProperties(edge.getPropertyList(), relationshipEntity);

        // register it in the mapping context
        mappingContext.addRelationshipEntity(relationshipEntity, edge.getId());

        // set the start and end entities
        ClassInfo relEntityInfo = metadata.classInfo(relationshipEntity);

        FieldInfo startNodeWriter = relEntityInfo.getStartNodeReader();
        if (startNodeWriter != null) {
            startNodeWriter.write(relationshipEntity, startEntity);
        } else {
            throw new RuntimeException(
                "Cannot find a writer for the StartNode of relational entity " + relEntityInfo.name());
        }

        FieldInfo endNodeWriter = relEntityInfo.getEndNodeReader();
        if (endNodeWriter != null) {
            endNodeWriter.write(relationshipEntity, endEntity);
        } else {
            throw new RuntimeException(
                "Cannot find a writer for the EndNode of relational entity " + relEntityInfo.name());
        }

        return relationshipEntity;
    }

    private void mapOneToMany(Collection<Edge> oneToManyRelationships) {

        EntityCollector entityCollector = new EntityCollector();
        List<MappedRelationship> relationshipsToRegister = new ArrayList<>();

        // first, build the full set of related entities of each type and direction for each source entity in the relationship
        for (Edge edge : oneToManyRelationships) {

            Object instance = mappingContext.getNodeEntity(edge.getStartNode());
            Object parameter = mappingContext.getNodeEntity(edge.getEndNode());

            // is this a relationship entity we're trying to map?
            Object relationshipEntity = mappingContext.getRelationshipEntity(edge.getId());
            if (relationshipEntity != null) {
                // establish a relationship between
                FieldInfo outgoingWriter =
                    findIterableWriter(instance, relationshipEntity, edge.getType(), Direction.OUTGOING);
                if (outgoingWriter != null) {
                    entityCollector.collectRelationship(edge.getStartNode(),
                        DescriptorMappings.getType(outgoingWriter.getTypeDescriptor()), edge.getType(), Direction.OUTGOING,
                        edge.getId(), edge.getEndNode(), relationshipEntity);
                    relationshipsToRegister.add(
                        new MappedRelationship(edge.getStartNode(), edge.getType(), edge.getEndNode(), edge.getId(),
                            instance.getClass(), DescriptorMappings.getType(outgoingWriter.getTypeDescriptor())));
                }
                FieldInfo incomingWriter =
                    findIterableWriter(parameter, relationshipEntity, edge.getType(), Direction.INCOMING);
                if (incomingWriter != null) {
                    entityCollector.collectRelationship(edge.getEndNode(),
                        DescriptorMappings.getType(incomingWriter.getTypeDescriptor()), edge.getType(), Direction.INCOMING,
                        edge.getId(), edge.getStartNode(), relationshipEntity);
                    relationshipsToRegister.add(
                        new MappedRelationship(edge.getStartNode(), edge.getType(), edge.getEndNode(), edge.getId(),
                            instance.getClass(), DescriptorMappings.getType(incomingWriter.getTypeDescriptor())));
                }
            } else {

                // Use getRelationalWriter instead of findIterableWriter
                // findIterableWriter will return matching iterable even when there is better matching single field
                FieldInfo outgoingWriter =
                    getRelationalWriter(metadata.classInfo(instance), edge.getType(), Direction.OUTGOING, parameter);
                if (outgoingWriter != null) {
                    if (!outgoingWriter.forScalar()) {
                        entityCollector.collectRelationship(edge.getStartNode(),
                            DescriptorMappings.getType(outgoingWriter.getTypeDescriptor()), edge.getType(),
                            Direction.OUTGOING,
                            edge.getEndNode(), parameter);
                    } else {
                        outgoingWriter.write(instance, parameter);
                    }
                    MappedRelationship mappedRelationship = new MappedRelationship(edge.getStartNode(), edge.getType(),
                        edge.getEndNode(), null, instance.getClass(),
                        DescriptorMappings.getType(outgoingWriter.getTypeDescriptor()));
                    relationshipsToRegister.add(mappedRelationship);
                }
                FieldInfo incomingWriter
                    = getRelationalWriter(metadata.classInfo(parameter), edge.getType(), Direction.INCOMING, instance);
                if (incomingWriter != null) {
                    if (!incomingWriter.forScalar()) {
                        entityCollector.collectRelationship(edge.getEndNode(),
                            DescriptorMappings.getType(incomingWriter.getTypeDescriptor()), edge.getType(),
                            Direction.INCOMING,
                            edge.getStartNode(), instance);
                    } else {
                        incomingWriter.write(parameter, instance);
                    }
                    relationshipsToRegister.add(
                        new MappedRelationship(edge.getStartNode(), edge.getType(), edge.getEndNode(), null,
                            instance.getClass(), DescriptorMappings.getType(incomingWriter.getTypeDescriptor())));
                }
            }
        }

        entityCollector.forCollectedEntities((sourceId, type, direction, targetType, entities) ->
            mapOneToMany(mappingContext.getNodeEntity(sourceId), targetType, entities, type, direction)
        );

        // now register all the relationships we've mapped as iterable types into the mapping context
        for (MappedRelationship mappedRelationship : relationshipsToRegister) {
            mappingContext.addRelationship(mappedRelationship);
        }
    }

    /**
     * Return an iterable writer to map a relationship onto an entity for the given relationshipType and relationshipDirection
     *
     * @param instance              the instance onto which the relationship is to be mapped
     * @param parameter             the value to be mapped
     * @param relationshipType      the relationship type
     * @param relationshipDirection the relationship direction
     * @return FieldWriter or null if none exists
     */
    private FieldInfo findIterableWriter(Object instance, Object parameter, String relationshipType,
        Direction relationshipDirection) {
        ClassInfo classInfo = metadata.classInfo(instance);
        return EntityAccessManager
            .getIterableField(classInfo, parameter.getClass(), relationshipType, relationshipDirection);
    }

    /**
     * Map many values to an instance based on the relationship type.
     * See DATAGRAPH-637
     *
     * @param instance         the instance to map values onto
     * @param valueType        the type of each value
     * @param values           the values to map
     * @param relationshipType the relationship type associated with these values
     */
    private void mapOneToMany(Object instance, Class<?> valueType, Object values, String relationshipType,
        Direction relationshipDirection) {

        ClassInfo classInfo = metadata.classInfo(instance);

        FieldInfo writer = EntityAccessManager
            .getIterableField(classInfo, valueType, relationshipType, relationshipDirection);
        if (writer != null) {
            if (writer.type().isArray() || Iterable.class.isAssignableFrom(writer.type())) {
                FieldInfo reader = EntityAccessManager
                    .getIterableField(classInfo, valueType, relationshipType, relationshipDirection);
                Object currentValues;
                if (reader != null) {
                    currentValues = reader.read(instance);
                    if (writer.type().isArray()) {
                        values = EntityAccessManager.merge(writer.type(), values, (Object[]) currentValues, valueType);
                    } else {
                        values = EntityAccessManager
                            .merge(writer.type(), values, (Collection) currentValues, valueType);
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
            if (declaresRelationshipTo(relationshipEntityClass, source.getClass(), edge.getType(), Direction.OUTGOING)) {
                return classInfo;
            }

            if (declaresRelationshipTo(relationshipEntityClass, target.getClass(), edge.getType(), Direction.INCOMING)) {
                return classInfo;
            }
        }
        // we've made our best efforts to find the correct RE. If we've failed it means
        // that either a matching RE doesn't exist, or its start and end nodes don't declare a reference
        // to the RE. If we can find a single properly-formed matching RE for this edge we'll assume it is
        // the right one, otherwise ... give up
        if (classInfos.size() == 1) {
            ClassInfo classInfo = classInfos.iterator().next();
            if (nodeTypeMatches(classInfo, source, StartNode.class) && nodeTypeMatches(classInfo, target,
                EndNode.class)) {
                return classInfo;
            }
        } else {
            if (classInfos.size() == 0) {
                // not necessarily a problem, we mey be fetching edges we don't want to hydrate
                if (logger.isDebugEnabled()) {
                    logger.debug("Unable to find a matching @RelationshipEntity for {}", edge.toString());
                }
            } else {
                // almost definitely a user bug
                logger.error("Found more than one matching @RelationshipEntity for {} but cannot tell which one to use",
                    edge.toString());
            }
        }
        return null;
    }

    /**
     * Determines if a class hierarchy that possibly declares a given relationship declares it on a specific class.
     * This method is used to resolve polymorphic relationship entity relationship types. In this case, 'to' is
     * a concrete subclass of a relationship entity whose relationship type is declared on a superclass. In other words,
     * the relationship type is polymorphic.  The class 'by' is a class that is either a start node or end node of that
     * polymorphic type, but which does not necessarily refer to the relationship entity subtype referenced by 'to'.
     * See #298
     * Logic:
     * Given a class 'to', a class 'by' and a relationship 'r':
     * if the class 'by' (or one of its superclasses) declares a setter or field 'x', annotated with relationship 'r':
     * if the class of 'x' is the same as 'to':
     * <-- true
     * Otherwise: <-- false.
     *
     * @param to                    the Class which the named relationship must refer to
     * @param by                    a Class the named relationship is declared by
     * @param relationshipName      the name of the relationship
     * @param relationshipDirection the direction of the relationship
     * @return true if 'by' declares the specified relationship on 'to', false otherwise
     */
    private boolean declaresRelationshipTo(Class to, Class by, String relationshipName, Direction relationshipDirection) {
        return EntityAccessManager
            .getRelationalWriter(metadata.classInfo(by.getName()), relationshipName, relationshipDirection, to) != null;
    }

    /**
     * Checks that the class of the node matches the class defined in the class info for a given annotation
     *
     * @param classInfo       the ClassInfo
     * @param node            the node object
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
            clazz = DescriptorMappings.getType(fieldInfo.getTypeDescriptor());
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
