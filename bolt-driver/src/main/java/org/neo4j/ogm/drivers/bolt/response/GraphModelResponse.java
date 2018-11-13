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

package org.neo4j.ogm.drivers.bolt.response;

import org.neo4j.driver.v1.StatementResult;
import org.neo4j.ogm.drivers.bolt.driver.BoltEntityAdapter;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.transaction.TransactionManager;

/**
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class GraphModelResponse extends BoltResponse<GraphModel> {

    private final BoltGraphModelAdapter adapter;

    public GraphModelResponse(StatementResult result, TransactionManager transactionManager, BoltEntityAdapter boltEntityAdapter) {

        super(result, transactionManager);

        this.adapter = new BoltGraphModelAdapter(boltEntityAdapter);
    }

    @Override
    public GraphModel fetchNext() {
        if (result.hasNext()) {
            return adapter.adapt(result.next().asMap());
        }
        return null;
    }
}
