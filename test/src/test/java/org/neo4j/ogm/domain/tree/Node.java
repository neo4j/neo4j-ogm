/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

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
