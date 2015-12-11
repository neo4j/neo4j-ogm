/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.persistence.model;

import org.junit.After;
import org.junit.Before;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.mappingcontext.EntityGraphMapper;
import org.neo4j.ogm.mappingcontext.EntityMapper;
import org.neo4j.ogm.mappingcontext.MappingContext;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.request.Statements;
import org.neo4j.ogm.testutil.GraphTestUtils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @author Mark Angrish
 */
public abstract class MappingTrait extends MultiDriverTestClass {

    protected static MappingContext mappingContext;
    protected static ExecutionEngine executionEngine;
    private static GraphDatabaseService graphDatabase;
    private static MetaData mappingMetadata;
    protected EntityMapper mapper;

    public static void setUp(String... packages) {
        graphDatabase = getGraphDatabaseService();
        executionEngine = new ExecutionEngine(graphDatabase);
        mappingMetadata = new MetaData(packages);
        mappingContext = new MappingContext(mappingMetadata);
    }

    @Before
    public void setUpMapper() {
        this.mapper = new EntityGraphMapper(mappingMetadata, mappingContext);
    }

    @After
    public void cleanGraph() {
        executionEngine.execute("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r, n");
        mappingContext.clear();
    }

    public void saveAndVerify(Object domainObject, String sameGraphCypher) {

        save(domainObject);
        GraphTestUtils.assertSameGraph(graphDatabase, sameGraphCypher);
    }

    public void save(Object domainObject) {
        Statements cypher = new Statements(this.mapper.map(domainObject).getStatements());

        assertNotNull("The resultant cypher statements shouldn't be null", cypher.getStatements());
        assertFalse("The resultant cypher statements shouldn't be empty", cypher.getStatements().isEmpty());

        for (Statement query : cypher.getStatements()) {
            System.out.println("compiled: " + query.getStatement() + ", props: " + query.getParameters());
            executionEngine.execute(query.getStatement(), query.getParameters());
        }
    }


}
