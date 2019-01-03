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
package org.neo4j.ogm.context;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.model.GraphRowModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.EntityInstantiator;

/**
 * @author vince
 */
public class GraphRowListModelMapper implements ResponseMapper<GraphRowListModel> {

    private final MetaData metaData;
    private final MappingContext mappingContext;
    private EntityInstantiator entityInstantiator;

    public GraphRowListModelMapper(MetaData metaData, MappingContext mappingContext,
        EntityInstantiator entityInstantiator) {
        this.metaData = metaData;
        this.mappingContext = mappingContext;
        this.entityInstantiator = entityInstantiator;
    }

    public <T> Iterable<T> map(Class<T> type, Response<GraphRowListModel> response) {

        List<T> result = new ArrayList<>();
        Set<Long> resultEntityIds = new LinkedHashSet<>();
        ClassInfo classInfo = metaData.classInfo(type.getName());

        Set<Long> nodeIds = new LinkedHashSet<>();
        Set<Long> edgeIds = new LinkedHashSet<>();
        GraphEntityMapper ogm = new GraphEntityMapper(metaData, mappingContext, entityInstantiator);

        GraphRowListModel graphRowsModel;

        while ((graphRowsModel = response.next()) != null) {
            for (GraphRowModel graphRowModel : graphRowsModel.model()) {
                //Load the GraphModel into the ogm
                ogm.map(type, graphRowModel.getGraph(), nodeIds, edgeIds);
                //Extract the id's of filtered nodes from the rowData and return them
                Object[] rowData = graphRowModel.getRow();
                for (Object data : rowData) {
                    if (data instanceof Number) {
                        resultEntityIds.add(((Number) data).longValue());
                    }
                }
            }
        }
        ogm.executePostLoad(nodeIds, edgeIds);

        if (classInfo.annotationsInfo().get(RelationshipEntity.class) == null) {
            for (Long resultEntityId : resultEntityIds) {
                result.add((T) mappingContext.getNodeEntity(resultEntityId));
            }
        } else {
            for (Long resultEntityId : resultEntityIds) {
                result.add((T) mappingContext.getRelationshipEntity(resultEntityId));
            }
        }
        return result;
    }
}
