/*
 * Copyright (c) 2002-2026 "Neo4j,"
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

/**
 * @author Luanne Misquitta
 * @author Christopher Quadflieg
 */
public class SortClause {

    private final SortOrder.Direction direction;
    private final String[] properties;
    private final boolean ignoreCase;

    SortClause(SortOrder.Direction direction, boolean ignoreCase, String... properties) {
        this.direction = direction;
        this.ignoreCase = ignoreCase;
        this.properties = properties;
    }

    public String[] getProperties() {
        return properties;
    }

    public SortClause fromResolvedProperties(String... resolvedProperties) {
        if (resolvedProperties.length != properties.length) {
            throw new IllegalArgumentException("Resolved properties count must match existing properties count.");
        }

        return new SortClause(this.direction, this.ignoreCase, resolvedProperties);

    }

    String asString() {
        StringBuilder sb = new StringBuilder();

        if (properties.length > 0) {
            for (String n : properties) {
                if (ignoreCase) {
                    sb.append("toLower(");
                }
                sb.append("$.").append(n);
                if (ignoreCase) {
                    sb.append(")");
                }
                if (direction == SortOrder.Direction.DESC) {
                    sb.append(" DESC");
                }
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
}
