/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.unit.mapper;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.cypher.statement.ParameterisedStatement;
import org.neo4j.ogm.cypher.statement.ParameterisedStatements;
import org.neo4j.ogm.mapper.EntityGraphMapper;
import org.neo4j.ogm.mapper.EntityToGraphMapper;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.testutil.GraphTestUtils;
import org.neo4j.test.TestGraphDatabaseFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public abstract class MappingTest
{
    protected EntityToGraphMapper mapper;

    private static GraphDatabaseService graphDatabase;
    private static ExecutionEngine executionEngine;
    private static MetaData mappingMetadata;
    private static MappingContext mappingContext;

    public static void setUp(String... packages) {
        graphDatabase = new TestGraphDatabaseFactory().newImpermanentDatabase();
        executionEngine = new ExecutionEngine(graphDatabase);
        mappingMetadata = new MetaData(packages);
        mappingContext = new MappingContext(mappingMetadata);
    }

    @AfterClass
    public static void shutDownDatabase() {
        graphDatabase.shutdown();
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

    public ExecutionResult execute(String statement) {
        return executionEngine.execute(statement);
    }

    public void saveAndVerify(Object domainObject, String sameGraphCypher) {

        save(domainObject);
        GraphTestUtils.assertSameGraph(graphDatabase, sameGraphCypher);
    }

    public void save(Object domainObject) {
        ParameterisedStatements cypher = new ParameterisedStatements(this.mapper.map(domainObject).getStatements());

        assertNotNull("The resultant cypher statements shouldn't be null", cypher.getStatements());
        assertFalse("The resultant cypher statements shouldn't be empty", cypher.getStatements().isEmpty());

        for (ParameterisedStatement query : cypher.getStatements()) {
            System.out.println("compiled: " + query.getStatement());
            executionEngine.execute(query.getStatement(), query.getParameters());
        }
    }



}
