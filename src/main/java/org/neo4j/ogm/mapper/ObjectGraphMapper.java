package org.neo4j.ogm.mapper;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.graphaware.graphmodel.neo4j.NodeModel;
import org.graphaware.graphmodel.neo4j.Property;
import org.graphaware.graphmodel.neo4j.RelationshipModel;
import org.neo4j.ogm.entityaccess.EntityAccessFactory;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.ObjectFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Javadoc
 */
public class ObjectGraphMapper implements GraphModelToObjectMapper<GraphModel> {

    private static final MappingContext mappingContext = new MappingContext();

    private final ObjectFactory objectFactory;
    private final EntityAccessFactory entityAccessFactory;
    private final Map<Class, List<Object>> typeMap = new HashMap<>();

    private final Class root;

    /**
     * @param type The type of the root object
     * @param objectFactory The {@link ObjectFactory} to use for instantiating types
     * @param entityAccessorFactory To determine how the property values should be mapped to the fields
     */
    // there may be a case for encapsulating these params in MappingConfiguration
    public ObjectGraphMapper(Class<?> type, ObjectFactory objectFactory,
            EntityAccessFactory entityAccessorFactory) {

        this.root = type;
        this.objectFactory = objectFactory;
        this.entityAccessFactory = entityAccessorFactory;
    }

    @Override
    public Object mapToObject(GraphModel graphModel) {
        typeMap.clear();
        try {
            mapNodes(graphModel);
            mapRelationships(graphModel);
            return root();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // required for tests, since the ids of objects are not unique across tests.
    public void reset() {
        mappingContext.clear();
    }

    public Object root() throws Exception {
        List<Object> roots = typeMap.get(root);
        return roots.get(roots.size()-1);
    }

    public void evict(Class type) {
        typeMap.remove(type);
    }

    public List<Object> get(Class clazz) {
        return typeMap.get(clazz);
    }

    public void register(Object object) {
        List<Object> objectList = typeMap.get(object.getClass());
        if (objectList == null) {
            objectList = new ArrayList<>();
            typeMap.put(object.getClass(), objectList);
        }
        objectList.add(object);
    }

    private void mapNodes(GraphModel graphModel) throws Exception {

        for (NodeModel node : graphModel.getNodes()) {

            Object object = mappingContext.get(node.getId());
            if (object == null) { // this is a never before seen object.
                object = this.objectFactory.instantiateObjectMappedTo(node);
                mappingContext.register(object, node.getId());
                // Note: ASSUMPTION! the object's properties can't change if we've already parsed this previously!
                setProperties(node, object);
            }
            register(object);
        }
    }

    private void mapRelationships(GraphModel graphModel) throws Exception {

        final List<RelationshipModel> vectorRelationships = new ArrayList<>();

        for (RelationshipModel edge : graphModel.getRelationships()) {
            Object parent = mappingContext.get(edge.getStartNode());
            Object child = mappingContext.get(edge.getEndNode());
            if (setValue(parent, child)) {
               evict(child.getClass());
            } else {
                vectorRelationships.add(edge);
            }
        }
        mapOneToMany(vectorRelationships);
    }

    private void mapOneToMany(List<RelationshipModel> vectorRelationships) throws Exception {
        for (RelationshipModel edge : vectorRelationships) {
            Object instance = mappingContext.get(edge.getStartNode());
            Object parameter = mappingContext.get(edge.getEndNode());
            Class<?> type = parameter.getClass();
            if (get(type) != null) {
                entityAccessFactory.forProperty(type.getSimpleName()).setIterable(instance, get(type));
                evict(type); // we've added all instances of type for this object, no point in repeating the effort.
            }
        }
    }

    private void setProperties(NodeModel nodeModel, Object instance) throws Exception {
        for (Property property : nodeModel.getPropertyList()) {
            entityAccessFactory.forProperty((String) property.getKey()).set(instance, property.getValue());
        }
    }

    private boolean setValue(Object instance, Object parameter) throws Exception {
        try {
            entityAccessFactory.forProperty(parameter.getClass().getSimpleName()).setValue(instance, parameter);
            return true;
        } catch (MappingException me) {
            return false;
        }
    }

}
