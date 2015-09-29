package org.neo4j.ogm.driver.embedded.response;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.ogm.driver.JsonAdapter;
import org.neo4j.ogm.session.response.model.GraphModel;
import org.neo4j.ogm.session.result.GraphModelResult;
import org.neo4j.ogm.session.result.ResultAdapter;
import org.neo4j.ogm.session.result.ResultProcessingException;

import java.util.Iterator;
import java.util.Map;

/**
 * This adapter will transform an embedded response into a json response
 *
 * @author vince
 */
public class GraphModelAdapter extends JsonAdapter implements ResultAdapter<Map<String, Object>, GraphModel> {

    /**
     * Parses a row from the result object and transforms it into a JSON representation
     * compatible with the "graph" type response from Neo's Http transactional end point.
     *
     * @param data the data to transform, given as a map
     * @return
     */
    public GraphModel adapt(Map<String, Object> data) {

        StringBuilder sb = new StringBuilder();
        StringBuilder nb = new StringBuilder();
        StringBuilder rb = new StringBuilder();

        OPEN_OBJECT(sb);
        OPEN_OBJECT("graph", sb);

        OPEN_ARRAY("nodes", nb);
        OPEN_ARRAY("relationships", rb);

        for (Map.Entry mapEntry : data.entrySet()) {
            if (mapEntry.getValue() instanceof Path) {
                buildPath((Path) mapEntry.getValue(), nb, rb);
            }
            else if (mapEntry.getValue() instanceof Node) {
                buildNode((Node) mapEntry.getValue(), nb);
            }
            else if (mapEntry.getValue() instanceof Relationship) {
                buildRelationship((Relationship) mapEntry.getValue(), nb, rb);
            }

            else if (mapEntry.getValue() instanceof Iterable) {
                Iterable collection = (Iterable) mapEntry.getValue();
                Iterator iterator = collection.iterator();
                while (iterator.hasNext()) {
                    Object element = iterator.next();
                    if (element instanceof Path) {
                        buildPath((Path) element, nb, sb);
                    }
                    else if (element instanceof Node) {
                        buildNode((Node) element, nb);
                    }
                    else if (element instanceof Relationship) {
                        buildRelationship((Relationship) element, nb, rb);
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

        CLOSE_ARRAY(nb);
        CLOSE_ARRAY(rb);

        sb.append(nb);
        sb.append(COMMA);
        sb.append(rb);

        CLOSE_OBJECT(sb);

        CLOSE_OBJECT(sb);

        try {
            String record = sb.toString();
            return mapper.readValue(record, GraphModelResult.class).getGraph();
        } catch (Exception e) {
            throw new ResultProcessingException("Could not parse response", e);
        }
    }

    private void buildPath(Path path, StringBuilder nodes, StringBuilder edges) {

        Iterator<Relationship> relIterator = path.relationships().iterator();

        while (relIterator.hasNext()) {

            Relationship rel = relIterator.next();

            //buildNode(rel.getStartNode(), nodes);
            //nodes.append(COMMA);
            //buildNode(rel.getEndNode(), nodes);

            buildRelationship(rel, nodes, edges);
        }

    }

    private void buildNode(Node node, StringBuilder sb) {

        if (sb.length() > 10) {
            sb.append(COMMA);
        }

        sb.append(OPEN_BRACE);

        sb.append(KEY_VALUE("id", String.valueOf(node.getId())));  // cypher returns this as a quoted String
        sb.append(COMMA);

        sb.append(KEY_VALUES("labels", node.getLabels()));
        sb.append(COMMA);

        buildProperties(node, sb);

        sb.append(CLOSE_BRACE);

    }

    private void buildRelationship(Relationship relationship, StringBuilder nb, StringBuilder rb) {

        if (rb.length() > 20) {
            rb.append(COMMA);
        }

        rb.append(OPEN_BRACE);

        rb.append(KEY_VALUE("id", String.valueOf(relationship.getId())));
        rb.append(COMMA);

        rb.append(KEY_VALUE("type", relationship.getType().name()));
        rb.append(COMMA);

        rb.append(KEY_VALUE("startNode", String.valueOf(relationship.getStartNode().getId())));
        rb.append(COMMA);

        rb.append(KEY_VALUE("endNode", String.valueOf(relationship.getEndNode().getId())));
        rb.append(COMMA);

        buildProperties(relationship, rb);

        rb.append(CLOSE_BRACE);

        // todo - don't create duplicates
        buildNode(relationship.getStartNode(), nb);
        buildNode(relationship.getEndNode(), nb);

    }

    private void buildProperties(PropertyContainer container, StringBuilder sb) {

        OPEN_OBJECT("properties", sb);

        Iterator<String> i = container.getPropertyKeys().iterator();
        while (i.hasNext()) {
            String k = i.next();
            Object v = container.getProperty(k);
            if (v.getClass().isArray()) {
                sb.append(KEY_VALUES(k, convertToIterable(v)));
            } else {
                sb.append(KEY_VALUE(k, v));
            }
            if (i.hasNext()) {
                sb.append(COMMA);
            }
        }

        CLOSE_OBJECT(sb);
    }

}
