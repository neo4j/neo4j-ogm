/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

package org.neo4j.ogm.response.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.neo4j.ogm.model.Edge;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.Node;

/**
 * The results of a query, modelled as graph data.
 *
 * @author Michal Bachman
 */
public class DefaultGraphModel implements GraphModel {

    private final Set<Node> nodes = new LinkedHashSet<>();
    private final Set<Node> projectedNodes = new HashSet<>();
    private final Set<Edge> relationships = new LinkedHashSet<>();

    @Override
    public Set<Node> getNodes() {
        return nodes;
    }

    public void setNodes(NodeModel[] nodes) {
        Collections.addAll(this.nodes, nodes);
    }

    @Override
    public Set<Node> getProjectedNodes() {
        return projectedNodes;
    }

    @Override
    public Set<Edge> getRelationships() {
        return relationships;
    }

    public void setRelationships(RelationshipModel[] relationships) {
        Collections.addAll(this.relationships, relationships);
    }
}
