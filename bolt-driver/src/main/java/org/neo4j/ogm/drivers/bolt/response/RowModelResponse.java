/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.ogm.drivers.bolt.response;

import java.util.Arrays;

import org.neo4j.driver.StatementResult;
import org.neo4j.ogm.drivers.bolt.driver.BoltEntityAdapter;
import org.neo4j.ogm.model.RowModel;

/**
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class RowModelResponse extends BoltResponse<RowModel> {

    private final BoltRowModelAdapter adapter;

    public RowModelResponse(StatementResult result, BoltEntityAdapter entityAdapter) {

        super(result);

        this.adapter = new BoltRowModelAdapter(entityAdapter);
        this.adapter.setColumns(Arrays.asList(columns()));
    }

    @Override
    public RowModel fetchNext() {
        if (result.hasNext()) {
            return adapter.adapt(result.next().asMap());
        }
        return null;
    }
}
