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
package org.neo4j.ogm.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.neo4j.ogm.model.QueryStatistics;

/**
 * @author Vince Bickers
 */
public interface Response<T> extends AutoCloseable {

    T next();

    /**
     * Convert remaining items in this response to list
     * This might be used to materialize whole response for checking number of results, allowing to close transaction
     * etc.
     * Doesn't call {@link #close()}.
     */
    default List<T> toList() {
        ArrayList<T> models = new ArrayList<>();
        T model;
        while ((model = next()) != null) {
            models.add(model);
        }
        return models;
    }

    void close();

    String[] columns();

    /**
     * Responses that contain statistics can hook into here to return.
     *
     * @return An empty optional containing no statistics.
     * @since 3.2
     */
    default Optional<QueryStatistics> getStatistics() {
        return Optional.empty();
    }
}
