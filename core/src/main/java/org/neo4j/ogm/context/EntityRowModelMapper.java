/*
 * Copyright (c) 2002-2020 "Neo4j,"
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

package org.neo4j.ogm.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.Utils;

public class EntityRowModelMapper implements ResponseMapper<RowModel> {

    private static final Set<Class<?>> VOID_TYPES = new HashSet<>(Arrays.asList(Void.class, void.class));

    /**
     * @param <T> The type of entity to which the row is to be mapped
     */
    @Override
    public <T> Iterable<T> map(Class<T> type, Response<RowModel> response) {

        if (VOID_TYPES.contains(type)) {
            return Collections.emptyList();
        }

        Collection<T> result = new ArrayList<>();

        RowModel model;
        while ((model = response.next()) != null) {

            if (model.variables().length > 1) {
                throw new RuntimeException(
                    "Scalar response queries must only return one column. Make sure your cypher query only returns one item.");
            }
            for (int i = 0; i < model.variables().length; i++) {
                Object o = model.getValues()[0];
                result.add((T) Utils.coerceTypes(type, o));
            }
        }
        return result;
    }
}
