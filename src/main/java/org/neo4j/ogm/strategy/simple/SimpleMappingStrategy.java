package org.neo4j.ogm.strategy.simple;

import org.graphaware.graphmodel.Property;
import org.graphaware.graphmodel.neo4j.EdgeModel;
import org.graphaware.graphmodel.neo4j.GraphModel;
import org.graphaware.graphmodel.neo4j.NodeModel;
import org.neo4j.ogm.mapper.GraphModelToObjectMapper;

import java.util.*;

/**
 * A simple mapping strategy
 */
public class SimpleMappingStrategy implements GraphModelToObjectMapper<GraphModel> {

    private final MappingContext mappingContext;

    private SimpleMappingStrategy(Class type) {
        this.mappingContext = new MappingContext(type);
    }

    public static SimpleMappingStrategy forType(Class type) {
        return new SimpleMappingStrategy(type);
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

            // Object object = classDictionary.instantiate(node);

            String baseClass = node.getLabels()[0]; // by convention :)
            Object object = instantiate(baseClass);

            mappingContext.register(object, node.getId());

            setProperties(node, object);
        }
    }

    private void mapRelationships(GraphModel graphModel) throws Exception {

        final List<EdgeModel> vectorRelationships = new ArrayList<>();

        for (EdgeModel edge : graphModel.getRelationships()) {
            Object parent = mappingContext.get(edge.getStartNode());
            Object child  = mappingContext.get(edge.getEndNode());
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
            Class type = parameter.getClass();
            if (mappingContext.get(type) != null) {
                Setter.forProperty(type.getSimpleName()).setIterable(instance, mappingContext.get(type));
                mappingContext.evict(type); // we've added all instances of type, no point in repeating the effort.
            }
        }
    }

    private void setProperties(NodeModel nodeModel, Object instance) throws Exception {
        for (Property property : nodeModel.getAttributes()) {
            String name = (String) property.getKey();
            Object parameter = property.getValue();
            Setter.forProperty(name).set(instance, parameter);
        }
    }

    private boolean setValue(Object instance, Object parameter) throws Exception {
        try {
            Setter.forProperty(parameter.getClass().getSimpleName()).setValue(instance, parameter);
            return true;
        } catch (NoSuchMethodException me) {
            return false;
        }
    }

    // TODO: these need moving somewhere :
    private Object instantiate(String baseClass) throws Exception {
        return Class.forName(fqn(baseClass)).newInstance();
    }

    private String fqn(String simpleName) {
        return "org.neo4j.ogm.mapper.domain.bike." + simpleName;
    }

}
