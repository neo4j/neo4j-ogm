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

import org.neo4j.ogm.cypher.query.DefaultRowModelRequest;
import org.neo4j.ogm.driver.api.request.RowModelRequest;
import org.neo4j.ogm.driver.api.request.Statement;
import org.neo4j.ogm.driver.api.response.Response;
import org.neo4j.ogm.driver.impl.model.RowModel;
import org.neo4j.ogm.entityaccess.FieldWriter;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.session.Capability;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.request.strategy.DeleteNodeStatements;
import org.neo4j.ogm.session.request.strategy.DeleteRelationshipStatements;
import org.neo4j.ogm.session.request.strategy.DeleteStatements;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * @author Vince Bickers
 */
public class DeleteDelegate implements Capability.Delete {

    private final Neo4jSession session;

    public DeleteDelegate(Neo4jSession neo4jSession) {
        this.session = neo4jSession;
    }

    private DeleteStatements getDeleteStatementsBasedOnType(Class type) {
        if (session.metaData().isRelationshipEntity(type.getName())) {
            return new DeleteRelationshipStatements();
        }
        return new DeleteNodeStatements();
    }


    private <T> void deleteAll(T object) {
        List<T> list;
        if (object.getClass().isArray()) {
            list = Arrays.asList(object);
        } else {
            list = (List<T>) object;
        }
        for (T element : list) {
            delete(element);
        }
    }

    @Override
    public <T> void delete(T object) {
        if (object.getClass().isArray() || Iterable.class.isAssignableFrom(object.getClass())) {
            deleteAll(object);
        } else {
            ClassInfo classInfo = session.metaData().classInfo(object);
            if (classInfo != null) {
                Field identityField = classInfo.getField(classInfo.identityField());
                Long identity = (Long) FieldWriter.read(identityField, object);
                if (identity != null) {
                    Statement request = getDeleteStatementsBasedOnType(object.getClass()).delete(identity);
                    DefaultRowModelRequest query = new DefaultRowModelRequest(request.getStatement(), request.getParameters());
                    try (Response<RowModel> response = session.requestHandler().execute(query)) {
                        session.context().clear(object);
                    }
                }
            } else {
                session.info(object.getClass().getName() + " is not an instance of a persistable class");
            }
        }
    }

    @Override
    public <T> void deleteAll(Class<T> type) {
        ClassInfo classInfo = session.metaData().classInfo(type.getName());
        if (classInfo != null) {
            Statement request = getDeleteStatementsBasedOnType(type).deleteByType(session.entityType(classInfo.name()));
            RowModelRequest query = new DefaultRowModelRequest(request.getStatement(), request.getParameters());
            try (Response<RowModel> response = session.requestHandler().execute(query)) {
                session.context().clear(type);
            }
        } else {
            session.info(type.getName() + " is not a persistable class");
        }
    }


    @Override
    public void purgeDatabase() {
        Statement stmt = new DeleteNodeStatements().purge();
        RowModelRequest query = new DefaultRowModelRequest(stmt.getStatement(), stmt.getParameters());
        session.requestHandler().execute(query).close();
        session.context().clear();
    }

    @Override
    public void clear() {
        session.context().clear();
    }
}
