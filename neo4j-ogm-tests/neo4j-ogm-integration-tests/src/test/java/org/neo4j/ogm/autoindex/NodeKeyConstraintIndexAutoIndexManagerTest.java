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
 * @author Gerrit Meier
 */
public class NodeKeyConstraintIndexAutoIndexManagerTest extends BaseAutoIndexManagerTestClass {

    private static final String CONSTRAINT =
        "CONSTRAINT org_neo4j_ogm_domain_autoindex_nodekeyconstraintentity_name_age_node_key FOR (`entity`:`Entity`) REQUIRE (`entity`.`name`,`entity`.`age`) IS NODE KEY";

    private static final String INDEX = "INDEX org_neo4j_ogm_domain_autoindex_nodekeyconstraintentity_name_age FOR (`entity`:`Entity`) ON (`entity`.`name`,`entity`.`age`)";

    public NodeKeyConstraintIndexAutoIndexManagerTest() {
        super(new String[] { CONSTRAINT }, NodeKeyConstraintEntity.class);
    }

    @BeforeClass
    public static void setUpClass() {
        assumeTrue("This test uses composite index and node key constraint and can only be run on enterprise edition",
            useEnterpriseEdition());

        assumeTrue("This tests uses composite index and can only be run on Neo4j 3.2.0 and later",
            isVersionOrGreater("3.2.0"));
    }

    @Override
    protected void additionalTearDown() {
        executeDrop(INDEX);
        executeDrop(CONSTRAINT);
    }

    @Test
    public void testAutoIndexManagerUpdateConstraintChangedToIndex() {
        executeCreate(INDEX);

        runAutoIndex("update");

        executeForIndexes(indexes -> assertThat(indexes).hasSize(expectedNumberOfAdditionalIndexes));
        executeForConstraints(constraints -> assertThat(constraints).hasSize(1));
    }
}
