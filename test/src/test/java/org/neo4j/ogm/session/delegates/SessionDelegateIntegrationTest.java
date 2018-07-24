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
package org.neo4j.ogm.session.delegates;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.typeconversion.DateLongConverter;
import org.neo4j.ogm.typeconversion.DateStringConverter;

/**
 * Integration tests for the session delegate.
 *
 * @author Michael J. Simons
 */
public class SessionDelegateIntegrationTest extends MultiDriverTestClass {

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

    @Test // DATAGRAPH-933
    public void shouldPickupCorrectFieldInfo() {

        final Date filterValue = new Date();

        final Filter recordedAtFilter = new Filter("recordedAt", ComparisonOperator.GREATER_THAN, filterValue);
        final Filter releasedFilter = new Filter("released", ComparisonOperator.GREATER_THAN, filterValue);
        final Filter releasedAtFilter = new Filter("releasedAt", ComparisonOperator.GREATER_THAN, filterValue);
        final Filter enteredChartAtFilter = new Filter("enteredChartAt", ComparisonOperator.GREATER_THAN, filterValue);
        final Filter leftChartFilter = new Filter("leftChart", ComparisonOperator.GREATER_THAN, filterValue);
        final Filter leftChartAtFilter = new Filter("leftChartAt", ComparisonOperator.GREATER_THAN, filterValue);

        final SessionDelegate sessionDelegate = new LoadByTypeDelegate((Neo4jSession) session);
        sessionDelegate.resolvePropertyAnnotations(Album.class, Arrays.asList(
            recordedAtFilter,
            releasedFilter,
            releasedAtFilter,
            enteredChartAtFilter,
            leftChartFilter,
            leftChartAtFilter
        ));

        assertThat(recordedAtFilter.getPropertyConverter())
            .as("Property converter %s should be used for Date fields without @Property-annotation",
                DateStringConverter.class)
            .isInstanceOf(DateStringConverter.class);
        assertThat(releasedFilter.getPropertyConverter())
            .as("Property converter %s should be used for Date fields with @Property-annotation referred by field name",
                DateStringConverter.class)
            .isInstanceOf(DateStringConverter.class);
        assertThat(releasedAtFilter.getPropertyConverter())
            .as("Property converter %s should be used for Date fields with @Property-annotation referred by property name",
                DateStringConverter.class)
            .isInstanceOf(DateStringConverter.class);

        assertThat(enteredChartAtFilter.getPropertyConverter())
            .as("Specified provider should be used")
            .isInstanceOf(DateLongConverter.class);
        assertThat(leftChartFilter.getPropertyConverter())
            .as("Specified provider should be used")
            .isInstanceOf(DateLongConverter.class);
        assertThat(leftChartAtFilter.getPropertyConverter())
            .as("Specified provider should be used")
            .isInstanceOf(DateLongConverter.class);
    }
}
