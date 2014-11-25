package org.neo4j.ogm.cypher.compiler;

import org.neo4j.ogm.metadata.info.ClassInfo;

import java.util.*;

public abstract class NodeBuilder implements CypherEmitter {

    private final String cypherReference;

    protected final Map<String, Object> props = new HashMap<>();
    protected final List<String> labels = new ArrayList<>();

    /**
     * Constructs a new {@link NodeBuilder} identified by the named variable in the context of its enclosing Cypher
     * query.
     *
     * @param variableName The name of the variable to use
     */
    NodeBuilder(String variableName) {
        this.cypherReference = variableName;
    }

    public NodeBuilder addLabel(String labelName) {
        this.labels.add(labelName);
        return this;
    }


    public NodeBuilder addProperty(String propertyName, Object value) {
        this.props.put(propertyName, value);
        return this;
    }

    public NodeBuilder addLabels(Iterable<String> labelName) {
        for (String label : labelName) {
            addLabel(label);
        }
        return this;
    }

    public abstract NodeBuilder mapProperties(Object toPersist, ClassInfo classInfo);

    @Override
    public String toString() {
        return this.labels + "(" + this.props + ')';
    }

    public static String toCsv(Iterable<String> elements) {
        StringBuilder sb = new StringBuilder();
        for (String element : elements) {
            sb.append(element).append(',');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public String reference() {
        return cypherReference;
    }
}
