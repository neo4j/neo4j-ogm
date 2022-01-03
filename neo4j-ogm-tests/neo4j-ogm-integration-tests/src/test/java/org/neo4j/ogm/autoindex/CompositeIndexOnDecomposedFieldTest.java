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

import java.util.Collections;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.config.AutoIndexMode;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Michael J. Simons
 */
public class CompositeIndexOnDecomposedFieldTest extends TestContainersTestBase {

    @BeforeClass
    public static void setUpClass() {

        assumeTrue("This test uses composite index and node key constraint and can only be run on enterprise edition",
            useEnterpriseEdition());

        assumeTrue("This test uses db.indexes() which does not contain all required information prior to 3.3",
            isVersionOrGreater("3.3"));
    }

    @Test // GH-789
    public void autoIndexManagerShouldWorkWithDecomposedField() {

        SessionFactory sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.gh789.Entity");
        Configuration configuration = new Configuration.Builder().autoIndex(AutoIndexMode.ASSERT.getName()).build();
        sessionFactory.runAutoIndexManager(configuration);

        assertThat(countIndexes(sessionFactory.openSession(), "Entity")).isEqualTo(2L);
    }

    @Test // GH-789
    public void shouldStillGenerateIndexOnNonCompositeFields() {

        SessionFactory sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.gh789.Entity2");
        Configuration configuration = new Configuration.Builder().autoIndex(AutoIndexMode.ASSERT.getName()).build();
        sessionFactory.runAutoIndexManager(configuration);

        assertThat(countIndexes(sessionFactory.openSession(), "Entity2")).isEqualTo(4L);
    }

    @Test // GH-789
    public void shouldWarnWithUnsupportedIndexes() {

        SessionFactory sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.gh789.Entity3");
        Configuration configuration = new Configuration.Builder().autoIndex(AutoIndexMode.ASSERT.getName()).build();
        sessionFactory.runAutoIndexManager(configuration);

        assertThat(countIndexes(sessionFactory.openSession(), "Entity3")).isEqualTo(0L);
    }

    /**
     * It appears that the index tests are somewhat interdependent, especially on embedded. Indexes created through
     * this tests make a couple of other tests, for example the {@link NodeKeyConstraintIndexAutoIndexManagerTest} fail.
     * The easiest solution to get rid of the indexes created here and <strong>not</strong> involving inheriting
     * {@link BaseAutoIndexManagerTestClass} is to scan a non existing package so that no entities are found and than
     * run the index manager in assert mode.
     */
    @AfterClass
    public static void tearDown() {

        SessionFactory sessionFactory = new SessionFactory(getDriver(),
            "org.neo4j.ogm.domain.gh789.non_existing_package");
        Configuration configuration = new Configuration.Builder().autoIndex(AutoIndexMode.ASSERT.getName()).build();
        sessionFactory.runAutoIndexManager(configuration);
    }

    private static long countIndexes(Session session, String primaryLabel) {

        String query;
        if (isVersionOrGreater("3.5")) {
            String labelsOrTypes = isVersionOrGreater("4.0") ? "labelsOrTypes" : "tokenNames";
            query = "CALL db.indexes() YIELD " + labelsOrTypes + " AS labelsOrTypes, properties \n"
                + "WHERE labelsOrTypes = [$label]\n"
                + "UNWIND properties AS p\n"
                + "WITH p\n"
                + "RETURN COUNT(p) as cnt";

        } else {
            query = "CALL db.indexes() YIELD label, properties \n"
                + "WHERE label = $label\n"
                + "UNWIND properties AS p\n"
                + "WITH p\n"
                + "RETURN COUNT(p) as cnt";
        }

        return session.queryForObject(Long.class, query, Collections.singletonMap("label", primaryLabel));
    }
}
