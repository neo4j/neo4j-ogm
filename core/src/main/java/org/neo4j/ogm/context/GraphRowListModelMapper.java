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

import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.model.GraphRowModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.EntityInstantiator;

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

        // Retrieve all the row models
        List<GraphRowModel> listOfRowModels = response.toList()
            .stream()
            .flatMap(rowsModel -> rowsModel.model().stream())
            .collect(toList());

        response.close();

        // Extract the graph models and the ids of all result entities
        // I guess those are the entities that are clearly identified by
        // something in the return clause.
        List<GraphModel> listOfGraphModels = new ArrayList<>();
        Set<Long> idsOfResultEntities = new LinkedHashSet<>();

        listOfRowModels.forEach(graphRowModel -> {

            Set<Long> idsInCurrentRow = Arrays.stream(graphRowModel.getRow())
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .map(Number::longValue)
                .collect(toSet());

            listOfGraphModels.add(graphRowModel.getGraph());
            idsOfResultEntities.addAll(idsInCurrentRow);
        });

        // The order map here contains the ids of the top level nodes as they have appeared in the result.
        // Top level meaning here nodes that should be returned in the result set, not their related nodes.
        Map<Long, Long> order = new HashMap<>();
        AtomicLong index = new AtomicLong(0L);
        idsOfResultEntities.forEach(id -> order.put(id, index.getAndIncrement()));

        BiFunction<GraphModel, Long, Boolean> includeModelObject =
            (graphModel, nativeId) -> idsOfResultEntities.contains(nativeId);
        return delegate.map(type, listOfGraphModels, includeModelObject, Collections.unmodifiableMap(order));
    }
}
