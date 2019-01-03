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

package org.neo4j.ogm.autoindex;

import static org.assertj.core.api.Assertions.*;

import org.junit.After;
import org.junit.Test;
import org.neo4j.ogm.domain.autoindex.SingleIndexEntity;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class SingleIndexAutoIndexManagerTest extends BaseAutoIndexManagerTestClass {

    private static final String INDEX = "INDEX ON :`Entity`(`login`)";
    private static final String CONSTRAINT = "CONSTRAINT ON (entity:Entity) ASSERT entity.login IS UNIQUE";

    public SingleIndexAutoIndexManagerTest() {
        super(new String[] { INDEX }, SingleIndexEntity.class);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        // clean up in case this test fails
        executeDrop(CONSTRAINT);
    }

    @Test
    public void testAutoIndexManagerUpdateConstraintChangedToIndex() {
        executeCreate(CONSTRAINT);

        runAutoIndex("update");

        executeForIndexes(indexes -> {
            assertThat(indexes).hasSize(1);
        });
        executeForConstraints(constraints -> {
            assertThat(constraints).isEmpty();
        });
    }
}
