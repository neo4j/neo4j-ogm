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

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.ConstraintDefinition;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Must not end with "Test" so it does not run on TC.
 *
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public abstract class BaseAutoIndexManagerTestClass extends MultiDriverTestClass {

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

    private GraphDatabaseService service;
    protected SessionFactory sessionFactory;

    public BaseAutoIndexManagerTestClass(String[] expectedIndexDefinitions, Class<?>... packages) {
        sessionFactory = new SessionFactory(driver, Arrays.stream(packages).map(Class::getName).toArray(String[]::new));

        this.expectedIndexDefinitions = expectedIndexDefinitions;
    }

    @Before
    public void setUp() {
        service = getGraphDatabaseService();

        service.execute("MATCH (n) DETACH DELETE n");
        String[] existingConstraints = service.execute("CALL db.constraints()").stream().map(r -> r.get("description")).toArray(String[]::new);
        executeDrop(existingConstraints);

        if (isEnterpriseEdition() && isVersionOrGreater("3.2.0")) {
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
    public void tearDown() throws Exception {
        executeDrop(expectedIndexDefinitions);
        executeDrop(statements);
    }

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

        Configuration configuration = getBaseConfiguration()
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
        Configuration configuration = getBaseConfiguration().autoIndex(mode).build();
        sessionFactory.runAutoIndexManager(configuration);
    }

    void executeForIndexes(Consumer<List<IndexDefinition>> consumer) {
        try (Transaction tx = service.beginTx()) {
            Iterable<IndexDefinition> indexes = service.schema().getIndexes();
            List<IndexDefinition> pureIndexes = StreamSupport.stream(indexes.spliterator(), false)
                .filter(indexDefinition -> !indexDefinition.isConstraintIndex())
                .collect(toList());
            consumer.accept(pureIndexes);
            tx.success();
        }
    }

    void executeForConstraints(Consumer<List<ConstraintDefinition>> consumer) {
        try (Transaction tx = service.beginTx()) {
            List<ConstraintDefinition> constraints = StreamSupport
                .stream(service.schema().getConstraints().spliterator(), false)
                .collect(toList());
            consumer.accept(constraints);
            tx.success();
        }
    }

    void executeCreate(String... statements) {
        for (String statement : statements) {
            logger.info("Execute CREATE " + statement);
            Result execute = service.execute("CREATE " + statement);
            execute.close();
        }
    }

    void executeDrop(String... statements) {
        for (String statement : statements) {
            // need to handle transaction manually because when the service.execute fails with exception
            // it does not clean up the tx resources, leading to deadlock later
            Transaction tx = service.beginTx();
            try {
                service.execute("DROP " + statement);

                tx.success();
            } catch (Exception e) {
                logger.trace("Could not execute drop for statement (this is likely expected) {}", statement, e);
                tx.failure();
            }
            tx.close();
        }
    }
}
