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
package org.neo4j.ogm.context;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.model.GraphRowModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.EntityInstantiator;

import static java.util.stream.Collectors.*;

/**
 * @author Vince Bickers
 * @author Michael J. Simons
 */
public class GraphRowListModelMapper implements ResponseMapper<GraphRowListModel> {

    private final GraphEntityMapper delegate;

    public GraphRowListModelMapper(MetaData metaData, MappingContext mappingContext,
        EntityInstantiator entityInstantiator) {

        this.delegate = new GraphEntityMapper(metaData, mappingContext, entityInstantiator);
    }

    public <T> Iterable<T> map(Class<T> type, Response<GraphRowListModel> response) {
        Set<Long> idsOfResultEntities = new LinkedHashSet<>();

        Response<GraphModel> graphResponse = new Response<GraphModel>() {

            GraphRowListModel currentIteratedModel;
            int currentIndex = 0;

            @Override
            public GraphModel next() {
                if (currentIteratedModel == null) {
                    currentIteratedModel = response.next();

                    if (currentIteratedModel == null) {
                        return null;
                    }
                    currentIndex = 0;
                }

                List<GraphRowModel> listOfRowModels = currentIteratedModel.model();
                if (listOfRowModels.size() <= currentIndex) {
                    currentIteratedModel = null;
                    return next();
                }
                GraphRowModel graphRowModel = listOfRowModels.get(currentIndex++);

                Set<Long> idsInCurrentRow = Arrays.stream(graphRowModel.getRow())
                    .filter(Number.class::isInstance)
                    .map(Number.class::cast)
                    .map(Number::longValue)
                    .collect(toSet());

                idsOfResultEntities.addAll(idsInCurrentRow);

                return graphRowModel.getGraph();
            }

            @Override
            public void close() {
                response.close();
            }

            @Override
            public String[] columns() {
                return response.columns();
            }
        };

        // although it looks like that the `idsOfResultEntities` will stay empty, they won't, trust us.
        BiFunction<GraphModel, Long, Boolean> includeModelObject =
            (graphModel, nativeId) -> idsOfResultEntities.contains(nativeId);

        return delegate.map(type, graphResponse, includeModelObject);
    }
}
