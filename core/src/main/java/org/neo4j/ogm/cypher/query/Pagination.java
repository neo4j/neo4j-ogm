/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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
 * @author Vince Bickers
 */
public class Pagination {

    private final Integer index;
    private final Integer size;
    private Integer offset;

    public Pagination(int pageNumber, int pageSize) {

        if (pageNumber < 0) {
            throw new RuntimeException("Page number must not be negative");
        }

        if (pageSize < 1) {
            throw new RuntimeException("Page size must greater then zero");
        }

        this.index = pageNumber;
        this.size = pageSize;
    }

	/**
     * The offset, if present, determines how many records to skip.
     * Otherwise, pageNumber * pageSize records are skipped.
     * @param offset the offset
     */
    public void setOffset(Integer offset) {
        if (offset < 0) {
            throw new RuntimeException("Offset must greater then zero");
        }
        this.offset = offset;
    }

    public String toString() {
        if (offset != null) {
            return " SKIP " + offset + " LIMIT " + size;
        }
        return " SKIP " + (index * size) + " LIMIT " + size;
    }
}
