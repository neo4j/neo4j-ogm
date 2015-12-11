package org.neo4j.ogm.drivers.embedded.response;

import org.neo4j.graphdb.*;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.response.model.DefaultGraphModel;
import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.response.model.RelationshipModel;
import org.neo4j.ogm.result.ResultAdapter;

import java.util.*;

/**
 * This adapter will transform an embedded response into a json response
 *
 * @author vince
 */
public class GraphModelAdapter extends JsonAdapter implements ResultAdapter<Map<String, Object>, GraphModel> {

    /**
     * Parses a row from the result object and transforms it into a GraphModel
     *
     * @param data the data to transform, given as a map
     * @return the data transformed to an {@link GraphModel}
     */
    public GraphModel adapt(Map<String, Object> data) {

        // These two sets keep track of which nodes and edges have already been built, so we don't redundantly
        // write the same node or relationship entity many times.
        final Set<Long> nodeIdentities = new HashSet<>();
        final Set<Long> edgeIdentities = new HashSet<>();

        GraphModel graphModel = new DefaultGraphModel();

        for (Map.Entry mapEntry : data.entrySet()) {

            if (mapEntry.getValue() instanceof Path) {
                buildPath((Path) mapEntry.getValue(), graphModel, nodeIdentities, edgeIdentities);
            }
            else if (mapEntry.getValue() instanceof Node) {
                buildNode((Node) mapEntry.getValue(), graphModel, nodeIdentities);
            }
            else if (mapEntry.getValue() instanceof Relationship) {
                buildRelationship((Relationship) mapEntry.getValue(), graphModel, nodeIdentities, edgeIdentities);
            }

            else if (mapEntry.getValue() instanceof Iterable) {
                Iterable collection = (Iterable) mapEntry.getValue();
                Iterator iterator = collection.iterator();
                while (iterator.hasNext()) {
                    Object element = iterator.next();
                    if (element instanceof Path) {
                        buildPath((Path) element, graphModel, nodeIdentities, edgeIdentities);
                    }
                    else if (element instanceof Node) {
                        buildNode((Node) element, graphModel, nodeIdentities);
                    }
                    else if (element instanceof Relationship) {
                        buildRelationship((Relationship) element, graphModel, nodeIdentities, edgeIdentities);
                    }
                    else {
                        throw new RuntimeException("Not handled:" + mapEntry.getValue().getClass());
                    }
                }
            }
        }

        return graphModel;
    }

    void buildPath(Path path, GraphModel graphModel, Set nodeIdentities, Set edgeIdentities) {
        Iterator<Relationship> relIterator = path.relationships().iterator();
        Iterator<Node> nodeIterator = path.nodes().iterator();

        while (relIterator.hasNext()) {
            Relationship rel = relIterator.next();
            buildRelationship(rel, graphModel, nodeIdentities, edgeIdentities);
        }

        while (nodeIterator.hasNext()) {
            Node node = nodeIterator.next();
            buildNode(node, graphModel, nodeIdentities);
        }     
    }

    void buildNode(Node node, GraphModel graphModel, Set nodeIdentities) {
        if (!nodeIdentities.contains(node.getId())) {

            nodeIdentities.add(node.getId());

            NodeModel nodeModel = new NodeModel();
            nodeModel.setId(node.getId());
            List<String> labelNames = new ArrayList();
            for (Label label : node.getLabels()) {
                labelNames.add(label.name());
            }
            nodeModel.setLabels(labelNames.toArray(new String[] {}));            
            
            nodeModel.setProperties(buildProperties(node));

            graphModel.getNodes().add(nodeModel);
        }    
    }

    void buildRelationship(Relationship relationship, GraphModel graphModel, Set nodeIdentities, Set edgeIdentities) {

        if (!edgeIdentities.contains(relationship.getId())) {

            edgeIdentities.add(relationship.getId());

            RelationshipModel edgeModel = new RelationshipModel();
            edgeModel.setId(relationship.getId());
            edgeModel.setType(relationship.getType().name());
            edgeModel.setStartNode(relationship.getStartNode().getId());
            edgeModel.setEndNode(relationship.getEndNode().getId());

            edgeModel.setProperties(buildProperties(relationship));
            graphModel.getRelationships().add(edgeModel);

            buildNode(relationship.getStartNode(), graphModel, nodeIdentities);
            buildNode(relationship.getEndNode(), graphModel, nodeIdentities);
        }
    }


    Map<String, Object> buildProperties(PropertyContainer container) {

        Map<String, Object> properties = new HashMap<>();

        Iterator<String> i = container.getPropertyKeys().iterator();
        while (i.hasNext()) {
            String k = i.next();
            Object v = container.getProperty(k);
            if (v.getClass().isArray()) {
                properties.put(k, convertToIterable(v));
            } else {
                properties.put(k, v);
            }
        }
        return properties;
    }
}
