package org.neo4j.ogm.strategy.simple;

import java.util.ArrayList;
import java.util.List;

import org.graphaware.graphmodel.Property;
import org.graphaware.graphmodel.neo4j.EdgeModel;
import org.graphaware.graphmodel.neo4j.GraphModel;
import org.graphaware.graphmodel.neo4j.NodeModel;
import org.neo4j.ogm.mapper.GraphModelToObjectMapper;
import org.neo4j.ogm.metadata.PersistentFieldDictionary;
import org.neo4j.ogm.metadata.ObjectFactory;
import org.neo4j.ogm.metadata.PersistentField;
import org.neo4j.ogm.strategy.EntityAccessFactory;

/**
 * TODO: Javadoc
 */
public class ObjectGraphMapper implements GraphModelToObjectMapper<GraphModel> {

    private final MappingContext mappingContext;
    private final ObjectFactory objectFactory;
    private final EntityAccessFactory entityAccessFactory;
    private final PersistentFieldDictionary persistentFieldDictionary;

    /**
     * @param type The type of the root object
     * @param objectFactory The {@link ObjectFactory} to use for instantiating types
     * @param entityAccessorFactory To determine how the property values should be mapped to the fields
     * @param persistentFieldDict Contains information about how fields should be mapped to properties and vice versa
     */
    // there may be a case for encapsulating these params in MappingConfiguration
    public ObjectGraphMapper(Class<?> type, ObjectFactory objectFactory,
            EntityAccessFactory entityAccessorFactory, PersistentFieldDictionary persistentFieldDict) {
        this.mappingContext = new MappingContext(type);
        this.objectFactory = objectFactory;
        this.entityAccessFactory = entityAccessorFactory;
        this.persistentFieldDictionary = persistentFieldDict;
    }

    @Override
    public Object mapToObject(GraphModel graphModel) {
        try {
            mapNodes(graphModel);
            mapRelationships(graphModel);
            return mappingContext.root();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void mapNodes(GraphModel graphModel) throws Exception {
        for (NodeModel node : graphModel.getNodes()) {

            Object object = this.objectFactory.instantiateObjectMappedTo(node);

            mappingContext.register(object, node.getId());

            setProperties(node, object);
        }
    }

    private void mapRelationships(GraphModel graphModel) throws Exception {

        final List<EdgeModel> vectorRelationships = new ArrayList<>();

        for (EdgeModel edge : graphModel.getRelationships()) {
            Object parent = mappingContext.get(edge.getStartNode());
            Object child = mappingContext.get(edge.getEndNode());
            if (setValue(parent, child)) {
                mappingContext.evict(child.getClass());
            } else {
                vectorRelationships.add(edge);
            }
        }
        mapOneToMany(vectorRelationships);
    }

    private void mapOneToMany(List<EdgeModel> vectorRelationships) throws Exception {
        for (EdgeModel edge : vectorRelationships) {
            Object instance = mappingContext.get(edge.getStartNode());
            Object parameter = mappingContext.get(edge.getEndNode());
            Class<?> type = parameter.getClass();
            if (mappingContext.get(type) != null) {
                entityAccessFactory.forType(type).setIterable(instance, mappingContext.get(type));
                mappingContext.evict(type); // we've added all instances of type, no point in repeating the effort.
            }
        }
    }

    private void setProperties(NodeModel nodeModel, Object instance) throws Exception {
        for (Property property : nodeModel.getAttributes()) {
            PersistentField pf = persistentFieldDictionary.lookUpPersistentFieldForProperty(property);
            entityAccessFactory.forPersistentField(pf).set(instance, property.getValue());
        }
    }

    private boolean setValue(Object instance, Object parameter) throws Exception {
        try {
            this.entityAccessFactory.forType(parameter.getClass()).setValue(instance, parameter);
            return true;
        } catch (NoSuchMethodException me) {
            return false;
        }
    }

}
