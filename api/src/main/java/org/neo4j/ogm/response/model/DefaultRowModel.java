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

import org.neo4j.ogm.model.RowModel;

/**
 * The results of a query, modelled as row data.
 *
 * @author Vince Bickers
 */
public class DefaultRowModel implements RowModel {

    private final Object[] values;
    private final String[] variables;

    public DefaultRowModel(Object[] values, String[] variables) {
        this.values = values;
        this.variables = variables;
    }

    public Object[] getValues() {
        return values;
    }

    public String[] variables() {
        return variables;
    }
}
