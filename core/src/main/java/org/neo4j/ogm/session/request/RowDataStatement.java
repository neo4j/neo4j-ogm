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
package org.neo4j.ogm.session.request;

import java.util.Map;
import java.util.Optional;

import org.neo4j.ogm.request.OptimisticLockingConfig;
import org.neo4j.ogm.request.Statement;

/**
 * @author Luanne Misquitta
 */
public class RowDataStatement implements Statement {

    private String statement;
    private OptimisticLockingConfig optimisticLockingConfig;
    private Map<String, Object> parameters;
    private String[] resultDataContents = new String[] { "row" };

    public RowDataStatement() {
    }

    public RowDataStatement(String statement, Map<String, Object> parameters) {
        this.statement = statement;
        this.parameters = parameters;
    }

    public RowDataStatement(String statement, Map<String, Object> parameters,
        OptimisticLockingConfig optimisticLockingConfig) {

        this.statement = statement;
        this.parameters = parameters;
        this.optimisticLockingConfig = optimisticLockingConfig;
    }

    @Override
    public String getStatement() {
        return statement;
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public String[] getResultDataContents() {
        return resultDataContents;
    }

    @Override
    public boolean isIncludeStats() {
        return false;
    }

    @Override
    public Optional<OptimisticLockingConfig> optimisticLockingConfig() {
        return Optional.ofNullable(optimisticLockingConfig);
    }
}
