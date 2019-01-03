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
package org.neo4j.ogm.session.request;

import java.util.Map;

import org.neo4j.ogm.request.OptimisticLockingConfig;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.request.StatementFactory;

/**
 * Statement factory that is used to modify cypher statements before they are sent to the cypher compiler.
 *
 * @author Michael J. Simons
 */
class CypherModifyingStatementFactory implements StatementFactory {
    private final RowStatementFactory delegate;

    public CypherModifyingStatementFactory(RowStatementFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public Statement statement(String statement, Map<String, Object> parameters) {
        return delegate.statement(applyExtensions(statement), parameters);
    }

    @Override
    public RowDataStatement statement(String statement, Map<String, Object> parameters,
        OptimisticLockingConfig config) {
        return delegate.statement(applyExtensions(statement), parameters, config);
    }

    String applyExtensions(String statement) {
        return "schwupp";
    }
}
