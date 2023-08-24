/*
 * Copyright (c) 2002-2023 "Neo4j,"
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
package org.neo4j.ogm.persistence.session.capability;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;

import org.assertj.core.api.BDDAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.ogm.domain.gh952.BookWasReadBy;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.LoggerRule;
import org.neo4j.ogm.testutil.TestContainersTestBase;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

/**
 * This test class is specifically for GH-952 and needs to be executed separately within a clean JVM and new Neo4j
 * instance so that native IDs are generated as expected.
 *
 * @author Niels Oertel
 */
public class QueryCapabilityGH952Test extends TestContainersTestBase {

    private SessionFactory sessionFactory;
    private Session session;

    static {
        LoggerContext logCtx = (LoggerContext) LoggerFactory.getILoggerFactory();
        logCtx.getLogger(Neo4jSession.class).setLevel(Level.DEBUG);
    }

    @Rule
    public final LoggerRule loggerRule = new LoggerRule();

    @Before
    public void init() throws IOException {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.gh952");
        session = sessionFactory.openSession();
        session.purgeDatabase();
        session.clear();
    }

    @Test // GH-952
    public void shouldCorrectlyMapNodesAndRelsWithSameId() {
        // create test graph
        session.query(""//
                + "MERGE (h1:Human{name:'Jane Doe', uuid:'AAAA0001'}) "//
                + "MERGE (h2:Human{name:'Jon Doe Jr.', uuid:'AAAA0002'}) "//
                + "MERGE (b:Book{title:'Moby-Dick', uuid:'AAAA0003'}) "//
                + "MERGE (h1)-[:PARENT_OF{uuid:'BBBB0001', lastMeeting:1689347516000}]->(h2) "//
                + "MERGE (b)-[:READ_BY{uuid:'BBBB0002', date:1689693116000}]->(h1)", //
                Collections.emptyMap());

        // verify that we reached the expected setup
        BDDAssertions.assertThat(session.queryForObject(Long.class, "MATCH (n{uuid:'AAAA0001'}) RETURN id(n)", Collections.emptyMap())).isNotNull();
        BDDAssertions.assertThat(session.queryForObject(Long.class, "MATCH (n{uuid:'AAAA0002'}) RETURN id(n)", Collections.emptyMap())).isNotNull();
        BDDAssertions.assertThat(session.queryForObject(Long.class, "MATCH (n{uuid:'AAAA0003'}) RETURN id(n)", Collections.emptyMap())).isNotNull();
        BDDAssertions.assertThat(session.queryForObject(Long.class, "MATCH ()-[r{uuid:'BBBB0001'}]->() RETURN id(r)", Collections.emptyMap()))
                .isNotNull();
        BDDAssertions.assertThat(session.queryForObject(Long.class, "MATCH ()-[r{uuid:'BBBB0002'}]->() RETURN id(r)", Collections.emptyMap()))
                .isNotNull();

        // load the relationship entity between Moby-Dick and Jane Doe including the children of Jane
        BDDAssertions.assertThat(//
                session.query(BookWasReadBy.class, ""//
                        + "MATCH (b:Book{title:$bookTitle})-[r:READ_BY]->(h:Human) "//
                        + "RETURN"//
                        + "  r, b, h, [[ (h)-[p:PARENT_OF]->(c) | [p, c]]], id(r)", //
                        Collections.singletonMap("bookTitle", "Moby-Dick")))//
                .hasSize(1)// -> this check fails without the fix for GH-952
                .allSatisfy(bookReadBy -> {
                    BDDAssertions.assertThat(bookReadBy.getBook().getTitle()).isEqualTo("Moby-Dick");
                    BDDAssertions.assertThat(bookReadBy.getDate()).isEqualTo(Instant.ofEpochMilli(1689693116000L));
                    BDDAssertions.assertThat(bookReadBy.getHuman().getName()).isEqualTo("Jane Doe");
                    BDDAssertions.assertThat(bookReadBy.getHuman().getChildren())//
                            .hasSize(1)//
                            .allSatisfy(parentOf -> {
                                BDDAssertions.assertThat(parentOf.getParent().getName()).isEqualTo("Jane Doe");
                                BDDAssertions.assertThat(parentOf.getLastMeeting()).isEqualTo(Instant.ofEpochMilli(1689347516000L));
                                BDDAssertions.assertThat(parentOf.getChild().getName()).isEqualTo("Jon Doe Jr.");
                            });
                });
    }

}
