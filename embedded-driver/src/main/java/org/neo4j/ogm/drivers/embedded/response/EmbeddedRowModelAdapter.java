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

package org.neo4j.ogm.drivers.embedded.response;

import org.neo4j.ogm.drivers.embedded.driver.EmbeddedEntityAdapter;
import org.neo4j.ogm.result.adapter.RowModelAdapter;

/**
 * This adapter will transform an embedded response into a json response
 *
 * @author vince
 * @author Michael J. Simons
 */
public class EmbeddedRowModelAdapter extends RowModelAdapter {

    private final EmbeddedEntityAdapter entityAdapter;

    public EmbeddedRowModelAdapter(EmbeddedEntityAdapter entityAdapter) {
        this.entityAdapter = entityAdapter;
    }

    @Override
    public boolean isPath(Object value) {
        return entityAdapter.isPath(value);
    }

    @Override
    public boolean isNode(Object value) {
        return entityAdapter.isNode(value);
    }

    @Override
    public boolean isRelationship(Object value) {
        return entityAdapter.isRelationship(value);
    }
}
