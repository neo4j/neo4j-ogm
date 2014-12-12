package org.neo4j.ogm.cypher.compiler;

import org.neo4j.ogm.cypher.statement.ParameterisedStatement;
import org.neo4j.ogm.mapper.MappedRelationship;

import java.util.*;

/**
 * Maintains contextual information throughout the process of compiling Cypher statements to persist a graph of objects.
 */
public class CypherContext {

    private final Map<Object, NodeBuilder> visitedObjects = new HashMap<>();
    private final Map<String, Object> createdObjects = new HashMap<>();
    private final Collection<MappedRelationship> registeredRelationships = new HashSet<>();
    private final Collection<Object> log = new HashSet<>();

    private List<ParameterisedStatement> statements;

    public boolean visited(Object obj) {
        return this.visitedObjects.containsKey(obj);
    }

    public void visit(Object toPersist, NodeBuilder nodeBuilder) {
        this.visitedObjects.put(toPersist, nodeBuilder);
    }

    public void registerRelationship(MappedRelationship mappedRelationship) {
        this.registeredRelationships.add(mappedRelationship);
    }

    public NodeBuilder retrieveNodeBuilderForObject(Object obj) {
        return this.visitedObjects.get(obj);
    }

    public boolean contains(MappedRelationship mappedRelationship) {
        return this.registeredRelationships.contains(mappedRelationship);
    }

    public void setStatements(List<ParameterisedStatement> statements) {
        this.statements = statements;
    }

    public List<ParameterisedStatement> getStatements() {
        return this.statements;
    }

    public void registerNewObject(String cypherName, Object toPersist) {
        createdObjects.put(cypherName, toPersist);
    }

    public Object getNewObject(String cypherName) {
        return createdObjects.get(cypherName);
    }

    public Collection<MappedRelationship> registeredRelationships() {
        return registeredRelationships;
    }

    public void log(Object object) {
        log.add(object);
    }

    public Collection<Object> log() {
        return log;
    }
}