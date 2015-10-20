/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.api.response.model;

import org.neo4j.ogm.api.model.Graph;
import org.neo4j.ogm.api.model.GraphRow;

/**
 * Represents a single row in a query response which returns both graph and row data.
 *
 * @author Luanne Misquitta
 */
public class GraphRowModel implements GraphRow {

    private Graph graph;
    private Object[] row;

    public GraphRowModel(Graph graph, Object[] row) {
        this.graph = graph;
        this.row = row;
    }

    public Graph getGraph() {
        return graph;
    }

    public Object[] getRow() {
        return row;
    }
}
