package org.neo4j.ogm.mapper;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.entityaccess.*;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.NodeModel;
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.model.RelationshipModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

public class GraphEntityMapper implements GraphToEntityMapper<GraphModel> {

    private final Logger logger = LoggerFactory.getLogger(GraphEntityMapper.class);

    private final MappingContext mappingContext;
    private final EntityFactory entityFactory;
    private final MetaData metadata;
    private final EntityAccessStrategy entityAccessStrategy;

    public GraphEntityMapper(MetaData metaData, MappingContext mappingContext) {
        this.metadata = metaData;
        this.entityFactory = new EntityFactory(metadata);
        this.mappingContext = mappingContext;
        this.entityAccessStrategy = new DefaultEntityAccessStrategy();
    }

    @Override
    public <T> Set<T> map(Class<T> type, GraphModel graphModel) {
        mapEntities(type, graphModel);
        try {
            Set<T> set = new HashSet<>();
            for (Object o : mappingContext.getAll(type)) {
                set.add(type.cast(o));
            }
            return set;
        } catch (Exception e) {
            throw new MappingException("Error mapping GraphModel to instance of " + type.getName(), e);
        }
    }

    private <T> void mapEntities(Class<T> type, GraphModel graphModel) {
        try {
            mapNodes(graphModel);
            mapRelationships(graphModel);
        } catch (Exception e) {
            throw new MappingException("Error mapping GraphModel to instance of " + type.getName(), e);
        }
    }

    private void mapNodes(GraphModel graphModel) {
        for (NodeModel node : graphModel.getNodes()) {
            Object entity = mappingContext.get(node.getId());
            if (entity == null) {
                entity = mappingContext.registerNodeEntity(entityFactory.newObject(node), node.getId());
            }
            setIdentity(entity, node.getId());
            setProperties(node, entity);
            mappingContext.remember(entity);
        }
    }

    private void setIdentity(Object instance, Long id) {
        ClassInfo classInfo = metadata.classInfo(instance);
        FieldInfo fieldInfo = classInfo.identityField();
        FieldWriter.write(classInfo.getField(fieldInfo), instance, id);
    }

    private void setProperties(NodeModel nodeModel, Object instance) {
        // cache this.
        ClassInfo classInfo = metadata.classInfo(instance);
        for (Property property : nodeModel.getPropertyList()) {
            writeProperty(classInfo, instance, property);
        }
    }

    private void setProperties(RelationshipModel relationshipModel, Object instance) {
        // cache this.
        ClassInfo classInfo = metadata.classInfo(instance);
        if (relationshipModel.getProperties() != null) {
        for (Entry<String, Object> property : relationshipModel.getProperties().entrySet()) {
            writeProperty(classInfo, instance, Property.with(property.getKey(), property.getValue()));
        }}
    }

    private void writeProperty(ClassInfo classInfo, Object instance, Property property) {

        PropertyWriter writer = entityAccessStrategy.getPropertyWriter(classInfo, property.getKey().toString());

        if (writer == null) {
            logger.warn("Unable to find property: {} on class: {} for writing", property.getKey(), classInfo.name());
        } else {
            Object value = property.getValue();
            // merge iterable / arrays and co-erce to the correct attribute type
            if (writer.type().isArray() || Iterable.class.isAssignableFrom(writer.type())) {
                PropertyReader reader = entityAccessStrategy.getPropertyReader(classInfo, property.getKey().toString());
                if (reader != null) {
                    Object currentValue = reader.read(instance);
                    if (writer.type().isArray()) {
                        value = EntityAccess.merge(writer.type(), (Iterable<?>) value, (Object[]) currentValue);
                    } else {
                        value = EntityAccess.merge(writer.type(), (Iterable<?>) value, (Iterable<?>) currentValue);
                    }
                }
            }
            writer.write(instance, value);
        }
    }

    private boolean mapOneToOne(Object source, Object parameter, RelationshipModel edge) {

        String edgeLabel = edge.getType();
        ClassInfo sourceInfo = metadata.classInfo(source);

        RelationalWriter writer = entityAccessStrategy.getRelationalWriter(sourceInfo, edgeLabel, parameter);
        if (writer != null) {
            writer.write(source, parameter);
            // FIXME: this doesn't remember the right relationship type when objectAccess sets a RelEntity
            // indeed, why do we use objectAccess.relationshipName instead of just edge.getType?
            mappingContext.remember(new MappedRelationship(edge.getStartNode(), edgeLabel, edge.getEndNode()));
            return true;
        }

        return false;
    }

    private void mapRelationships(GraphModel graphModel) {

        final Set<RelationshipModel> oneToMany = new HashSet<>();

        for (RelationshipModel edge : graphModel.getRelationships()) {

            Object source = mappingContext.get(edge.getStartNode());
            Object target = mappingContext.get(edge.getEndNode());

            // check whether this edge should in fact be handled as a relationship entity
            // This works because a relationship in the graph that has properties must be represented
            // by a domain entity annotated with @RelationshipEntity, and (if it exists) it will be found by
            // metadata.resolve(...)
            ClassInfo relationshipEntityClassInfo = metadata.resolve(edge.getType());

            if (relationshipEntityClassInfo != null) {
                logger.debug("Found relationship type: {} to map to RelationshipEntity: {}", edge.getType(), relationshipEntityClassInfo.name());

                // look to see if this relationship already exists in the mapping context.
                Object relationshipEntity = mappingContext.getRelationshipEntity(edge.getId());

                // do we know about it?
                if (relationshipEntity == null) { // no, create a new relationship entity
                    relationshipEntity = createRelationshipEntity(edge, source, target);
                }

                // source.setRelationshipEntity if OUTGOING/BOTH
                if (!relationshipDirection(source, edge, relationshipEntity).equals(Relationship.INCOMING)) {
                    // try and find a one-to-one writer
                    ClassInfo sourceInfo = metadata.classInfo(source);
                    RelationalWriter writer = entityAccessStrategy.getRelationalWriter(sourceInfo, edge.getType(), relationshipEntity);
                    if (writer != null) {
                        writer.write(source, relationshipEntity);
                        mappingContext.remember(new MappedRelationship(edge.getStartNode(), edge.getType(), edge.getEndNode()));
                    } else {
                        oneToMany.add(edge);
                    }
                }
                // target.setRelationshipEntity if INCOMING/BOTH
                if (!relationshipDirection(target, edge, relationshipEntity).equals(Relationship.OUTGOING)) {
                    ClassInfo targetInfo = metadata.classInfo(target);
                    RelationalWriter writer = entityAccessStrategy.getRelationalWriter(targetInfo, edge.getType(), relationshipEntity);
                    if (writer != null) {
                        writer.write(target, relationshipEntity);
                    } else {
                        oneToMany.add(edge);
                    }
                }
            }
            else {
                if (!mapOneToOne(source, target, edge)) {
                    oneToMany.add(edge);
                }
                mapOneToOne(target, source, edge);  // try the inverse mapping
            }
        }
        mapOneToMany(oneToMany);
    }

    private Object createRelationshipEntity(RelationshipModel edge, Object startEntity, Object endEntity) {

        // create and hydrate the new RE
        Object relationshipEntity = entityFactory.newObject(edge);
        setIdentity(relationshipEntity, edge.getId());
        setProperties(edge, relationshipEntity);

        // register it in the mapping context
        mappingContext.registerRelationshipEntity(relationshipEntity, edge.getId());

        // set the start and end entities
        ClassInfo relEntityInfo = metadata.classInfo(relationshipEntity);
        RelationalWriter startNodeAccess = entityAccessStrategy.getRelationalWriter(relEntityInfo, edge.getType(), startEntity);
        if (startNodeAccess != null) {
            startNodeAccess.write(relationshipEntity, startEntity);
        } // todo : throw exception, though this is detectable during metadata load

        RelationalWriter endNodeAccess = entityAccessStrategy.getRelationalWriter(relEntityInfo, edge.getType(), endEntity);
        if (endNodeAccess != null) {
            endNodeAccess.write(relationshipEntity, endEntity);
        } // todo : throw exception, though this is detectable during metadata load

        return relationshipEntity;
    }

    public Set<Object> get(Class<?> clazz) {
        return mappingContext.getAll(clazz);
    }

    private void mapOneToMany(Set<RelationshipModel> oneToManyRelationships) {

        EntityCollector typeRelationships = new EntityCollector();

        // first, build the full set of related entities of each type for each source entity in the relationship
        for (RelationshipModel edge : oneToManyRelationships) {

            Object instance = mappingContext.get(edge.getStartNode());
            Object parameter = mappingContext.get(edge.getEndNode());

            // is this a relationship entity we're trying to map?
            Object relationshipEntity = mappingContext.getRelationshipEntity(edge.getId());
            if (relationshipEntity != null) {
                if (!relationshipDirection(instance, edge, relationshipEntity).equals(Relationship.INCOMING)) {
                    typeRelationships.recordTypeRelationship(instance, relationshipEntity);
                }
                if (!relationshipDirection(parameter, edge, relationshipEntity).equals(Relationship.OUTGOING)) {
                    typeRelationships.recordTypeRelationship(parameter, relationshipEntity);
                }
            }
            else {
                typeRelationships.recordTypeRelationship(instance, parameter);
            }
        }

        // then set the entire collection at the same time.
        for (Object instance : typeRelationships.getOwningTypes()) {
            Map<Class<?>, Set<Object>> handled = typeRelationships.getTypeCollectionMapping(instance);
            for (Class<?> type : handled.keySet()) {
                Collection<?> entities = handled.get(type);
                mapOneToMany(instance, type, entities, oneToManyRelationships);
            }
        }
    }

    /**
     * Returns the relationship direction between source and target of the specified type as specified
     * by the source entity. This may be different from the direction registered in the graph.
     *
     * By default, if a direction is not specified through an annotation, the direction follows
     * the graph's convention, (source)-[:R]->(target) or OUTGOING from source to target.
     *
     * @param source an entity representing the start of the relationship from the graph's perspective
     * @param edge   {@link RelationshipModel} holding information about the relationship in the graph
     * @param target en entity representing the end of the relationship from the graph's perspective
     * @return  one of {@link Relationship.OUTGOING}, {@link Relationship.INCOMING}, {@link Relationship.BOTH}
     */
    private String relationshipDirection(Object source, RelationshipModel edge, Object target) {
        ClassInfo classInfo = metadata.classInfo(source);
        RelationalWriter writer = entityAccessStrategy.getRelationalWriter(classInfo, edge.getType(), target);
        if (writer == null) {
            writer = entityAccessStrategy.getIterableWriter(classInfo, target.getClass());
        }
        return writer.relationshipDirection();
    }

    private boolean mapOneToMany(Object instance, Class<?> valueType, Object values, Set<RelationshipModel> edges) {

        ClassInfo classInfo = metadata.classInfo(instance);

        RelationalWriter writer = entityAccessStrategy.getIterableWriter(classInfo, valueType);
        if (writer != null) {
            if (writer.type().isArray() || Iterable.class.isAssignableFrom(writer.type())) {
                RelationalReader reader = entityAccessStrategy.getIterableReader(classInfo, valueType);
                if (reader != null) {
                    Object currentValues = reader.read(instance);
                    if (writer.type().isArray()) {
                        values = EntityAccess.merge(writer.type(), (Iterable<?>) values, (Object[]) currentValues);
                    } else {
                        values = EntityAccess.merge(writer.type(), (Iterable<?>) values, (Iterable<?>) currentValues);
                    }
                }
                values = EntityAccess.merge(writer.type(), (Iterable<?>) values, new ArrayList<>());
            }
            writer.write(instance, values);

            for (RelationshipModel edge : edges) {
                mappingContext.remember(new MappedRelationship(edge.getStartNode(), edge.getType(), edge.getEndNode()));
            }
            return true;
        }

        logger.warn("Unable to map iterable of type: {} onto property of {}", valueType, classInfo.name());
        return false;
    }

}
