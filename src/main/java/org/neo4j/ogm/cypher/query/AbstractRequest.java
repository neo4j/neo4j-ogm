/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */
package org.neo4j.ogm.cypher.query;

import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.statement.CypherStatement;
import org.neo4j.ogm.driver.api.request.Statement;

import java.util.Map;

/**
 * @author Vince Bickers
 */
public abstract class AbstractRequest extends CypherStatement implements Statement, FilteringPagingAndSorting {

    protected AbstractRequest(String cypher, Map<String, ?> parameters) {
        super(cypher, parameters);
    }

    public AbstractRequest setPagination(Pagination page) {
        super.addPaging(page);
        return this;
    }

    public AbstractRequest setFilters(Filters filters) {
        super.addFilters(filters);
        return this;
    }

    public AbstractRequest setSortOrder(SortOrder sortOrder) {
        super.addSortOrder(sortOrder);
        return this;
    }

    public boolean isIncludeStats() {
        return false;
    }
}
