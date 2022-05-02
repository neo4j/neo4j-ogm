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

import java.util.function.Predicate;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.autoindex.CompositeIndexChild;
import org.neo4j.ogm.domain.autoindex.CompositeIndexEntity;
import org.neo4j.ogm.domain.autoindex.MultipleCompositeIndexEntity;
import org.neo4j.ogm.session.SessionFactory;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 * @author Gerrit Meier
 */
public class CompositeIndexAutoIndexManagerTest extends BaseAutoIndexManagerTestClass {

    private static final String[] INDEXES = {
        "INDEX org_neo4j_ogm_domain_autoindex_compositeindexentity_name_age FOR (`entitywithcompositeindex`:`EntityWithCompositeIndex`) ON (`entitywithcompositeindex`.`name`,`entitywithcompositeindex`.`age`)",
        "INDEX org_neo4j_ogm_domain_autoindex_multiplecompositeindexentity_firstName_age FOR (`entitywithmultiplecompositeindexes`:`EntityWithMultipleCompositeIndexes`) ON (`entitywithmultiplecompositeindexes`.`firstName`,`entitywithmultiplecompositeindexes`.`age`)",
        "INDEX org_neo4j_ogm_domain_autoindex_multiplecompositeindexentity_firstName_email FOR (`entitywithmultiplecompositeindexes`:`EntityWithMultipleCompositeIndexes`) ON (`entitywithmultiplecompositeindexes`.`firstName`,`entitywithmultiplecompositeindexes`.`email`)"
    };
    private static final String CONSTRAINT = "CONSTRAINT EntityWithCompositeIndex_d FOR (entity:EntityWithCompositeIndex) REQUIRE (entity.name, entity.age) IS NODE KEY";

    public CompositeIndexAutoIndexManagerTest() {
        super(INDEXES, CompositeIndexEntity.class, CompositeIndexChild.class, MultipleCompositeIndexEntity.class);
    }

    @BeforeClass
    public static void setUpClass() {

        assumeTrue("This test uses composite index and node key constraint and can only be run on enterprise edition",
            useEnterpriseEdition());

        assumeTrue("This test uses db.indexes() which does not contain all required information prior to 3.3",
            isVersionOrGreater("3.3"));
    }

    @Override
    protected void additionalTearDown() {
        executeDrop(CONSTRAINT);
        executeDrop(INDEXES);
    }

    @Test
    public void testAutoIndexManagerUpdateConstraintChangedToIndex() {
        executeCreate(CONSTRAINT);

        runAutoIndex("update");

        executeForIndexes(indexes -> {
            assertThat(indexes.stream().filter(byLabel("EntityWithCompositeIndex"))).asList()
                .hasSize(1);
        });
        executeForConstraints(constraints -> assertThat(constraints).isEmpty());
    }

    @Test
    public void testMultipleCompositeIndexAnnotations() {
        try {
            runAutoIndex("update");
            executeForIndexes(indexes ->
                assertThat(indexes.stream().filter(byLabel("EntityWithMultipleCompositeIndexes"))).asList()
                    .hasSize(2)
            );
        } finally {
            executeDrop(INDEXES[1]);
            executeDrop(INDEXES[2]);
        }
    }

    @Test
    public void shouldSupportScanningNonEntityPackages() {
        new SessionFactory(getDriver(), CompositeIndexAutoIndexManagerTest.class.getName());
    }

    private static Predicate<IndexInfo> byLabel(String label) {
        return indexDefinition -> label.equals(indexDefinition.getLabel());
    }
}
