/*
 * Copyright (c) 2002-2019 "Neo4j,"
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

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.ogm.domain.autoindex.CompositeIndexChild;
import org.neo4j.ogm.domain.autoindex.CompositeIndexEntity;
import org.neo4j.ogm.domain.autoindex.MultipleCompositeIndexEntity;
import org.neo4j.ogm.session.SessionFactory;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class CompositeIndexAutoIndexManagerTest extends BaseAutoIndexManagerTestClass {

    private static final String[] INDEXES = {
        "INDEX ON :`EntityWithCompositeIndex`(`name`,`age`)",
        "INDEX ON :`EntityWithMultipleCompositeIndexes`(`firstName`,`age`)",
        "INDEX ON :`EntityWithMultipleCompositeIndexes`(`firstName`,`email`)"
    };
    private static final String CONSTRAINT = "CONSTRAINT ON (entity:EntityWithCompositeIndex) ASSERT (entity.name, entity.age) IS NODE KEY";

    public CompositeIndexAutoIndexManagerTest() {
        super(INDEXES, CompositeIndexEntity.class, CompositeIndexChild.class, MultipleCompositeIndexEntity.class);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        assumeTrue("This test uses composite index and node key constraint and can only be run on enterprise edition",
            isEnterpriseEdition());

        assumeTrue("This tests uses composite index and can only be run on Neo4j 3.2.0 and later",
            isVersionOrGreater("3.2.0"));
    }

    @Override
    @After
    public void tearDown() throws Exception {

        super.tearDown();
        executeDrop(CONSTRAINT);
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
            executeDrop("INDEX ON :EntityWithMultipleCompositeIndexes(firstName, age)");
            executeDrop("INDEX ON :EntityWithMultipleCompositeIndexes(firstName, email)");
        }
    }

    @Test
    public void shouldSupportScanningNonEntityPackages() {
        new SessionFactory(CompositeIndexAutoIndexManagerTest.class.getName());
    }

    private static Predicate<IndexDefinition> byLabel(String label) {
        return indexDefinition -> label.equals(indexDefinition.getLabel().name());
    }
}
