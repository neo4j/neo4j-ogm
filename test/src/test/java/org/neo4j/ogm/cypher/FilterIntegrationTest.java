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
package org.neo4j.ogm.cypher;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.music.Studio;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the generated filter fragments.
 *
 * @author Michael J. Simons
 */
public class FilterIntegrationTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.music");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
    }

    @After
    public void clear() {
        session.purgeDatabase();
    }

    @Test
    public void ignoreCaseShouldNotBeApplicableToComparisonOtherThanEquals() {
        final String emi = "EMI Studios, London";
        session.save(new Studio(emi));
        final Filter nameFilter = new Filter("name", ComparisonOperator.EQUALS, "eMi Studios, London").ignoreCase();
        assertThat(session.loadAll(Studio.class, nameFilter, 0))
            .hasSize(1)
            .extracting(Studio::getName)
            .containsExactly(emi);
    }
}
