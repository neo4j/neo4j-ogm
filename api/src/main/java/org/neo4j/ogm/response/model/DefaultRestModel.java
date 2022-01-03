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

import java.util.Map;
import java.util.Optional;

import org.neo4j.ogm.model.RestModel;

/**
 * The results of a query, modelled as rest response data.
 *
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class DefaultRestModel implements RestModel {

    private final Map<String, Object> row;

    /**
     * Creates a new {@link RestModel} based on the given row. If the row is empty or {@literal null}, then
     * the no model is returned but an empty {@link java.util.Optional}.
     *
     * @param row The row this model is based on, maybe {@literal null}
     * @return An optional {@link RestModel}
     */
    public static Optional<DefaultRestModel> basedOn(Map<String, Object> row) {

        if (row == null || row.isEmpty()) {
            return Optional.empty();
        }

        DefaultRestModel restModel = new DefaultRestModel(row);
        return Optional.of(restModel);
    }

    private DefaultRestModel(Map<String, Object> row) {
        this.row = row;
    }

    @Override
    public Map<String, Object> getRow() {
        return row;
    }
}
