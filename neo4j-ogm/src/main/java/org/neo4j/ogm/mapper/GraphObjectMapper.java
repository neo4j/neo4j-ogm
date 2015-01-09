package org.neo4j.ogm.mapper;

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

public class GraphObjectMapper implements GraphToObjectMapper<GraphModel> {

    private final Logger logger = LoggerFactory.getLogger(GraphObjectMapper.class);

    private final MappingContext mappingContext;
    private final ObjectFactory objectFactory;
    private final MetaData metadata;
    private final EntityAccessStrategy entityAccessStrategy;

    public GraphObjectMapper(MetaData metaData, MappingContext mappingContext) {
        this.metadata = metaData;
        this.objectFactory = new ObjectFactory(metadata);
        this.mappingContext = mappingContext;
        this.entityAccessStrategy = new DefaultEntityAccessStrategy();
    }

    @Override
    public <T> Set<T> load(Class<T> type, GraphModel graphModel) {
        map(type, graphModel);
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

    private <T> void map(Class<T> type, GraphModel graphModel) {
        try {
            mapNodes(graphModel);
            mapRelationships(graphModel);
        } catch (Exception e) {
            throw new MappingException("Error mapping GraphModel to instance of " + type.getName(), e);
        }
    }

    private void mapNodes(GraphModel graphModel) {
        for (NodeModel node : graphModel.getNodes()) {
            Object object = mappingContext.get(node.getId());
            if (object == null) {
                object = mappingContext.register(objectFactory.newObject(node), node.getId());
            }
            setIdentity(object, node.getId());
            setProperties(node, object);
            mappingContext.remember(object);
        }
    }

    private void setIdentity(Object instance, Long id) {
        ClassInfo classInfo = metadata.classInfo(instance.getClass().getName());
        FieldInfo fieldInfo = classInfo.identityField();
        FieldWriter.write(classInfo.getField(fieldInfo), instance, id);
    }

    private void setProperties(NodeModel nodeModel, Object instance) {
        // cache this.
        ClassInfo classInfo = metadata.classInfo(instance.getClass().getName());
        for (Property property : nodeModel.getPropertyList()) {
            writeProperty(classInfo, instance, property);
        }
    }

    private void setProperties(RelationshipModel relationshipModel, Object instance) {
        // cache this.
        ClassInfo classInfo = metadata.classInfo(instance.getClass().getName());
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
        ClassInfo sourceInfo = metadata.classInfo(source.getClass().getName());

        RelationalWriter objectAccess = entityAccessStrategy.getRelationalWriter(sourceInfo, edgeLabel, parameter);
        if (objectAccess != null) {
            objectAccess.write(source, parameter);
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
            ClassInfo relationshipEntityClassInfo = metadata.resolve(edge.getType());
            if (relationshipEntityClassInfo != null) {
                logger.debug("Found relationship type: {} to map to RelationshipEntity: {}", edge.getType(), relationshipEntityClassInfo.name());

                Object relationshipEntity = mappingContext.getRelationshipEntity(edge.getId());
                if (relationshipEntity == null) {
                    relationshipEntity = objectFactory.newObject(edge);
                    mappingContext.registerRelationship(relationshipEntity, edge.getId());

                    setIdentity(relationshipEntity, edge.getId());
                    setProperties(edge, relationshipEntity);

                    // It's not really right semantically to call getRelationalWriter because we're not setting a relationship
                    // - then again, we can't call the property one at all because our type isn't "simple"
                    // now, we could just insist that you annotate start/end nodes and resolve these via EAS
                    // Do even we want to have a "simple" strategy for this given anno's are a must for EA anyway?
                    // - still should ask EAS if it's field/method, even if we do look for @StartNode rather than @Relationship
                    ClassInfo relEntityInfo = metadata.classInfo(relationshipEntity.getClass().getName());
                    RelationalWriter startNodeAccess = entityAccessStrategy.getRelationalWriter(relEntityInfo, edge.getType(), source);
                    if (startNodeAccess != null) {
                        startNodeAccess.write(relationshipEntity, source);
                    }
                    RelationalWriter endNodeAccess = entityAccessStrategy.getRelationalWriter(relEntityInfo, edge.getType(), target);
                    if (endNodeAccess != null) {
                        endNodeAccess.write(relationshipEntity, target);
                    }
                    // TODO: consider throwing an exception here because the relationship type isn't usable?
                }

                // source.setRelationshipEntity
                ClassInfo sourceInfo = metadata.classInfo(source.getClass().getName());
                RelationalWriter sourceAccess = entityAccessStrategy.getRelationalWriter(sourceInfo, edge.getType(), relationshipEntity);
                if (sourceAccess != null) {
                    sourceAccess.write(source, relationshipEntity);
                    mappingContext.remember(new MappedRelationship(edge.getStartNode(), edge.getType(), edge.getEndNode()));
                } else {
                    // cannot set RE on source as one-to-one
                    oneToMany.add(edge);
                }

                // target.setRelationshipEntity
                ClassInfo targetInfo = metadata.classInfo(target.getClass().getName());
                RelationalWriter targetAccess = entityAccessStrategy.getRelationalWriter(targetInfo, edge.getType(), relationshipEntity);
                if (targetAccess != null) {
                    targetAccess.write(target, relationshipEntity);
                    // NB: this is a different direction from the above, although I'm not sure that's the right thing to do :/
                    mappingContext.remember(new MappedRelationship(edge.getStartNode(), edge.getType(), edge.getEndNode()));
                } else {
                    // cannot set RE on target as one-to-one
                    oneToMany.add(edge);
                }
            } else {
                if (!mapOneToOne(source, target, edge)) {
                    oneToMany.add(edge);
                }
                mapOneToOne(target, source, edge);  // try the inverse mapping
            }
        }
        mapOneToMany(oneToMany);
    }

    public Set<Object> get(Class<?> clazz) {
        return mappingContext.getAll(clazz);
    }

    private void mapOneToMany(Set<RelationshipModel> oneToManyRelationships) {

        ObjectCollector typeRelationships = new ObjectCollector();

        // first, build the full set of related objects of each type for each source object in the relationship
        for (RelationshipModel edge : oneToManyRelationships) {

            Object instance = mappingContext.get(edge.getStartNode());
            Object parameter = mappingContext.get(edge.getEndNode());

            Object relationshipEntity = mappingContext.getRelationshipEntity(edge.getId());
            if (relationshipEntity != null) {
                // process the relationship entity that was previously placed in the mapping context
                typeRelationships.recordTypeRelationship(parameter, relationshipEntity);
                typeRelationships.recordTypeRelationship(instance, relationshipEntity);
            }
            else {
                typeRelationships.recordTypeRelationship(instance, parameter);
            }
        }

        // then set the entire collection at the same time.
        for (Object instance : typeRelationships.getOwningTypes()) {
            Map<Class<?>, Set<Object>> handled = typeRelationships.getTypeCollectionMapping(instance);
            for (Class<?> type : handled.keySet()) {
                Collection<?> objects = handled.get(type);
                mapOneToMany(instance, type, objects, oneToManyRelationships);
            }
        }
    }

    private boolean mapOneToMany(Object instance, Class<?> valueType, Object values, Set<RelationshipModel> edges) {

        ClassInfo classInfo = metadata.classInfo(instance.getClass().getName());

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
                values = EntityAccess.merge(writer.type(), (Iterable<?>) values, new ArrayList<Object>());
            }
            writer.write(instance, values);

            String relType = writer.relationshipName(); // FIXME: doesn't work for a relationship entity
            for (RelationshipModel edge : edges) {
                mappingContext.remember(new MappedRelationship(edge.getStartNode(), edge.getType(), edge.getEndNode()));
            }
            return true;
        }

        logger.warn("Unable to map iterable of type: {} onto property of {}", valueType, classInfo.name());
        return false;
    }

}
