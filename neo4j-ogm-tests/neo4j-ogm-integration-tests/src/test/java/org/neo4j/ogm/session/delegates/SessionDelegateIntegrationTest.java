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
import org.neo4j.ogm.testutil.TestContainersTestBase;
import org.neo4j.ogm.typeconversion.DateLongConverter;
import org.neo4j.ogm.typeconversion.DateStringConverter;

/**
 * Integration tests for the session delegate.
 *
 * @author Michael J. Simons
 */
public class SessionDelegateIntegrationTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void createSessionFactory() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.music");
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
