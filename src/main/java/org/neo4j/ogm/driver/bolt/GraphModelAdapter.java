package org.neo4j.ogm.driver.bolt;

import org.neo4j.driver.*;
import org.neo4j.ogm.driver.JsonAdapter;
import org.neo4j.ogm.session.result.GraphModelResult;
import org.neo4j.ogm.session.result.ResultAdapter;
import org.neo4j.ogm.session.result.ResultProcessingException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author vince
 */
public class GraphModelAdapter extends JsonAdapter implements ResultAdapter<Result, GraphModelResult> {

    @Override
    public GraphModelResult adapt(Result response) {

        StringBuilder sb = new StringBuilder();

        OPEN_OBJECT(sb);
        OPEN_OBJECT("graph", sb);

        Value value = response.get(0);
        //Iterator<Value> iterator = response.fieldNames();

        //while (iterator.hasNext()) {
        //    Value item = iterator.next();

            Value item = response.get(0);

            if (item.isPath()) {
                build(item.asPath(), sb);
            }

            else if (item.isNode()) {
                build(item.asNode(), sb);
            }

            else if (item.isRelationship()) {
                build(item.asRelationship(), sb);
            }

            else {
                throw new RuntimeException("Not handled: " + item);
            }

            //if (iterator.hasNext()) {
            //    sb.append(COMMA);
            //}
        //}

        CLOSE_OBJECT(sb);
        CLOSE_OBJECT(sb);

        try {
            return mapper.readValue(sb.toString().getBytes(), GraphModelResult.class);
        } catch (Exception e) {
            throw new ResultProcessingException("Could not parse response", e);
        }

    }

    private void build(Path path, StringBuilder sb) {

        StringBuilder nodes = new StringBuilder();
        StringBuilder edges = new StringBuilder();

        OPEN_ARRAY("nodes", nodes);
        OPEN_ARRAY("relationships", edges);

        Iterator<Node> nodeIterator = path.nodes().iterator();
        Iterator<Relationship> relationshipIterator = path.relationships().iterator();

        while (nodeIterator.hasNext()) {
            Node node = nodeIterator.next();
            build(node, nodes);
            if (nodeIterator.hasNext()) {
                nodes.append(COMMA);
            }
        }

        while (relationshipIterator.hasNext()) {
            Relationship relationship = relationshipIterator.next();
            build(relationship, edges);
            if (relationshipIterator.hasNext()) {
                edges.append(COMMA);
            }
        }

        CLOSE_ARRAY(nodes);
        CLOSE_ARRAY(edges);

        sb.append(nodes);
        sb.append(COMMA);
        sb.append(edges);
    }

    private void build(Node node, StringBuilder sb) {

        sb.append(OPEN_BRACE);

        sb.append(KEY_VALUE("id", String.valueOf(node.identity()).substring(5)));
        sb.append(COMMA);

        sb.append(KEY_VALUES("labels", node.labels()));
        sb.append(COMMA);

        buildProperties(node, sb);

        sb.append(CLOSE_BRACE);

    }

    private void build(Relationship relationship, StringBuilder sb) {

        sb.append(OPEN_BRACE);

        sb.append(KEY_VALUE("id", String.valueOf(relationship.identity()).substring(4)));
        sb.append(COMMA);

        sb.append(KEY_VALUE("type", relationship.type()));
        sb.append(COMMA);

        sb.append(KEY_VALUE("startNode", String.valueOf(relationship.start()).substring(5)));
        sb.append(COMMA);

        sb.append(KEY_VALUE("endNode", String.valueOf(relationship.end()).substring(5)));
        sb.append(COMMA);

        buildProperties(relationship, sb);

        sb.append(CLOSE_BRACE);

    }

    private void buildProperties(Entity container, StringBuilder sb) {

        OPEN_OBJECT("properties", sb);

        Iterator<String> i = container.propertyKeys().iterator();
        while (i.hasNext()) {
            String k = i.next();
            Value v = container.property(k);

            if (v.isList()) {
                List<Object> values = new ArrayList();
                Iterator<Value> items = v.iterator();

                while (items.hasNext()) {
                    values.add(underlying(items.next()));
                }
                sb.append(KEY_VALUES(k, values));
            }
            else {
                sb.append(KEY_VALUE(k, underlying(v)));
            }
            if (i.hasNext()) {
                sb.append(COMMA);
            }
        }

        CLOSE_OBJECT(sb);
    }

    private Object underlying(Value item) {

        if (item.isInteger()) {
             return item.javaLong();
        }
        if (item.isText()) {
             return item.javaString();
        }
        else {
            throw new RuntimeException("Type not handled: " + item);
        }
    }

}
