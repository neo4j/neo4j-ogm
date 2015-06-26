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
package org.neo4j.ogm.cypher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vince Bickers
 */
public class Filters implements Iterable<Filter> {

    private List<Filter> filters = new ArrayList();

    public Filters add(Filter... filters) {
        for (Filter filter : filters) {
            this.filters.add(filter);
        }
        return this;
    }

    public Filters add(String key, Object value) {
        this.filters.add(new Filter(key, value));
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
