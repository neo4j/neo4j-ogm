package org.neo4j.ogm.mapper.cypher;

import java.util.*;

/**
 * Maintains contextual information throughout the process of building Cypher statements to persist a graph of objects.
 */
public class CypherContext {

    private final Map<Object, NodeBuilder> visitedObjects = new HashMap<>();
    private final Map<String, Object> cypherObjects = new HashMap();
    private final Map<Long, NodeBuilder> nodesById = new HashMap<>();
    private final Collection<RegisteredRelationship> registeredRelationships = new HashSet<>();
    private List<ParameterisedStatement> statements;

    public boolean containsObject(Object obj) {
        return this.visitedObjects.containsKey(obj);
    }

    public void add(Object toPersist, NodeBuilder nodeBuilder) {
        this.visitedObjects.put(toPersist, nodeBuilder);
    }

    public void add(Object toPersist, Long nodeId, NodeBuilder nodeBuilder) {
        this.visitedObjects.put(toPersist, nodeBuilder);
        this.nodesById.put(nodeId, nodeBuilder);
    }

    public NodeBuilder findById(Long nodeId) {
        return this.nodesById.get(nodeId);
    }

    public void registerRelationship(NodeBuilder startNode, String relationshipType, NodeBuilder endNode) {
        this.registeredRelationships.add(new RegisteredRelationship(startNode, relationshipType, endNode));
    }

    public NodeBuilder retrieveNodeBuilderForObject(Object obj) {
        return this.visitedObjects.get(obj);
    }

    public boolean doesNotContainRelationship(Long startNodeId, String relationshipType, Long endNodeId) {
        return !this.registeredRelationships.contains(
                new RegisteredRelationship(findById(startNodeId), relationshipType, findById(endNodeId)));
    }

    public void setStatements(List<ParameterisedStatement> statements) {
        this.statements = statements;
    }

    public List<ParameterisedStatement> getStatements() {
        return this.statements;
    }

    public void registerNewObject(String cypherName, Object toPersist) {
        cypherObjects.put(cypherName, toPersist);
    }

    public Object getNewObject(String cypherName) {
        return cypherObjects.get(cypherName);
    }


    private static final class RegisteredRelationship {

        private final String type;
        private final NodeBuilder startNode;
        private final NodeBuilder endNode;

        RegisteredRelationship(NodeBuilder startNode, String type, NodeBuilder endNode) {
            this.startNode = startNode;
            this.type = type;
            this.endNode = endNode;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.endNode == null) ? 0 : this.endNode.hashCode());
            result = prime * result + ((this.startNode == null) ? 0 : this.startNode.hashCode());
            result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof CypherContext.RegisteredRelationship))
                return false;
            RegisteredRelationship other = (RegisteredRelationship) obj;
            if (this.endNode == null) {
                if (other.endNode != null)
                    return false;
            } else if (!this.endNode.equals(other.endNode))
                return false;
            if (this.startNode == null) {
                if (other.startNode != null)
                    return false;
            } else if (!this.startNode.equals(other.startNode))
                return false;
            if (this.type == null) {
                if (other.type != null)
                    return false;
            } else if (!this.type.equals(other.type))
                return false;
            return true;
        }

    }

}
