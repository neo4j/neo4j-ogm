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
 * @author Michael J. Simons
 */
public class NodeDeleteStatements implements DeleteStatements {

    @Override
    public CypherQuery delete(Long id) {
        return new DefaultRowModelRequest("MATCH (n) WHERE ID(n) = $id OPTIONAL MATCH (n)-[r0]-() DELETE r0, n",
            Collections.singletonMap("id", id));
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
        params.put("type", "node");

        return new DefaultRowModelRequest("MATCH (n) "
            + "  WHERE id(n) = $id AND n.`" + versionField.property() + "` = $version "
            + "SET "
            + " n.`" + versionField.property() + "` = n.`" + versionField.property() + "` + 1 "
            + "WITH n "
            + " WHERE n.`" + versionField.property() + "` = $version + 1 "
            + "OPTIONAL MATCH (n)-[r0]-() "
            + "DELETE r0, n "
            + "RETURN DISTINCT id(n) AS id", // Use DISTINCT because node may have multiple relationships
            params,
            optimisticLockingConfig);

    }

    @Override
    public CypherQuery delete(Collection<Long> ids) {
        return new DefaultRowModelRequest("MATCH (n) WHERE ID(n) in $ids OPTIONAL MATCH (n)-[r0]-() DELETE r0, n",
            Collections.singletonMap("ids", ids));
    }

    @Override
    public CypherQuery deleteAll() {
        return new DefaultRowModelRequest("MATCH (n) OPTIONAL MATCH (n)-[r0]-() DELETE r0, n", Collections.emptyMap());
    }

    @Override
    public CypherQuery delete(String label) {
        return new DefaultRowModelRequest(
            String.format("MATCH (n:`%s`) OPTIONAL MATCH (n)-[r0]-() DELETE r0, n", label), Collections.emptyMap());
    }

    @Override
    public CypherQuery delete(String label, Iterable<Filter> filters) {
        FilteredQuery query = FilteredQueryBuilder.buildNodeQuery(label, filters);
        query.setReturnClause(" OPTIONAL MATCH (n)-[r0]-() DELETE r0, n");
        return new DefaultRowModelRequest(query.statement(), query.parameters());
    }

    @Override
    public CypherQuery deleteAndList(String label, Iterable<Filter> filters) {
        FilteredQuery query = FilteredQueryBuilder.buildNodeQuery(label, filters);
        query.setReturnClause(" OPTIONAL MATCH (n)-[r0]-() DELETE r0, n RETURN ID(n)");
        return new DefaultRowModelRequest(query.statement(), query.parameters());
    }
}
