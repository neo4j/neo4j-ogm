package org.neo4j.ogm.driver.embedded;

import org.neo4j.graphdb.*;
import org.neo4j.ogm.session.response.adapter.ResponseAdapter;

import java.util.Iterator;
import java.util.Map;

/**
 * This adapter will transform an embedded response into a json response
 *
 * @author vince
 */
public class RowModelAdapter extends ModelAdapter implements ResponseAdapter<Result, String> {

    /**
     * Reads the next row from the result object and transforms it into a JSON representation
     * compatible with the "row" type response from Neo's Http transactional end point.
     *
     * @param result
     * @return
     */
    public String adapt(Result result) {

        StringBuilder sb = new StringBuilder();
        Map<String, Object> data = result.next();

        OPEN_OBJECT(sb);

        OPEN_ARRAY("row", sb);

        Iterator<Map.Entry<String, Object>> iter = data.entrySet().iterator();

        while (iter.hasNext()) {

            Map.Entry mapEntry = iter.next();
            if (mapEntry.getValue() instanceof Long) {
                buildIntegral((Long) mapEntry.getValue(), sb);
            }
            else {
                throw new RuntimeException("Not handled: " + mapEntry.getValue().getClass());
            }
            if (iter.hasNext()) {
                sb.append(COMMA);
            }
        }

        CLOSE_ARRAY(sb);

        CLOSE_OBJECT(sb);


        return sb.toString();
    }

    private void buildIntegral(Long value, StringBuilder sb) {
        sb.append(value.toString());
    }

//    private void build(Path path, StringBuilder sb) {
//
//        StringBuilder nodes = new StringBuilder();
//        StringBuilder edges = new StringBuilder();
//
//        OPEN_ARRAY("nodes", nodes);
//        OPEN_ARRAY("relationships", edges);
//
//        Iterator<Relationship> relIterator = path.relationships().iterator();
//
//        while (relIterator.hasNext()) {
//
//            Relationship rel = relIterator.next();
//
//            buildNode(rel.getStartNode(), nodes);
//            nodes.append(COMMA);
//            buildNode(rel.getEndNode(), nodes);
//
//            buildRelationship(rel, edges);
//
//            if (relIterator.hasNext()) {
//                nodes.append(COMMA);
//                edges.append(COMMA);
//            }
//        }
//
//        CLOSE_ARRAY(nodes);
//        CLOSE_ARRAY(edges);
//
//        sb.append(nodes);
//        sb.append(COMMA);
//        sb.append(edges);
//    }
//
//    private void buildNode(Node node, StringBuilder sb) {
//
//        sb.append(OPEN_BRACE);
//
//        sb.append(KEY_VALUE("id", String.valueOf(node.getId())));  // cypher returns this as a quoted String
//        sb.append(COMMA);
//
//        sb.append(KEY_VALUES("labels", node.getLabels()));
//        sb.append(COMMA);
//
//        buildProperties(node, sb);
//
//        sb.append(CLOSE_BRACE);
//
//    }
//
//    private void buildRelationship(Relationship relationship, StringBuilder sb) {
//
//        sb.append(OPEN_BRACE);
//
//        sb.append(KEY_VALUE("id", String.valueOf(relationship.getId())));
//        sb.append(COMMA);
//
//        sb.append(KEY_VALUE("type", relationship.getType().name()));
//        sb.append(COMMA);
//
//        sb.append(KEY_VALUE("startNode", String.valueOf(relationship.getStartNode().getId())));
//        sb.append(COMMA);
//
//        sb.append(KEY_VALUE("endNode", String.valueOf(relationship.getEndNode().getId())));
//        sb.append(COMMA);
//
//        buildProperties(relationship, sb);
//
//        sb.append(CLOSE_BRACE);
//
//    }
//
//    private void buildProperties(PropertyContainer container, StringBuilder sb) {
//
//        OPEN_OBJECT("properties", sb);
//
//        Iterator<String> i = container.getPropertyKeys().iterator();
//        while (i.hasNext()) {
//            String k = i.next();
//            Object v = container.getProperty(k);
//            if (v.getClass().isArray()) {
//                sb.append(KEY_VALUES(k, convertToIterable(v)));
//            } else {
//                sb.append(KEY_VALUE(k, v));
//            }
//            if (i.hasNext()) {
//                sb.append(COMMA);
//            }
//        }
//
//        CLOSE_OBJECT(sb);
//    }

}
