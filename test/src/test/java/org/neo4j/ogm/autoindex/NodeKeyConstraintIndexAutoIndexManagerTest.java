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
import static org.junit.Assume.*;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.autoindex.NodeKeyConstraintEntity;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class NodeKeyConstraintIndexAutoIndexManagerTest extends BaseAutoIndexManagerTestClass {

    private static final String CONSTRAINT =
        "CONSTRAINT ON (`entity`:`Entity`) ASSERT (`entity`.`name`,`entity`.`age`) IS NODE KEY";

    private static final String INDEX = "INDEX ON :`Entity`(`name`,`age`)";

    public NodeKeyConstraintIndexAutoIndexManagerTest() {
        super(new String[] { CONSTRAINT }, NodeKeyConstraintEntity.class);
    }

    @BeforeClass
    public static void setUpClass() {
        assumeTrue("This test uses composite index and node key constraint and can only be run on enterprise edition",
            isEnterpriseEdition());

        assumeTrue("This tests uses composite index and can only be run on Neo4j 3.2.0 and later",
            isVersionOrGreater("3.2.0"));
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        executeDrop(INDEX);
    }

    @Test
    public void testAutoIndexManagerUpdateConstraintChangedToIndex() throws Exception {
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
