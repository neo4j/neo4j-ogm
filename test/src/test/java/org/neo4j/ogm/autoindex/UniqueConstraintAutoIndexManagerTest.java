/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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
import org.neo4j.ogm.domain.autoindex.UniqueConstraintEntity;

/**
 * @author Frantisek Hartman
 */
public class UniqueConstraintAutoIndexManagerTest extends BaseAutoIndexManagerTest {

    private static final String INDEX = "INDEX ON :`Entity`(`login`)";
    private static final String CONSTRAINT = "CONSTRAINT ON (`entity`:`Entity`) ASSERT `entity`.`login` IS UNIQUE";

    public UniqueConstraintAutoIndexManagerTest() {
        super(CONSTRAINT,
            UniqueConstraintEntity.class.getName());
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        // clean up index created in case this test in case it fails
        executeDrop(INDEX);
    }

    @Test
    public void testAutoIndexManagerUpdateIndexChangedToConstraint() throws Exception {
        executeCreate(INDEX);

        runAutoIndex("update");

        executeForIndexes(indexes -> {
            assertThat(indexes).isEmpty();
        });
        executeForConstraints(constraints -> {
            assertThat(constraints).hasSize(1);
        });
    }
}
