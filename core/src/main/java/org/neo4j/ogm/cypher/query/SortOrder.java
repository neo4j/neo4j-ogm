/*
 * Copyright (c) 2002-2025 "Neo4j,"
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

import static org.neo4j.ogm.cypher.query.SortOrder.Direction.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vince Bickers
 * @author Jonathan D'Orleans
 * @author Christopher Quadflieg
 */
public class SortOrder {

    final List<SortClause> sortClauses;

    /**
     * Creates SortOrder with a empty list of {@link SortClause}.
     */
    public SortOrder() {
        sortClauses = new ArrayList<>();
    }

    /**
     * Creates SortOrder with a new {@link SortClause} containing properties ordered by {@link Direction#ASC}.
     *
     * @param properties list of properties ordered by {@link Direction#ASC}
     */
    public SortOrder(String... properties) {
        this(ASC, properties);
    }

    /**
     * Creates SortOrder with a new {@link SortClause} containing properties ordered by direction.
     *
     * @param direction  the specified {@link Direction} can be either ASC or DESC
     * @param properties list of properties ordered by direction
     */
    public SortOrder(Direction direction, String... properties) {
        this();
        add(direction, properties);
    }

    public static SortOrder fromSortClauses(List<SortClause> sortClauses) {
        SortOrder sortOrder = new SortOrder();
        sortOrder.sortClauses.addAll(sortClauses);
        return sortOrder;
    }

    /**
     * Adds a new {@link SortClause} containing a property ordered by {@link Direction#ASC}.
     *
     * @param property   the property ordered by {@link Direction#ASC}
     * @param ignoreCase whether to ignore case when sorting
     */
    public SortOrder add(String property, boolean ignoreCase) {
        return add(ASC, ignoreCase, property);
    }

    /**
     * Adds a new {@link SortClause} containing properties ordered by {@link Direction#ASC}.
     *
     * @param properties list of properties ordered by {@link Direction#ASC}
     */
    public SortOrder add(String... properties) {
        return add(ASC, properties);
    }

    /**
     * Adds a new {@link SortClause} containing properties ordered by {@link Direction#ASC},
     * ignoring the case of the value to sort by.
     *
     * @param ignoreCase flag to indicate if the sorting should be done by ignoring the case.
     * @param properties list of properties ordered by {@link Direction#ASC}
     */
    public SortOrder add(boolean ignoreCase, String... properties) {
        return add(ASC, ignoreCase, properties);
    }

    /**
     * Adds a new {@link SortClause} containing properties ordered by direction.
     *
     * @param direction  the specified {@link Direction} can be either ASC or DESC
     * @param properties list of properties ordered by direction
     */
    public SortOrder add(Direction direction, String... properties) {
        add(direction, false, properties);
        return this;
    }

    /**
     * Adds a new {@link SortClause} containing a property ordered by direction.
     *
     * @param direction  the specified {@link Direction} can be either ASC or DESC
     * @param ignoreCase whether to ignore case when sorting
     * @param properties   the property ordered by direction
     */
    public SortOrder add(Direction direction, boolean ignoreCase, String... properties) {
        sortClauses.add(new SortClause(direction, ignoreCase, properties));
        return this;
    }

    /**
     * Adds a new {@link SortClause} containing properties ordered by {@link Direction#ASC}.
     *
     * @param properties list of properties ordered by {@link Direction#ASC}
     */
    public SortOrder asc(String... properties) {
        return add(ASC, properties);
    }

    /**
     * Adds a new {@link SortClause} containing properties ordered by {@link Direction#DESC}.
     *
     * @param properties list of properties ordered by {@link Direction#DESC}
     */
    public SortOrder desc(String... properties) {
        return add(DESC, properties);
    }

    /**
     * Return the list of existing {@link SortClause}.
     */
    public List<SortClause> sortClauses() {
        return sortClauses;
    }

    public boolean hasSortClauses() {
        return !sortClauses.isEmpty();
    }

    public String asString() {
        StringBuilder sb = new StringBuilder();
        if (!sortClauses.isEmpty()) {
            sb.append(" ORDER BY ");
            for (SortClause ordering : sortClauses) {
                sb.append(ordering.asString());
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public enum Direction {
        ASC, DESC
    }
}
