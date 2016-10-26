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

    public Filters(String key, Object value) {
        add(key, value);
    }

    public Filters(Filter... filters) {
        add(filters);
    }

    public Filters(Iterable<Filter> filters) {
        add(filters);
    }


    public Filters add(String key, Object value) {
        this.add(new Filter(key, value));
        return this;
    }

    public Filters add(Filter... filters) {
        for (Filter filter : filters) {
            this.add(filter);
        }
        return this;
    }

    public Filters add(Iterable<Filter> filters) {
        for (Filter filter : filters) {
            this.add(filter);
        }
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

    @Override
    public Iterator<Filter> iterator() {
        return filters.iterator();
    }

    public boolean isEmpty() {
        return filters.isEmpty();
    }
}
