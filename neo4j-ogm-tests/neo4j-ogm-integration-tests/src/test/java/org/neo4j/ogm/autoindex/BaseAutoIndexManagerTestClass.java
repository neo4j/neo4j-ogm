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

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.driver.exceptions.DatabaseException;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Must not end with "Test" so it does not run on TC.
 *
 * @author Frantisek Hartman
 * @author Michael J. Simons
 * @author Gerrit Meier
 */
@SuppressWarnings("HiddenField")
public abstract class BaseAutoIndexManagerTestClass extends TestContainersTestBase {

    private static final Logger logger = LoggerFactory.getLogger(BaseAutoIndexManagerTestClass.class);

    private static final String[] COMMUNITY_INDEXES = {
        "INDEX ON :User(email)",
    };

    private static final String[] COMMUNITY_CONSTRAINTS = {
        "CONSTRAINT ON (user:User) ASSERT user.login IS UNIQUE",
    };

    private static final String[] ENTERPRISE_INDEXES = {
        "INDEX ON :User(email)",
        "INDEX ON :User(lat, lon)"
    };

    private static final String[] ENTERPRISE_CONSTRAINTS = {
        "CONSTRAINT ON (user:User) ASSERT user.login IS UNIQUE",
        "CONSTRAINT ON (user:User) ASSERT (user.key, user.key2) IS NODE KEY",
        "CONSTRAINT ON (user:User) ASSERT exists(user.address)",
        "CONSTRAINT ON ()-[rating:RATING]-() ASSERT exists(rating.stars)",
    };

    private String[] indexes;
    private String[] constraints;
    private String[] statements;
    private String[] expectedIndexDefinitions;

    protected SessionFactory sessionFactory;

    BaseAutoIndexManagerTestClass(String[] expectedIndexDefinitions, Class<?>... packages) {
        sessionFactory = new SessionFactory(getDriver(), Arrays.stream(packages).map(Class::getName).toArray(String[]::new));
        this.expectedIndexDefinitions = expectedIndexDefinitions;
    }

    /**
     * Avoid logging of database exceptions on bolt protocol level
     */
    @BeforeClass
    public static void disableBoltWarningLogs() {
        LoggerContext logCtx = (LoggerContext) LoggerFactory.getILoggerFactory();
        logCtx.getLogger("ChannelErrorHandler").setLevel(Level.ERROR);
        logCtx.getLogger("InboundMessageHandler").setLevel(Level.ERROR);
    }

    @Before
    public void setUp() {
        Session session = sessionFactory.openSession();
        session.query("MATCH (n) DETACH DELETE n", emptyMap());
        String[] existingConstraints = StreamSupport.stream(session.query("CALL db.constraints()", emptyMap())
            .queryResults().spliterator(), false)
            .map(r -> r.get("description"))
            .map(String.class::cast)
            .toArray(String[]::new);

        executeDrop(existingConstraints);

        if (useEnterpriseEdition() && isVersionOrGreater("3.2.0")) {
            indexes = ENTERPRISE_INDEXES;
            constraints = ENTERPRISE_CONSTRAINTS;
            statements = Stream.of(ENTERPRISE_INDEXES, ENTERPRISE_CONSTRAINTS).flatMap(Stream::of)
                .toArray(String[]::new);
        } else {
            indexes = COMMUNITY_INDEXES;
            constraints = COMMUNITY_CONSTRAINTS;
            statements = Stream.of(COMMUNITY_INDEXES, COMMUNITY_CONSTRAINTS).flatMap(Stream::of).toArray(String[]::new);
        }
    }

    @After
    public final void tearDown() {
        silentTearDown(() -> {
            executeDrop(expectedIndexDefinitions);
            executeDrop(statements);
        });

        silentTearDown(this::additionalTearDown);
    }

    private void silentTearDown(Runnable r) {
        try {
            r.run();
        } catch (DatabaseException e) {
            // just be silent because we might drop a lot of not existing indexes here
            String code = e.code();
            List<String> silencedFailures = Arrays
                .asList("Neo.DatabaseError.Schema.ConstraintDropFailed", "Neo.DatabaseError.Schema.IndexDropFailed");
            if (!silencedFailures.contains(code)) {
                throw e;
            }
        }
    }

    /**
     * Hook for removing further constraints etc. Will be executed in a try / catch block, exceptions will be swallowed.
     */
    protected abstract void additionalTearDown();

    @Test
    public void testAutoIndexNoneNoIndexIsCreated() {
        runAutoIndex("none");

        executeForIndexes(indexes -> {
            assertThat(indexes).isEmpty();
        });
        executeForConstraints(constraints -> {
            assertThat(constraints).isEmpty();
        });
    }

    @Test
    public void testAutoIndexNoneNoIndexIsDropped() {
        executeCreate(statements);

        runAutoIndex("none");

        executeForIndexes(indexes -> {
            assertThat(indexes).hasSize(this.indexes.length);
        });
        executeForConstraints(constraints -> {
            assertThat(constraints).hasSize(this.constraints.length);
        });
    }

    @Test
    public void testIndexesAreSuccessfullyValidated() {
        executeCreate(expectedIndexDefinitions);

        runAutoIndex("validate");
    }

    @Test
    public void testIndexValidationFailsOnMissingIndex() {

        final AbstractThrowableAssert<?, ? extends Throwable> assertThatException = assertThatThrownBy(
            () -> runAutoIndex("validate"))
            .isInstanceOf(MissingIndexException.class)
            .hasMessageContaining("Validation of Constraints and Indexes failed. Could not find the following :");
        for (String definition : this.expectedIndexDefinitions) {
            assertThatException.hasMessageContaining(definition);
        }
    }

    @Test
    public void testAutoIndexAssertDropsAllIndexesAndCreatesExisting() {
        executeCreate(statements);

        runAutoIndex("assert");

        List<Object> all = new ArrayList<>();
        executeForIndexes(all::addAll);
        executeForConstraints(all::addAll);

        assertThat(all).hasSize(this.expectedIndexDefinitions.length);
    }

    @Test
    public void testAutoIndexUpdateKeepIndexesAndCreateNew() {

        executeCreate(statements);

        runAutoIndex("update");

        List<Object> all = new ArrayList<>();
        executeForIndexes(all::addAll);
        executeForConstraints(all::addAll);

        int expectedNumberOfIndexes = this.indexes.length + this.constraints.length + this.expectedIndexDefinitions.length;
        assertThat(all).hasSize(expectedNumberOfIndexes);
    }

    @Test
    public void testAutoIndexUpdateIndexExistsDoNothing() {

        executeCreate(expectedIndexDefinitions);

        runAutoIndex("update");

        List<Object> all = new ArrayList<>();
        executeForIndexes(all::addAll);
        executeForConstraints(all::addAll);

        assertThat(all).hasSize(this.expectedIndexDefinitions.length);
    }

    @Test
    public void testAutoIndexDumpCreatesIndex() throws IOException {

        File file = File.createTempFile("test", ".cql");
        file.deleteOnExit();

        Configuration configuration = getBaseConfigurationBuilder()
            .autoIndex("dump")
            .generatedIndexesOutputDir(file.getParent())
            .generatedIndexesOutputFilename(file.getName())
            .build();

        sessionFactory.runAutoIndexManager(configuration);

        assertThat(file).exists().canRead();
        assertThat(contentOf(file))
            .contains(Arrays.stream(expectedIndexDefinitions).map(d -> "CREATE " + d).toArray(String[]::new));
    }

    void runAutoIndex(String mode) {
        Configuration configuration = getBaseConfigurationBuilder().autoIndex(mode).build();
        sessionFactory.runAutoIndexManager(configuration);
    }

    void executeForIndexes(Consumer<List<IndexInfo>> consumer) {
        Session session = sessionFactory.openSession();
        try (Transaction tx = session.beginTransaction()) {
            Result indexResult = session.query("CALL db.indexes()", emptyMap());
            List<IndexInfo> indexes = new ArrayList<>();

            for (Map<String, Object> queryResult : indexResult.queryResults()) {
                if (isVersionOrGreater("4.0.0")) {
                    IndexInfo indexInfo = new IndexInfo((String) queryResult.get("uniqueness"),
                        (String[]) queryResult.get("labelsOrTypes"));

                    indexes.add(indexInfo);
                } else {
                    String indexType = (String) queryResult.get("type");
                    String[] labels = queryResult.get("tokenNames") != null
                        ? (String[]) queryResult.get("tokenNames") // 3.5
                        : new String[] { (String) queryResult.get("label") }; // 3.4(-)

                    IndexInfo indexInfo = new IndexInfo(indexType, labels);

                    indexes.add(indexInfo);
                }
            }
            List<IndexInfo> pureIndexes = indexes.stream()
                .filter(indexInfo -> !indexInfo.isConstraintIndex())
                .collect(toList());
            consumer.accept(pureIndexes);
            tx.commit();
        }
    }

    void executeForConstraints(Consumer<List<ConstraintInfo>> consumer) {
        Session session = sessionFactory.openSession();
        try (Transaction tx = session.beginTransaction()) {
            Result constraintResult = session.query("CALL db.constraints()", emptyMap());
            List<ConstraintInfo> constraints = new ArrayList<>();

            for (Map<String, Object> stringObjectMap : constraintResult) {
                constraints.add(new ConstraintInfo());
            }

            consumer.accept(constraints);
            tx.commit();
        }
    }

    void executeCreate(String... statements) {
        Session session = sessionFactory.openSession();
        try (Transaction transaction = session.beginTransaction()) {
            for (String statement : statements) {
                logger.info("Execute CREATE " + statement);
                session.query("CREATE " + statement, emptyMap());
            }
            transaction.commit();
        }
    }

    void executeDrop(String... statements) {
        Session session = sessionFactory.openSession();
            for (String statement : statements) {
        try (Transaction transaction = session.beginTransaction()) {
                // need to handle transaction manually because when the service.execute fails with exception
                // it does not clean up the tx resources, leading to deadlock later
                try {
                    session.query("DROP " + statement, emptyMap());
                    transaction.commit();
                } catch (Exception e) {
                    logger.trace("Could not execute drop for statement (this is likely expected) {}", statement, e);
                    transaction.rollback();
                }
            }
        }
    }

    static class IndexInfo {
        List<String> labels;
        IndexInfoType type;

        IndexInfo(String rawType, String... labels) {
            this.type = getTypeFor(rawType);
            this.labels = Arrays.asList(labels);

        }

        public String getLabel() {
            return labels.size() > 0 ? labels.get(0) : "no_label";
        }

        boolean isConstraintIndex() {
            return IndexInfoType.UNIQUE.equals(type);
        }

        private IndexInfoType getTypeFor(String rawType) {
            return rawType.equals("node_unique_property") || rawType.equals("UNIQUE")
                ? IndexInfoType.UNIQUE
                : IndexInfoType.LABEL;
        }

        enum IndexInfoType {
            LABEL,
            UNIQUE
        }

        @Override
        public String toString() {
            return "IndexInfo{" +
                "labels=" + labels +
                ", type=" + type +
                '}';
        }
    }

    static class ConstraintInfo {

    }
}
