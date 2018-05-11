/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */
package org.neo4j.ogm.cypher.query;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.ogm.cypher.query.SortOrder.Direction.ASC;
import static org.neo4j.ogm.cypher.query.SortOrder.Direction.DESC;

/**
 * @author Vince Bickers
 */
public class SortOrder {

    public enum Direction {
        ASC, DESC
    }

    private List<SortClause> sortClauses;

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


    /**
     * Adds a new {@link SortClause} containing properties ordered by {@link Direction#ASC}.
     *
     * @param properties list of properties ordered by {@link Direction#ASC}
     */
    public SortOrder add(String... properties) {
        return add(ASC, properties);
    }

    /**
     * Adds a new {@link SortClause} containing properties ordered by direction.
     *
     * @param direction  the specified {@link Direction} can be either ASC or DESC
     * @param properties list of properties ordered by direction
     */
    public SortOrder add(Direction direction, String... properties) {
        sortClauses.add(new SortClause(direction, properties));
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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!sortClauses.isEmpty()) {
            sb.append(" ORDER BY ");
            for (SortClause ordering : sortClauses) {
                sb.append(ordering);
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
}
