/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.response.model;

import org.neo4j.ogm.model.GraphRowModel;

/**
 * Represents a single row in a query response which returns both graph and row data.
 *
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class DefaultGraphRowModel implements GraphRowModel {

    private DefaultGraphModel graph;
    private Object[] row;

    public DefaultGraphRowModel(DefaultGraphModel graph, Object[] row) {
        this.graph = graph;
        this.row = row;
    }

    public DefaultGraphModel getGraph() {
        return graph;
    }

    public Object[] getRow() {
        return row;
    }
}
