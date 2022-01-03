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
package org.neo4j.ogm.session.request.strategy.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.query.CypherQuery;
import org.neo4j.ogm.cypher.query.DefaultRowModelRequest;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.request.OptimisticLockingConfig;
import org.neo4j.ogm.session.request.FilteredQuery;
import org.neo4j.ogm.session.request.FilteredQueryBuilder;
import org.neo4j.ogm.session.request.strategy.DeleteStatements;

/**
 * @author Luanne Misquitta
 * @author Jasper Blues
 * @author Michael J. Simons
 */
public class RelationshipDeleteStatements implements DeleteStatements {

    public CypherQuery delete(Long id) {
        return new DefaultRowModelRequest("MATCH (n)-[r0]->() WHERE ID(r0) = $id DELETE r0", Collections
            .singletonMap("id", id));
    }

    @Override
    public CypherQuery delete(Long id, Object object, ClassInfo classInfo) {
        FieldInfo versionField = classInfo.getVersionField();
        Long version = (Long) versionField.read(object);
        OptimisticLockingConfig optimisticLockingConfig = new OptimisticLockingConfig(1,
            classInfo.staticLabels().toArray(new String[] {}), versionField.property());

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("version", version);
        params.put("type", "rel");

        return new DefaultRowModelRequest("MATCH (n)-[r0]->() "
            + "  WHERE ID(r0) = $id AND r0.`" + versionField.property() + "` = $version "
            + "SET "
            + " r0.`" + versionField.property() + "` = r0.`" + versionField.property() + "` + 1 "
            + "WITH r0 "
            + " WHERE r0.`" + versionField.property() + "` = $version + 1 "
            + "DELETE r0 "
            + "RETURN DISTINCT ID(r0) AS id", // Use DISTINCT because node may have multiple relationships
            params, optimisticLockingConfig);
    }

    public CypherQuery delete(Collection<Long> ids) {
        return new DefaultRowModelRequest("MATCH (n)-[r0]->() WHERE ID(r0) IN $ids DELETE r0",
            Collections.singletonMap("ids", ids));
    }

    public CypherQuery deleteAll() {
        return new DefaultRowModelRequest("MATCH (n) OPTIONAL MATCH (n)-[r0]-() DELETE r0", Collections.emptyMap());
    }

    public CypherQuery delete(String type) {
        return new DefaultRowModelRequest(String.format("MATCH (n)-[r0:`%s`]-() DELETE r0", type), Collections.emptyMap());
    }

    @Override
    public CypherQuery delete(String type, Iterable<Filter> filters) {
        FilteredQuery query = FilteredQueryBuilder.buildRelationshipQuery(type, filters);
        query.setReturnClause(" DELETE r0");
        return new DefaultRowModelRequest(query.statement(), query.parameters());
    }

    @Override
    public CypherQuery deleteAndList(String type, Iterable<Filter> filters) {
        FilteredQuery query = FilteredQueryBuilder.buildRelationshipQuery(type, filters);
        query.setReturnClause(" DELETE r0 RETURN ID(r0)");
        return new DefaultRowModelRequest(query.statement(), query.parameters());
    }
}
