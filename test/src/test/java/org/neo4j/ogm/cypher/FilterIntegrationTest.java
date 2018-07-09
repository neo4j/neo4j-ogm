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
