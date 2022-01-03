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
package org.neo4j.ogm.session.delegates;

import java.io.Serializable;
import java.util.Optional;

import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.context.GraphRowModelMapper;
import org.neo4j.ogm.cypher.query.DefaultGraphModelRequest;
import org.neo4j.ogm.cypher.query.PagingAndSortingQuery;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.request.GraphModelRequest;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.request.strategy.QueryStatements;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Mark Angrish
 * @author Michael J. Simons
 */
public class LoadOneDelegate extends SessionDelegate {

    private static final Logger logger = LoggerFactory.getLogger(LoadOneDelegate.class);

    public LoadOneDelegate(Neo4jSession session) {
        super(session);
    }

    public <T, ID extends Serializable> T load(Class<T> type, ID id) {
        return load(type, id, 1);
    }

    public <T, ID extends Serializable> T load(Class<T> type, ID id, int depth) {

        ClassInfo classInfo = session.metaData().classInfo(type.getName());
        if (classInfo == null) {
            throw new IllegalArgumentException(type + " is not a managed entity.");
        }
        final FieldInfo primaryIndexField = classInfo.primaryIndexField();
        if (primaryIndexField != null && !primaryIndexField.isTypeOf(id.getClass())) {
            throw new IllegalArgumentException(
                "Supplied id does not match primary index type on supplied class " + type.getName());
        }

        if (primaryIndexField == null && !(id instanceof Long)) {
            throw new IllegalArgumentException("Supplied id must be of type Long (native graph id) when supplied class "
                + "does not have primary id " + type.getName());
        }

        Optional<String> labelsOrType = session.determineLabelsOrTypeForLoading(type);
        if (!labelsOrType.isPresent()) {
            logger.warn("Unable to find database label for entity " + type.getName()
                + " : no results will be returned. Make sure the class is registered, "
                + "and not abstract without @NodeEntity annotation");
            return null;
        }

        QueryStatements<ID> queryStatements = session.queryStatementsFor(type, depth);

        PagingAndSortingQuery qry = queryStatements.findOneByType(labelsOrType.get(), convertIfNeeded(classInfo, id), depth);

        GraphModelRequest request = new DefaultGraphModelRequest(qry.getStatement(), qry.getParameters());

        return session.doInTransaction(() -> {
            try (Response<GraphModel> response = session.requestHandler().execute(request)) {
                new GraphRowModelMapper(session.metaData(), session.context(), session.getEntityInstantiator())
                    .map(type, response);
                return lookup(type, id);
            }
        }, Transaction.Type.READ_ONLY);
    }

    private <T, U> T lookup(Class<T> type, U id) {
        Object ref;
        ClassInfo typeInfo = session.metaData().classInfo(type.getName());

        FieldInfo primaryIndex = typeInfo.primaryIndexField();
        if (typeInfo.annotationsInfo().get(RelationshipEntity.class) == null) {
            if (primaryIndex == null) {
                ref = session.context().getNodeEntity((Long) id);
            } else {
                ref = session.context().getNodeEntityById(typeInfo, id);
            }
        } else {
            if (primaryIndex == null) {
                // Coercing to Long. identityField.convertedType() yields no parameterised type to call cast() with.
                // But we know this will always be Long.
                ref = session.context().getRelationshipEntity((Long) id);
            } else {
                ref = session.context().getRelationshipEntityById(typeInfo, id);
            }
        }
        try {
            return type.cast(ref);
        } catch (ClassCastException cce) {
            logger.warn("Could not cast entity {} for id {} to {}", ref, id, type);
            return null;
        }
    }

}
