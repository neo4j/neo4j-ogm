package org.neo4j.ogm.cypher.compiler;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to compile Cypher that holds information about a relationship, although it does not have any knowledge about its start
 * and end nodes.
 */
public abstract class RelationshipBuilder implements CypherEmitter {

    String type;
    private String direction;
    final Map<String, Object> props = new HashMap<>();
    final String reference;

    protected RelationshipBuilder(String variableName) {
        this.reference = variableName;
    }

    public String getType() {
        return this.type;
    }

    public RelationshipBuilder type(String type) {
        this.type = type;
        return this;
    }

    public void addProperty(String propertyName, Object propertyValue) {
        this.props.put(propertyName, propertyValue);
    }

    public RelationshipBuilder direction(String direction) {
        this.direction = direction;
        return this;
    }

    public boolean hasDirection(String direction) {
        return this.direction != null && this.direction.equals(direction);
    }

    public abstract void relate(String startNodeIdentifier, String endNodeIdentifier);

}
