/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.session.response.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.api.compiler.CompileContext;
import org.neo4j.ogm.api.model.Graph;
import org.neo4j.ogm.api.model.GraphRow;
import org.neo4j.ogm.api.model.GraphRows;
import org.neo4j.ogm.api.model.Row;
import org.neo4j.ogm.api.response.Response;
import org.neo4j.ogm.entityaccess.FieldWriter;
import org.neo4j.ogm.mapper.GraphEntityMapper;
import org.neo4j.ogm.mapper.MappedRelationship;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.mapper.TransientRelationship;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;

import java.lang.reflect.Field;
import java.util.*;

/**
 *  @author Vince Bickers
 *  @author Luanne Misquitta
 */
public class SessionResponseHandler implements ResponseHandler {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final MetaData metaData;
    private final MappingContext mappingContext;

    public SessionResponseHandler(MetaData metaData, MappingContext mappingContext) {
        this.metaData = metaData;
        this.mappingContext = mappingContext;
    }

    @Override
    public <T> Collection<T> loadByProperty(Class<T> type, Response<GraphRows> response) {

        Set<T> result = new LinkedHashSet<>();
        Set<Long> resultEntityIds = new LinkedHashSet<>();
        ClassInfo classInfo = metaData.classInfo(type.getName());

        GraphEntityMapper ogm = new GraphEntityMapper(metaData, mappingContext);

        GraphRows graphRowsModel;

        while ((graphRowsModel = response.next()) != null) {
            for (GraphRow graphRowModel : graphRowsModel.model()) {
                //Load the GraphModel into the ogm
                ogm.map(type, graphRowModel.getGraph());
                //Extract the id's of filtered nodes from the rowData and return them
                Object[] rowData = graphRowModel.getRow();
                for (Object data : rowData) {
                    if (data instanceof Number) {
                        resultEntityIds.add(((Number) data).longValue());
                    }
                }
            }
        }

        response.close();

        if (classInfo.annotationsInfo().get(RelationshipEntity.CLASS) == null) {
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

    @Override
    public void updateObjects(CompileContext context, Response<Row> rowModelResponse) {

        String[] variables = rowModelResponse.columns();
        Row rowModel;

        Map<String, Long> directRefMap = new HashMap<>();

        while ((rowModel = rowModelResponse.next()) != null) {
            Object[] results = rowModel.getValues();

            for (int i = 0; i < variables.length; i++) {

                String variable = variables[i];

                // create the mapping between the cypher variable and the newly created domain object's
                // identity, as returned by the database
                Long identity = Long.parseLong(results[i].toString());
                directRefMap.put(variable, identity);

                // find the newly created domain object in the context log
                Object persisted = context.getNewObject(variable);

                if (persisted != null) {  // it will be null if the variable represents a simple relationship.

                    // set the id field of the newly created domain object
                    ClassInfo classInfo = metaData.classInfo(persisted);
                    Field identityField = classInfo.getField(classInfo.identityField());
                    FieldWriter.write(identityField, persisted, identity);

                    // ensure the newly created domain object is added into the mapping context
                    if (classInfo.annotationsInfo().get(RelationshipEntity.CLASS) == null) {
                        mappingContext.registerNodeEntity(persisted, identity);
                    } else {
                        mappingContext.registerRelationshipEntity(persisted, identity);
                    }
                    mappingContext.remember(persisted); //remember the persisted entity so it isn't marked for rewrite just after it's been retrieved and had it's id set

                }
            }
        }

        // finally, all new relationships just established in the graph need to be added to the mapping context.
        if(directRefMap.size() > 0) {
            for (Object object : context.registry()) {
                if (object instanceof TransientRelationship) {
                    MappedRelationship relationship = (((TransientRelationship) object).convert(directRefMap));
                    if(mappingContext.getRelationshipEntity(relationship.getRelationshipId()) == null) {
                        relationship.setRelationshipId(null);
                    }
                    mappingContext.mappedRelationships().add(relationship);
                }
            }
        }

        rowModelResponse.close();
    }

    @Override
    public <T> T loadById(Class<T> type, Response<Graph> response, Long id) {
        GraphEntityMapper ogm = new GraphEntityMapper(metaData, mappingContext);
        Graph graphModel;

        while ((graphModel = response.next()) != null) {
            ogm.map(type, graphModel);
        }
        response.close();
        return lookup(type, id);
    }

    private <T> T lookup(Class<T> type, Long id) {
        Object ref;
        ClassInfo typeInfo = metaData.classInfo(type.getName());
        if (typeInfo.annotationsInfo().get(RelationshipEntity.CLASS) == null) {
            ref = mappingContext.getNodeEntity(id);
        } else {
            ref = mappingContext.getRelationshipEntity(id);
        }
        try {
            return type.cast(ref);
        }
        catch (ClassCastException cce) {
            return null;
        }

    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Response<Graph> response) {

        Set<T> objects = new LinkedHashSet<>();

        GraphEntityMapper ogm = new GraphEntityMapper(metaData, mappingContext);

        Graph graphModel;
        while ((graphModel = response.next()) != null) {
            objects.addAll(ogm.map(type, graphModel));
        }
        response.close();
        return objects;
    }

}
