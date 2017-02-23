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

package org.neo4j.ogm.context;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.model.GraphRowModel;
import org.neo4j.ogm.response.Response;

/**
 * @author vince
 */
public class GraphRowListModelMapper implements ResponseMapper<GraphRowListModel> {

    private final MetaData metaData;
    private final MappingContext mappingContext;

    public GraphRowListModelMapper(MetaData metaData, MappingContext mappingContext) {
        this.metaData = metaData;
        this.mappingContext = mappingContext;
    }

    public <T> Iterable<T> map(Class<T> type, Response<GraphRowListModel> response) {

        List<T> result = new ArrayList<>();
        Set<Long> resultEntityIds = new LinkedHashSet<>();
        ClassInfo classInfo = metaData.classInfo(type.getName());

        Set<Long> nodeIds = new LinkedHashSet<>();
        Set<Long> edgeIds = new LinkedHashSet<>();
        GraphEntityMapper ogm = new GraphEntityMapper(metaData, mappingContext);

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

        if (classInfo.annotationsInfo().get(RelationshipEntity.class) == null) {
            for(Long resultEntityId : resultEntityIds) {
                result.add((T) mappingContext.getNodeEntity(resultEntityId));
            }

        }
        else {
            for(Long resultEntityId : resultEntityIds) {
                result.add((T) mappingContext.getRelationshipEntity(resultEntityId));
            }
        }
        return result;
    }
}
