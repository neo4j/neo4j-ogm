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
package org.neo4j.ogm.metadata;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.ogm.context.EntityGraphMapper;
import org.neo4j.ogm.context.EntityMapper;
import org.neo4j.ogm.context.MappingContext;
import org.neo4j.ogm.cypher.compiler.CompileContext;
import org.neo4j.ogm.cypher.compiler.Compiler;
import org.neo4j.ogm.domain.cineasts.annotated.User;
import org.neo4j.ogm.domain.pizza.Pizza;
import org.neo4j.ogm.exception.core.MetadataException;
import org.neo4j.ogm.session.request.RowStatementFactory;

/**
 * @author Mark Angrish
 */
public class MergeWithPrimaryIndexTests {

    private static MetaData mappingMetadata;
    private static MappingContext mappingContext;
    private EntityMapper mapper;

    @BeforeAll
    public static void setUpTestDatabase() {
        mappingMetadata = new MetaData("org.neo4j.ogm.domain.metadata",
            "org.neo4j.ogm.domain.cineasts.annotated", "org.neo4j.ogm.domain.pizza");
        mappingContext = new MappingContext(mappingMetadata);
    }

    @BeforeEach
    public void setUpMapper() {
        mappingContext = new MappingContext(mappingMetadata);
        this.mapper = new EntityGraphMapper(mappingMetadata, mappingContext);
    }

    @AfterEach
    public void cleanGraph() {
        mappingContext.clear();
    }

    @Test
    void newNodeUsesGraphIdWhenPrimaryIndexNotPresent() {
        Pizza pizza = new Pizza("Plain");
        assertThat(pizza.getId()).isNull();
        Compiler compiler = mapAndCompile(pizza);
        assertThat(compiler.hasStatementsDependentOnNewNodes()).isFalse();
        assertThat(compiler.createNodesStatements().get(0).getStatement()).isEqualTo(
            "UNWIND $rows as row CREATE (n:`Pizza`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, $type as type");
    }

    @Test
    void newNodeUsesPrimaryIndexWhenPresent() {
        User newUser = new User("bachmania", "Michal Bachman", "password");
        Compiler compiler = mapAndCompile(newUser);
        assertThat(compiler.hasStatementsDependentOnNewNodes()).isFalse();
        assertThat(compiler.createNodesStatements().get(0).getStatement()).isEqualTo(
            "UNWIND $rows as row MERGE (n:`User`{login: row.props.login}) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, $type as type");
    }

    @Test
    void exceptionRaisedWhenMoreThanOnePrimaryIndexDefinedInSameClass() {
        assertThrows(MetadataException.class, () -> {
            new MetaData("org.neo4j.ogm.domain.invalid").classInfo("BadClass").primaryIndexField();
        });
    }

    private Compiler mapAndCompile(Object object) {
        CompileContext context = this.mapper.map(object);
        Compiler compiler = context.getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());
        return compiler;
    }
}
