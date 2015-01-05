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

public class GraphObjectMapper implements GraphToObjectMapper<GraphModel> {

    private final Logger logger = LoggerFactory.getLogger(GraphObjectMapper.class);

    private final MappingContext mappingContext;
    private final ObjectFactory objectFactory;
    private final MetaData metadata;
    private final EntityAccessStrategy objectAccessStrategy;

    public GraphObjectMapper(MetaData metaData, MappingContext mappingContext) {
        this.metadata = metaData;
        this.objectFactory = new ObjectFactory(metadata);
        this.mappingContext = mappingContext;
        this.objectAccessStrategy = new DefaultEntityAccessStrategy();
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

    private Long getIdentity(Object instance) {
        ClassInfo classInfo = metadata.classInfo(instance.getClass().getName());
        FieldInfo fieldInfo = classInfo.identityField();
        return (Long) FieldWriter.read(classInfo.getField(fieldInfo), instance);
    }

    private void setProperties(NodeModel nodeModel, Object instance) {
        // cache this.
        ClassInfo classInfo = metadata.classInfo(instance.getClass().getName());
        for (Property property : nodeModel.getPropertyList()) {
            writeProperty(classInfo, instance, property);
        }
    }

    private void writeProperty(ClassInfo classInfo, Object instance, Property property) {

        PropertyWriter writer = objectAccessStrategy.getPropertyWriter(classInfo, property.getKey().toString());

        if (writer == null) {
            logger.warn("Unable to find property: {} on class: {} for writing", property.getKey(), classInfo.name());
        } else {
            Object value = property.getValue();
            // merge iterable / arrays and co-erce to the correct attribute type
            if (writer.type().isArray() || Iterable.class.isAssignableFrom(writer.type())) {
                PropertyReader reader = objectAccessStrategy.getPropertyReader(classInfo, property.getKey().toString());
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

        RelationalWriter objectAccess = objectAccessStrategy.getRelationalWriter(sourceInfo, edgeLabel, parameter);
        if (objectAccess != null) {
            objectAccess.write(source, parameter);
            mappingContext.remember(new MappedRelationship(
                    edge.getStartNode(), relationshipType(objectAccess.relationshipName()), edge.getEndNode()));
            return true;
        }

        return false;
    }

    private void mapRelationships(GraphModel graphModel) {

        final Set<RelationshipModel> oneToMany = new HashSet<>();

        for (RelationshipModel edge : graphModel.getRelationships()) {
            Object source = mappingContext.get(edge.getStartNode());
            Object target = mappingContext.get(edge.getEndNode());
            if (!mapOneToOne(source, target, edge)) {
                oneToMany.add(edge);
            }
            mapOneToOne(target, source, edge);  // try the inverse mapping
        }
        mapOneToMany(oneToMany);
    }

    public Set<Object> get(Class<?> clazz) {
        return mappingContext.getAll(clazz);
    }

    private void mapOneToMany(Set<RelationshipModel> oneToManyRelationships) {

        Map<Object, Map<Class<?>, Set<Object>>> typeRelationships = new HashMap<>();

        // first, build the full set of related objects of each type for each source object in the relationship
        for (RelationshipModel edge : oneToManyRelationships) {

            Object instance = mappingContext.get(edge.getStartNode());
            Object parameter = mappingContext.get(edge.getEndNode());

            Map<Class<?>, Set<Object>> handled = typeRelationships.get(instance);
            if (handled == null) {
                typeRelationships.put(instance, handled=new HashMap<>());
            }
            Class<?> type = parameter.getClass();
            Set<Object> objects = handled.get(type);
            if (objects == null) {
                handled.put(type, objects=new HashSet<>());
            }
            objects.add(parameter);
        }

        // then set the entire collection at the same time.
        for (Object instance : typeRelationships.keySet()) {
            Map<Class<?>, Set<Object>> handled = typeRelationships.get(instance);
            for (Class<?> type : handled.keySet()) {
                Collection<?> objects = handled.get(type);
                mapOneToMany(instance, type, objects, oneToManyRelationships);
            }
        }
    }

    private boolean mapOneToMany(Object instance, Class<?> valueType, Object values, Set<RelationshipModel> edges) {

        ClassInfo classInfo = metadata.classInfo(instance.getClass().getName());

        RelationalWriter writer = objectAccessStrategy.getIterableWriter(classInfo, valueType);
        if (writer != null) {
            if (writer.type().isArray() || Iterable.class.isAssignableFrom(writer.type())) {
                RelationalReader reader = objectAccessStrategy.getIterableReader(classInfo, valueType);
                if (reader != null) {
                    Object currentValues = reader.read(instance);
                    if (writer.type().isArray()) {
                        values = EntityAccess.merge(writer.type(), (Iterable<?>) values, (Object[]) currentValues);
                    } else {
                        values = EntityAccess.merge(writer.type(), (Iterable<?>) values, (Iterable<?>) currentValues);
                    }
                }
                values = EntityAccess.merge(writer.type(), (Iterable<?>) values, new ArrayList());
            }
            writer.write(instance, values);

            String relType = writer.relationshipName();
            for (RelationshipModel edge : edges) {
                mappingContext.remember(new MappedRelationship(edge.getStartNode(), relType, edge.getEndNode()));
            }
            return true;
        }

        logger.warn("Unable to map iterable of type: {} onto property of {}", valueType, classInfo.name());
        return false;
    }

    // this is temporary - will be replaced by work Adam is doing.
    private String relationshipType(String property) {
        StringBuilder sb = new StringBuilder();
        for (char c : property.toCharArray()) {
            if (sb.length() > 0 && Character.isUpperCase(c)) {
                sb.append("_");
            }
            sb.append(Character.toUpperCase(c));
        }
        return sb.toString();
    }

}
