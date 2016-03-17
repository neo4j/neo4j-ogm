/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.GraphRowModel;

/**
 * Represents a single row in a query response which returns both graph and row data.
 *
 * @author Luanne Misquitta
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class DefaultGraphRowModel implements GraphRowModel {

    public DefaultGraphRowModel() {
    }

    private DefaultGraphModel graph;
    private Object[] row;

    public DefaultGraphRowModel(GraphModel graph, Object[] row) {
        this.graph = (DefaultGraphModel)graph;
        this.row = row;
    }

    public org.neo4j.ogm.model.GraphModel getGraph() {
        return graph;
    }

    public Object[] getRow() {
        return row;
    }
}
