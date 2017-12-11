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

package org.neo4j.ogm.response;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vince Bickers
 */
public interface Response<T> extends AutoCloseable {

    T next();

    /**
     * Convert remaining items in this response to list
     *
     * This might be used to materialize whole response for checking number of results, allowing to close transaction
     * etc.
     *
     * Doesn't call {@link #close()}.
     */
    default List<T> toList() {
        ArrayList<T> models = new ArrayList<>();
        T model;
        while ((model = next()) != null) {
            models.add(model);
        }
        return models;
    }

    void close();

    String[] columns();
}
