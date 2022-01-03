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
package org.neo4j.ogm.cypher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.ogm.cypher.function.FilterFunction;

/**
 * @author Vince Bickers
 * @author Jasper Blues
 */
public class Filters implements Iterable<Filter> {

    private List<Filter> filters = new ArrayList<>();

    public Filters() {
    }

    public Filters(Filter... filters) {
        add(filters);
    }

    public Filters(Iterable<Filter> filters) {
        add(filters);
    }

    public Filters add(Filter... additionalFilters) {
        for (Filter filter : additionalFilters) {
            this.add(filter);
        }
        return this;
    }

    public Filters add(Iterable<Filter> additionalFilters) {
        additionalFilters.forEach(this::add);
        return this;
    }

    public Filters add(FilterFunction function) {
        this.add(new Filter(function));
        return this;
    }

    public Filters add(Filter filter) {
        filter.setIndex(this.filters.size());
        this.filters.add(filter);
        return this;
    }

    /**
     * Convenience method to add a filter using {@link BooleanOperator#AND}.
     *
     * @param filter to be chained.
     * @return current {@link Filters} object with chained {@link Filter}.
     */
    public Filters and(Filter filter) {
        filter.setBooleanOperator(BooleanOperator.AND);
        return add(filter);
    }

    /**
     * Convenience method to add a filter using {@link BooleanOperator#OR}.
     *
     * @param filter to be chained.
     * @return current {@link Filters} object with chained {@link Filter}.
     */
    public Filters or(Filter filter) {
        filter.setBooleanOperator(BooleanOperator.OR);
        return add(filter);
    }

    @Override
    public Iterator<Filter> iterator() {
        return filters.iterator();
    }

    public boolean isEmpty() {
        return filters.isEmpty();
    }
}
