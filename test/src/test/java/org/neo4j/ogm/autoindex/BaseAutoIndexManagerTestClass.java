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

package org.neo4j.ogm.autoindex;

import com.google.common.collect.ObjectArrays;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.ConstraintDefinition;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Must not end with "Test" so it does not run on TC.
 *
 * @author Frantisek Hartman
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
    private String definition;

    private GraphDatabaseService service;
    protected MetaData metaData;
    protected SessionFactory sessionFactory;

    public BaseAutoIndexManagerTestClass(String definition, String... packages) {
        this.definition = definition;
        this.metaData = new MetaData(packages);
        sessionFactory = new SessionFactory(driver, packages);
    }

    @Before
    public void setUp() throws Exception {
        service = getGraphDatabaseService();

        service.execute("MATCH (n) DETACH DELETE n");

        if (isEnterpriseEdition() && isVersionOrGreater("3.2.0")) {
            indexes = ENTERPRISE_INDEXES;
            constraints = ENTERPRISE_CONSTRAINTS;
            statements = ObjectArrays.concat(ENTERPRISE_INDEXES, ENTERPRISE_CONSTRAINTS, String.class);
        } else {
            indexes = COMMUNITY_INDEXES;
            constraints = COMMUNITY_CONSTRAINTS;
            statements = ObjectArrays.concat(COMMUNITY_INDEXES, COMMUNITY_CONSTRAINTS, String.class);
        }
    }

    @After
    public void tearDown() throws Exception {
        executeDrop(definition);
        executeDrop(statements);
    }

    @Test
    public void testAutoIndexNoneNoIndexIsCreated() throws Exception {
        runAutoIndex("none");

        executeForIndexes(indexes -> {
            assertThat(indexes).isEmpty();
        });
        executeForConstraints(constraints -> {
            assertThat(constraints).isEmpty();
        });
    }

    @Test
    public void testAutoIndexNoneNoIndexIsDropped() throws Exception {
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
        executeCreate(definition);

        runAutoIndex("validate");
    }

    @Test
    public void testIndexValidationFailsOnMissingIndex() throws Exception {
        assertThatThrownBy(() -> runAutoIndex("validate"))
            .isInstanceOf(MissingIndexException.class)
            .hasMessageContaining("Validation of Constraints and Indexes failed. Could not find the following :")
            .hasMessageContaining(definition);
    }

    @Test
    public void testAutoIndexAssertDropsAllIndexesAndCreatesExisting() throws Exception {
        executeCreate(statements);

        runAutoIndex("assert");

        List<Object> all = new ArrayList<>();
        executeForIndexes(all::addAll);
        executeForConstraints(all::addAll);

        assertThat(all).hasSize(1);
    }

    @Test
    public void testAutoIndexUpdateKeepIndexesAndCreateNew() throws Exception {
        executeCreate(statements);

        runAutoIndex("update");

        List<Object> all = new ArrayList<>();
        executeForIndexes(all::addAll);
        executeForConstraints(all::addAll);

        int expectedNumberOfIndexes = this.indexes.length + this.constraints.length + 1;
        assertThat(all).hasSize(expectedNumberOfIndexes);
    }

    @Test
    public void testAutoIndexUpdateIndexExistsDoNothing() throws Exception {
        executeCreate(definition);

        runAutoIndex("update");

        List<Object> all = new ArrayList<>();
        executeForIndexes(all::addAll);
        executeForConstraints(all::addAll);

        assertThat(all).hasSize(1);
    }

    @Test
    public void testAutoIndexDumpCreatesIndex() throws IOException {

        File file = File.createTempFile("test", ".cql");

        try {
            Configuration configuration = getBaseConfiguration()
                .autoIndex("dump")
                .generatedIndexesOutputDir(file.getParent())
                .generatedIndexesOutputFilename(file.getName())
                .build();

            new AutoIndexManager(metaData, configuration, sessionFactory);

            assertThat(file.exists()).isTrue();
            try (InputStream is = new FileInputStream(file)) {
                String actual = IOUtils.toString(is);
                assertThat(actual).isEqualToIgnoringWhitespace("CREATE " + definition);
            }

        } finally {
            file.delete();
        }
    }

    void runAutoIndex(String mode) {
        Configuration configuration = getBaseConfiguration().autoIndex(mode).build();
        new AutoIndexManager(metaData, configuration, sessionFactory);
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
            Iterable<ConstraintDefinition> constraints = service.schema().getConstraints();
            consumer.accept(newArrayList(constraints));
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
