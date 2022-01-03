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
package org.neo4j.ogm.cypher.query;

import java.util.Map;

import org.neo4j.ogm.request.RestModelRequest;

/**
 * @author Luanne Misquitta
 */
public class DefaultRestModelRequest extends CypherQuery implements RestModelRequest {

    private final static String[] resultDataContents = new String[] { "rest" };

    public DefaultRestModelRequest(String cypher, Map<String, ?> parameters) {
        super(cypher, parameters);
    }

    @Override
    public String[] getResultDataContents() {
        return resultDataContents;
    }

    @Override
    public boolean isIncludeStats() {
        return true;
    }
}
