/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.metadata;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.context.EntityGraphMapper;
import org.neo4j.ogm.context.EntityMapper;
import org.neo4j.ogm.context.MappingContext;
import org.neo4j.ogm.cypher.compiler.CompileContext;
import org.neo4j.ogm.cypher.compiler.Compiler;
import org.neo4j.ogm.domain.cineasts.annotated.User;
import org.neo4j.ogm.domain.pizza.Pizza;
import org.neo4j.ogm.exception.MetadataException;
import org.neo4j.ogm.session.request.RowStatementFactory;

/**
 * @author Mark Angrish
 */
public class MergeWithPrimaryIndexTests {

    private static MetaData mappingMetadata;
    private static MappingContext mappingContext;
    private EntityMapper mapper;

    @BeforeClass
    public static void setUpTestDatabase() {
        mappingMetadata = new MetaData("org.neo4j.ogm.domain.autoindex.valid", "org.neo4j.ogm.domain.cineasts.annotated", "org.neo4j.ogm.domain.pizza");
        mappingContext = new MappingContext(mappingMetadata);
    }

    @Before
    public void setUpMapper() {
        mappingContext = new MappingContext(mappingMetadata);
        this.mapper = new EntityGraphMapper(mappingMetadata, mappingContext);
    }

    @After
    public void cleanGraph() {
        mappingContext.clear();
    }

    @Test
    public void newNodeUsesGraphIdWhenPrimaryIndexNotPresent() {
        Pizza pizza = new Pizza("Plain");
        assertNull(pizza.getId());
        Compiler compiler = mapAndCompile(pizza);
        assertFalse(compiler.hasStatementsDependentOnNewNodes());
        assertEquals("UNWIND {rows} as row CREATE (n:`Pizza`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type",
                compiler.createNodesStatements().get(0).getStatement());
    }

    @Test
    public void newNodeUsesPrimaryIndexWhenPresent() {
        User newUser = new User("bachmania", "Michal Bachman", "password");
        assertNull(newUser.getId());
        Compiler compiler = mapAndCompile(newUser);
        assertFalse(compiler.hasStatementsDependentOnNewNodes());
        assertEquals("UNWIND {rows} as row MERGE (n:`User`{login: row.props.login}) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type",
                compiler.createNodesStatements().get(0).getStatement());
    }

    @Test(expected = MetadataException.class)
    public void exceptionRaisedWhenMoreThanOnePrimaryIndexDefinedInSameClass() {
        new MetaData("org.neo4j.ogm.domain.autoindex.invalid").classInfo("BadClass").primaryIndexField();
    }


    private Compiler mapAndCompile(Object object) {
        CompileContext context = this.mapper.map(object);
        Compiler compiler = context.getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());
        return compiler;
    }
}
