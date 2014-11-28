package org.neo4j.ogm.mapper;

import org.neo4j.graphmodel.GraphModel;
import org.neo4j.graphmodel.NodeModel;
import org.neo4j.graphmodel.Property;
import org.neo4j.graphmodel.RelationshipModel;
import org.neo4j.ogm.entityaccess.DefaultObjectAccessStrategy;
import org.neo4j.ogm.entityaccess.FieldAccess;
import org.neo4j.ogm.entityaccess.ObjectAccess;
import org.neo4j.ogm.entityaccess.ObjectAccessStrategy;
import org.neo4j.ogm.entityaccess.ObjectFactory;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;

import java.util.*;

public class GraphObjectMapper implements GraphToObjectMapper<GraphModel> {

    private final MappingContext mappingContext;
    private final ObjectFactory objectFactory;
    private final MetaData metadata;
    private final ObjectAccessStrategy objectAccessStrategy;

    public GraphObjectMapper(MetaData metaData, MappingContext mappingContext) {
        this.metadata = metaData;
        this.objectFactory = new ObjectFactory(metadata);
        this.mappingContext = mappingContext;
        this.objectAccessStrategy = new DefaultObjectAccessStrategy();
    }

    @Override
    public <T> T load(Class<T> type, GraphModel graphModel)  {
        map(type, graphModel);
        try {
            return type.cast(mappingContext.getRoot(type));
        } catch (Exception e) {
            throw new MappingException("Error mapping GraphModel to instance of " + type.getName(), e);
        }
    }

    @Override
    public <T> Set<T> loadAll(Class<T> type, GraphModel graphModel) {
        map(type, graphModel);
        try {
            Set<T> set = new HashSet<>();
            for (Object o : mappingContext.getObjects(type)) {
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

    private void mapNodes(GraphModel graphModel) throws Exception {
        for (NodeModel node : graphModel.getNodes()) {
            Object object = mappingContext.get(node.getId());
            if (object == null) {
                object = mappingContext.register(objectFactory.newObject(node), node.getId());
                if (getIdentity(object) == null) {
                    synchronized (object) {
                        setIdentity(object, node.getId());
                        setProperties(node, object);
                        mappingContext.remember(object, metadata.classInfo(object.getClass().getName()));
                    }
                }
            }
        }
    }

    private void setIdentity(Object instance, Long id) throws Exception {
        ClassInfo classInfo = metadata.classInfo(instance.getClass().getName());
        FieldInfo fieldInfo = classInfo.identityField();
        FieldAccess.write(classInfo.getField(fieldInfo), instance, id);
    }

    private Long getIdentity(Object instance) throws Exception {
        ClassInfo classInfo = metadata.classInfo(instance.getClass().getName());
        FieldInfo fieldInfo = classInfo.identityField();
        return (Long) FieldAccess.read(classInfo.getField(fieldInfo), instance);
    }

    private void setProperties(NodeModel nodeModel, Object instance) throws Exception {
        // cache this.
        ClassInfo classInfo = metadata.classInfo(instance.getClass().getName());
        for (Property property : nodeModel.getPropertyList()) {
            writeProperty(classInfo, instance, property);
        }
    }

    private void writeProperty(ClassInfo classInfo, Object instance, Property property) {
        ObjectAccess objectAccess = objectAccessStrategy.getPropertyWriteAccess(classInfo, property.getKey().toString());
        if (objectAccess == null) {
            // TODO: log a warning, we don't recognise this property
        } else {
            objectAccess.write(instance, property.getValue());
        }
    }

    private boolean mapOneToOne(Object source, Object parameter, String relationshipType) {
        ClassInfo sourceInfo = metadata.classInfo(source.getClass().getName());

        ObjectAccess objectAccess = objectAccessStrategy.getRelationshipAccess(sourceInfo, relationshipType, parameter);
        if (objectAccess != null) {
            objectAccess.write(source, parameter);
            return true;
        }
        // TODO: log a warning here stating that we don't know how to map that relationship type
        return false;
    }

    private void mapRelationships(GraphModel graphModel) throws Exception {

        final List<RelationshipModel> oneToMany = new ArrayList<>();

        for (RelationshipModel edge : graphModel.getRelationships()) {
            Object source = mappingContext.get(edge.getStartNode());
            Object target = mappingContext.get(edge.getEndNode());
            if (!mapOneToOne(source, target, edge.getType())) {
                oneToMany.add(edge);
            }
            mapOneToOne(target, source, edge.getType());  // try the inverse mapping
        }
        mapOneToMany(oneToMany);
    }

    public Set<Object> get(Class<?> clazz) {
        return mappingContext.getObjects(clazz);
    }

    private void mapOneToMany(List<RelationshipModel> oneToManyRelationships) throws Exception {

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
                mapOneToMany(instance, type, objects);
            }
        }
    }

    private boolean mapOneToMany(Object instance, Class<?> type, Collection<?> objects) {
        ClassInfo classInfo = metadata.classInfo(instance.getClass().getName());

        ObjectAccess objectAccess = objectAccessStrategy.getIterableAccess(classInfo, type);
        if (objectAccess != null) {
            objectAccess.write(instance, objects);
            return true;
        }
        // TODO: should probably log something here too, since we plan to do so for missing properties
        return false;
    }

}
