package org.neo4j.ogm.mapper.cypher;

/**
 * Fluent builder interface to facilitate Cypher query construction for a particular node.
 */
public interface NodeBuilder {

    /**
     * Sets the Neo4j database ID of the node, which should be left as <code>null</code> if the node is new.
     *
     * @param nodeId The node ID
     * @return <code>this</code> for method chaining
     */
    NodeBuilder withId(Long nodeId);

    /**
     * Adds a label with the specific name to the node.
     *
     * @param labelName The name of the label to add
     * @return <code>this</code> for method chaining
     */
    NodeBuilder addLabel(String labelName);

    /**
     * Adds a label for each of the names in the given collection.
     *
     * @param labelNames The names of the labels to add to this node
     * @return <code>this</code> for method chaining
     */
    NodeBuilder addLabels(Iterable<String> labelNames);

    /**
     * Adds a property to the node represented by this {@link NodeBuilder} with the specified name and value.
     *
     * @param propertyName The name of the property to add
     * @param value The value of the property to set
     * @return <code>this</code> for method chaining
     */
    NodeBuilder addProperty(String propertyName, Object value);

}
