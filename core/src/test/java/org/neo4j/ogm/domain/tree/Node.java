package org.neo4j.ogm.domain.tree;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Frantisek Hartman
 */
public class Node {

    Long id;
    String name;

    // defaults to OUTGOING, but not specified on the annotation
    @Relationship(type = "CHILD")
    Set<Node> nodes;

    public Node() {
    }

    public Node(String name) {
        this.name = name;
    }

    public void add(Node node) {
        if (nodes == null) {
            nodes = new HashSet<>();
        }
        nodes.add(node);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Node> getNodes() {
        return nodes;
    }
}
