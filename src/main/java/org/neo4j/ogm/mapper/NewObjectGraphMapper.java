package org.neo4j.ogm.mapper;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.graphaware.graphmodel.neo4j.NodeModel;
import org.graphaware.graphmodel.neo4j.Property;
import org.graphaware.graphmodel.neo4j.RelationshipModel;
import org.neo4j.ogm.entityaccess.FieldAccess;
import org.neo4j.ogm.entityaccess.MethodAccess;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.factory.MetaDataConstructorObjectFactory;
import org.neo4j.ogm.metadata.factory.ObjectFactory;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;
import org.neo4j.ogm.metadata.info.MethodInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewObjectGraphMapper implements GraphModelToObjectMapper<GraphModel> {

    private static final MappingContext mappingContext = new MappingContext();

    private final Map<Class<?>, List<Object>> typeMap = new HashMap<>();
    private final ObjectFactory objectFactory;
    private final MetaData metadata;
    private Class root;

    public NewObjectGraphMapper(String... packages) {
        metadata = new MetaData(packages);
        objectFactory = new MetaDataConstructorObjectFactory(metadata);
    }

    @Override
    public Object mapToObject(GraphModel graphModel) {
        typeMap.clear();
        try {
            mapNodes(graphModel);
            mapRelationships(graphModel);
            return root();
        } catch (Exception e) {
            throw new MappingException("Error mapping GraphModel to Object", e);
        }
    }

    public Object root() throws Exception {
        List<Object> roots = typeMap.get(root);
        return roots.get(roots.size()-1);
    }

    private void mapNodes(GraphModel graphModel) throws Exception {

        for (NodeModel node : graphModel.getNodes()) {

            Object object = mappingContext.get(node.getId());
            if (object == null) { // object does not yet exist in our domain
                object = objectFactory.instantiateObjectMappedTo(node);
                mappingContext.register(object, node.getId());
                setProperties(node, object);
            }
            register(object);
        }
    }

    /*
     * by preference we use a setter. if one doesn't exist, fall back to field access
     */
    private void setProperties(NodeModel nodeModel, Object instance) throws Exception {
        // cache this.
        ClassInfo classInfo = metadata.classInfo(instance.getClass().getName());
        for (Property property : nodeModel.getPropertyList()) {
            writeProperty(classInfo, instance, property);
        }
    }

    private void writeProperty(ClassInfo classInfo, Object instance, Property property) {
        // a cache would help here.
        MethodInfo methodInfo = metadata.propertyGetter(classInfo, property.getKey().toString());
        if (methodInfo == null) {
            // also, a cache. metadata does not cache
            FieldInfo fieldInfo = metadata.propertyField(classInfo, property.getKey().toString());
            if (fieldInfo == null) {
                // log a warning, we don't recognise this property
            } else {
                FieldAccess.write(fieldInfo, instance, property.getValue());
            }
        } else {
            MethodAccess.write(methodInfo, instance, property.getValue());
        }
    }

    /**
     * Registers the object as an instance of its type in the typeMap register.
     *
     * At the end of the parse, there should be only one object left in the typeMap: the one
     * that we're returning to the caller.
     *
     * @param object the object to register.
     */
    public void register(Object object) {
        List<Object> objectList = typeMap.get(object.getClass());
        if (objectList == null) {
            objectList = new ArrayList<>();
            typeMap.put(object.getClass(), objectList);
        }
        objectList.add(object);
    }

    private void mapRelationships(GraphModel graphModel) throws Exception {

        final List<RelationshipModel> vectorRelationships = new ArrayList<>();

        for (RelationshipModel edge : graphModel.getRelationships()) {
            Object parent   = mappingContext.get(edge.getStartNode());
            Object child    = mappingContext.get(edge.getEndNode());
//            if (setValue(parent, child)) {
//                evict(child.getClass());
//            } else {
//                vectorRelationships.add(edge);
//            }
        }
        //mapOneToMany(vectorRelationships);
    }

}
