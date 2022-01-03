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
     *
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
