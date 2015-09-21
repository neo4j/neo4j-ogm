package org.neo4j.ogm.driver.embedded;

import org.neo4j.graphdb.*;
import org.neo4j.ogm.session.response.ResponseAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This adapter will transform an embedded response into a json response
 *
 * @author vince
 */
public class GraphResponseAdapter implements ResponseAdapter<String, Result> {

    private static final String QUOTE = "\"";
    private static final String OPEN_BRACE = "{";
    private static final String CLOSE_BRACE = "}";
    private static final String OPEN_BRACKET = "[";
    private static final String CLOSE_BRACKET = "]";
    private static final String COMMA = ",";
    private static final String COLON = ":";
    private static final String SPACE = " ";

    /**
     * Reads the next row from the result object and transforms it into a JSON representation
     * compatible with the "graph" type response from Neo's Http transactional end point.
     *
     * @param result
     * @return
     */
    public String adapt(Result result) {

        StringBuilder sb = new StringBuilder();
        Map<String, Object> data = result.next();

        OPEN_OBJECT(sb);

        OPEN_OBJECT("graph", sb);

        for (Map.Entry mapEntry : data.entrySet()) {
            if (mapEntry.getValue() instanceof Path) {
                graphTransform((Path) mapEntry.getValue(), sb);
            }
            else if (mapEntry.getValue() instanceof Node) {
                graphTransform((Node) mapEntry.getValue(), sb);
            }
            else if (mapEntry.getValue() instanceof Relationship) {
                graphTransform((Relationship) mapEntry.getValue(), sb);
            }
            else {
                throw new RuntimeException("Not handled: " + mapEntry.getValue().getClass());
            }
        }

        CLOSE_OBJECT(sb);

        CLOSE_OBJECT(sb);

        return sb.toString();
    }

    private void graphTransform(Path path, StringBuilder sb) {

        StringBuilder nodes = new StringBuilder();
        StringBuilder edges = new StringBuilder();

        OPEN_ARRAY("nodes", nodes);
        OPEN_ARRAY("relationships", edges);

        Iterator<Relationship> relIterator = path.relationships().iterator();

        while (relIterator.hasNext()) {

            Relationship rel = relIterator.next();

            graphTransform(rel.getStartNode(), nodes);
            nodes.append(COMMA);
            graphTransform(rel.getEndNode(), nodes);

            graphTransform(rel, edges);

            if (relIterator.hasNext()) {
                nodes.append(COMMA);
                edges.append(COMMA);
            }
        }

        CLOSE_ARRAY(nodes);
        CLOSE_ARRAY(edges);

        sb.append(nodes);
        sb.append(COMMA);
        sb.append(edges);
    }

    private void graphTransform(Node node, StringBuilder sb) {

        sb.append(OPEN_BRACE);

        sb.append(KEY_VALUE("id", String.valueOf(node.getId())));  // cypher returns this as a quoted String
        sb.append(COMMA);

        sb.append(KEY_VALUES("labels", node.getLabels()));
        sb.append(COMMA);

        graphTransformProperties(node, sb);

        sb.append(CLOSE_BRACE);

    }

    private void graphTransform(Relationship relationship, StringBuilder sb) {

        sb.append(OPEN_BRACE);

        sb.append(KEY_VALUE("id", String.valueOf(relationship.getId())));
        sb.append(COMMA);

        sb.append(KEY_VALUE("type", relationship.getType().name()));
        sb.append(COMMA);

        sb.append(KEY_VALUE("startNode", String.valueOf(relationship.getStartNode().getId())));
        sb.append(COMMA);

        sb.append(KEY_VALUE("endNode", String.valueOf(relationship.getEndNode().getId())));
        sb.append(COMMA);

        graphTransformProperties(relationship, sb);

        sb.append(CLOSE_BRACE);

    }

    private void graphTransformProperties(PropertyContainer container, StringBuilder sb) {

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


    private static void OPEN_OBJECT(StringBuilder sb) {
        sb.append(OPEN_BRACE);
    }

    private static void OPEN_OBJECT(String name, StringBuilder sb) {
        sb.append(KEY(name));
        sb.append(OPEN_BRACE);
    }

    private static void CLOSE_OBJECT(StringBuilder sb) {
        sb.append(CLOSE_BRACE);
    }

    private static void OPEN_ARRAY(String name, StringBuilder sb) {
        sb.append(KEY(name));
        sb.append(OPEN_BRACKET);
    }

    private static void CLOSE_ARRAY(StringBuilder sb) {
        sb.append(CLOSE_BRACKET);
    }

    private static final String quoted(String s) {
        return QUOTE.concat(s).concat(QUOTE);
    }

    private static final String KEY(String k) {
        return quoted(k).concat(COLON).concat(SPACE);
    }

    private static final String VALUE(Object v) {
        if (v instanceof String || v instanceof Label) {
            return quoted(v.toString());
        } else {
            return v.toString();
        }
    }

    private static final String KEY_VALUE(String k, Object v) {
        return KEY(k).concat(VALUE(v));
    }

    private static final String KEY_VALUES(String k, Iterable i) {

        String r = KEY(k).concat(OPEN_BRACKET);
        Iterator iter = i.iterator();

        while (iter.hasNext()) {
            Object v = iter.next();
            String s = VALUE(v);
            r = r.concat(s);
            if (iter.hasNext()) {
                r = r.concat(COMMA);
            }
        }
        r = r.concat(CLOSE_BRACKET);
        return r;
    }

    private static Iterable<Object> convertToIterable(Object array) {
        List ar = new ArrayList();
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            ar.add(Array.get(array, i));
        }
        return ar;
    }

}
