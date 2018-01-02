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

/**
 * @author Luanne Misquitta
 */
public class SortClause {

    private final SortOrder.Direction direction;
    private final String[] properties;

    public SortClause(SortOrder.Direction direction, String... properties) {
        this.direction = direction;
        this.properties = properties;
    }

    public String[] getProperties() {
        return properties;
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();

        if (properties.length > 0) {
            for (String n : properties) {
                sb.append("$.").append(n);
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
