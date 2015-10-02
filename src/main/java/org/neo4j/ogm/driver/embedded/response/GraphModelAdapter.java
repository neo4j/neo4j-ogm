package org.neo4j.ogm.driver.embedded.response;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.ogm.api.model.Graph;
import org.neo4j.ogm.api.result.ResultAdapter;
import org.neo4j.ogm.driver.impl.result.ResultGraphModel;
import org.neo4j.ogm.driver.impl.result.ResultProcessingException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This adapter will transform an embedded response into a json response
 *
 * @author vince
 */
public class GraphModelAdapter extends JsonAdapter implements ResultAdapter<Map<String, Object>, Graph> {

    /**
     * Parses a row from the result object and transforms it into a JSON representation
     * compatible with the "graph" type response from Neo's Http transactional end point.
     *
     * This is probably a bit stupid, because we could transform the object directly rather than via
     * an intermediate JSON representation, but, at least its working.
     *
     * @param data the data to transform, given as a map
     * @return
     */

    public Graph adapt(Map<String, Object> data) {

        // These two sets keep track of which nodes and edges have already been built, so we don't redundantly
        // write the same node or relationship entity many times. They are deliberately untyped.
        final Set nodeIdentities = new HashSet<>();
        final Set edgeIdentities = new HashSet<>();

        StringBuilder sb = new StringBuilder();
        StringBuilder nodesBuilder = new StringBuilder();
        StringBuilder edgesBuilder = new StringBuilder();

        OPEN_OBJECT(sb);
        OPEN_OBJECT("graph", sb);

        OPEN_ARRAY("nodes", nodesBuilder);
        OPEN_ARRAY("relationships", edgesBuilder);

        for (Map.Entry mapEntry : data.entrySet()) {
            if (mapEntry.getValue() instanceof Path) {
                buildPath((Path) mapEntry.getValue(), nodesBuilder, edgesBuilder, nodeIdentities, edgeIdentities);
            }
            else if (mapEntry.getValue() instanceof Node) {
                buildNode((Node) mapEntry.getValue(), nodesBuilder, nodeIdentities);
            }
            else if (mapEntry.getValue() instanceof Relationship) {
                buildRelationship((Relationship) mapEntry.getValue(), nodesBuilder, edgesBuilder, nodeIdentities, edgeIdentities);
            }

            else if (mapEntry.getValue() instanceof Iterable) {
                Iterable collection = (Iterable) mapEntry.getValue();
                Iterator iterator = collection.iterator();
                while (iterator.hasNext()) {
                    Object element = iterator.next();
                    if (element instanceof Path) {
                        buildPath((Path) element, nodesBuilder, sb, nodeIdentities, edgeIdentities);
                    }
                    else if (element instanceof Node) {
                        buildNode((Node) element, nodesBuilder, nodeIdentities);
                    }
                    else if (element instanceof Relationship) {
                        buildRelationship((Relationship) element, nodesBuilder, edgesBuilder, nodeIdentities, edgeIdentities);
                    }
                    else {
                        throw new RuntimeException("Not handled:" + mapEntry.getValue().getClass());
                    }


                }
            }
            else {
                throw new RuntimeException("Not handled: " + mapEntry.getValue().getClass());
            }
        }

        CLOSE_ARRAY(nodesBuilder);
        CLOSE_ARRAY(edgesBuilder);

        sb.append(nodesBuilder);
        sb.append(COMMA);
        sb.append(edgesBuilder);

        CLOSE_OBJECT(sb);

        CLOSE_OBJECT(sb);

        try {
            String record = sb.toString();
            return mapper.readValue(record, ResultGraphModel.class).model();
        } catch (Exception e) {
            throw new ResultProcessingException("Could not parse response", e);
        }
    }

    private void buildPath(Path path, StringBuilder nodesBuilder, StringBuilder edgesBuilder, Set nodeIds, Set edgeIds) {

        Iterator<Relationship> relIterator = path.relationships().iterator();
        Iterator<Node> nodeIterator = path.nodes().iterator();

        while (relIterator.hasNext()) {
            Relationship rel = relIterator.next();
            buildRelationship(rel, nodesBuilder, edgesBuilder, nodeIds, edgeIds);
        }

        while (nodeIterator.hasNext()) {
            Node node = nodeIterator.next();
            buildNode(node, nodesBuilder, nodeIds);
        }


    }

    private void buildNode(Node node, StringBuilder nodesBuilder, Set nodeIds) {

        if (!nodeIds.contains(node.getId())) {

            nodeIds.add(node.getId());

            if (nodesBuilder.length() > 10) {
                nodesBuilder.append(COMMA);
            }

            nodesBuilder.append(OPEN_BRACE);

            nodesBuilder.append(KEY_VALUE("id", String.valueOf(node.getId())));  // cypher returns this as a quoted String
            nodesBuilder.append(COMMA);

            nodesBuilder.append(KEY_VALUES("labels", node.getLabels()));
            nodesBuilder.append(COMMA);

            buildProperties(node, nodesBuilder);

            nodesBuilder.append(CLOSE_BRACE);
        }
    }

    private void buildRelationship(Relationship relationship, StringBuilder nodesBuilder, StringBuilder edgesBuilder, Set nodeIds, Set edgeIds) {

        if (!edgeIds.contains(relationship.getId())) {

            edgeIds.add(relationship.getId());

            if (edgesBuilder.length() > 20) {
                edgesBuilder.append(COMMA);
            }

            edgesBuilder.append(OPEN_BRACE);

            edgesBuilder.append(KEY_VALUE("id", String.valueOf(relationship.getId())));
            edgesBuilder.append(COMMA);

            edgesBuilder.append(KEY_VALUE("type", relationship.getType().name()));
            edgesBuilder.append(COMMA);

            edgesBuilder.append(KEY_VALUE("startNode", String.valueOf(relationship.getStartNode().getId())));
            edgesBuilder.append(COMMA);

            edgesBuilder.append(KEY_VALUE("endNode", String.valueOf(relationship.getEndNode().getId())));
            edgesBuilder.append(COMMA);

            buildProperties(relationship, edgesBuilder);

            edgesBuilder.append(CLOSE_BRACE);

            buildNode(relationship.getStartNode(), nodesBuilder, nodeIds);
            buildNode(relationship.getEndNode(), nodesBuilder, nodeIds);
        }
    }

    private void buildProperties(PropertyContainer container, StringBuilder builder) {

        OPEN_OBJECT("properties", builder);

        Iterator<String> i = container.getPropertyKeys().iterator();
        while (i.hasNext()) {
            String k = i.next();
            Object v = container.getProperty(k);
            if (v.getClass().isArray()) {
                builder.append(KEY_VALUES(k, convertToIterable(v)));
            } else {
                builder.append(KEY_VALUE(k, v));
            }
            if (i.hasNext()) {
                builder.append(COMMA);
            }
        }

        CLOSE_OBJECT(builder);
    }

}
