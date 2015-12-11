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
package org.neo4j.ogm.session.delegates;

import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.context.GraphEntityMapper;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.request.GraphModelRequest;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.cypher.query.AbstractRequest;
import org.neo4j.ogm.session.Capability;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.request.strategy.QueryStatements;

/**
 * @author Vince Bickers
 */
public class LoadOneDelegate implements Capability.LoadOne {

    private Neo4jSession session;

    public LoadOneDelegate(Neo4jSession session) {
        this.session = session;
    }

    @Override
    public <T> T load(Class<T> type, Long id) {
        return load(type, id, 1);
    }

    @Override
    public <T> T load(Class<T> type, Long id, int depth) {
        QueryStatements queryStatements = session.queryStatementsFor(type);
        AbstractRequest qry = queryStatements.findOne(id,depth);

        try (Response<GraphModel> response = session.requestHandler().execute((GraphModelRequest) qry)) {
            new GraphEntityMapper(session.metaData(), session.context()).map(type, response);
            return lookup(type, id);
        }
    }

    private <T> T lookup(Class<T> type, Long id) {
        Object ref;
        ClassInfo typeInfo = session.metaData().classInfo(type.getName());
        if (typeInfo.annotationsInfo().get(RelationshipEntity.CLASS) == null) {
            ref = session.context().getNodeEntity(id);
        } else {
            ref = session.context().getRelationshipEntity(id);
        }
        try {
            return type.cast(ref);
        }
        catch (ClassCastException cce) {
            return null;
        }

    }

}
