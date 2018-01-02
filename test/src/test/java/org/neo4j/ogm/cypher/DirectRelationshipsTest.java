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
package org.neo4j.ogm.cypher;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.context.EntityGraphMapper;
import org.neo4j.ogm.context.EntityMapper;
import org.neo4j.ogm.context.MappedRelationship;
import org.neo4j.ogm.context.MappingContext;
import org.neo4j.ogm.cypher.compiler.Compiler;
import org.neo4j.ogm.domain.filesystem.Document;
import org.neo4j.ogm.domain.filesystem.Folder;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.session.request.RowStatementFactory;

/**
 * This test suite contains tests of the cypher compiler output regarding
 * manipulation of direct relationships, i.e. relationships that are not
 * mediated in the entity model using RelationshipEntity instances.
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class DirectRelationshipsTest {

    private static MetaData mappingMetadata;
    private static MappingContext mappingContext;
    private EntityMapper mapper;

    @BeforeClass
    public static void setUpTestDatabase() {
        mappingMetadata = new MetaData("org.neo4j.ogm.domain.filesystem");
        mappingContext = new MappingContext(mappingMetadata);
    }

    @Before
    public void setUpMapper() {
        this.mapper = new EntityGraphMapper(mappingMetadata, mappingContext);
    }

    @After
    public void tidyUp() {
        mappingContext.clear();
    }

    @Test
    public void shouldSaveNewFolderDocumentPair() {

        Folder folder = new Folder();
        Document document = new Document();

        folder.getDocuments().add(document);
        document.setFolder(folder);

        Compiler compiler = mapper.map(folder).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        List<String> createNodeStatements = cypherStatements(compiler.createNodesStatements());
        assertThat(createNodeStatements).hasSize(2);
        assertThat(createNodeStatements.contains(
            "UNWIND {rows} as row CREATE (n:`Folder`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"))
            .isTrue();
        assertThat(createNodeStatements.contains(
            "UNWIND {rows} as row CREATE (n:`Document`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"))
            .isTrue();

        List<String> createRelStatements = cypherStatements(compiler.createRelationshipsStatements());
        assertThat(createRelStatements).hasSize(1);
        assertThat(createRelStatements.get(0)).isEqualTo(
            "UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`CONTAINS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type");

        compiler = mapper.map(document).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());
        createNodeStatements = cypherStatements(compiler.createNodesStatements());
        assertThat(createNodeStatements).hasSize(2);
        assertThat(createNodeStatements.contains(
            "UNWIND {rows} as row CREATE (n:`Folder`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"))
            .isTrue();
        assertThat(createNodeStatements.contains(
            "UNWIND {rows} as row CREATE (n:`Document`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"))
            .isTrue();

        createRelStatements = cypherStatements(compiler.createRelationshipsStatements());
        assertThat(createRelStatements).hasSize(1);
        assertThat(createRelStatements.get(0)).isEqualTo(
            "UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`CONTAINS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type");
    }

    @Test
    public void shouldSaveNewFolderWithTwoDocuments() {

        Folder folder = new Folder();
        Document doc1 = new Document();
        Document doc2 = new Document();

        folder.getDocuments().add(doc1);
        folder.getDocuments().add(doc2);

        doc1.setFolder(folder);
        doc2.setFolder(folder);

        //save folder
        Compiler compiler = mapper.map(folder).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        List<Statement> statements = compiler.createNodesStatements();
        List<String> createNodeStatements = cypherStatements(statements);
        assertThat(createNodeStatements).hasSize(2);
        assertThat(createNodeStatements.contains(
            "UNWIND {rows} as row CREATE (n:`Folder`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"))
            .isTrue();
        assertThat(createNodeStatements.contains(
            "UNWIND {rows} as row CREATE (n:`Document`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"))
            .isTrue();
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            if (statement.getStatement().contains("Folder")) {
                assertThat(rows).hasSize(1);
            }
            if (statement.getStatement().contains("Document")) {
                assertThat(rows).hasSize(2);
            }
        }

        statements = compiler.createRelationshipsStatements();
        List<String> createRelStatements = cypherStatements(statements);
        assertThat(createRelStatements).hasSize(1);
        assertThat(createRelStatements.get(0)).isEqualTo(
            "UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`CONTAINS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type");
        List rows = (List) statements.get(0).getParameters().get("rows");
        assertThat(rows).hasSize(2);

        //Save doc1
        compiler = mapper.map(doc1).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        statements = compiler.createNodesStatements();
        createNodeStatements = cypherStatements(statements);
        assertThat(createNodeStatements).hasSize(2);
        assertThat(createNodeStatements.contains(
            "UNWIND {rows} as row CREATE (n:`Folder`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"))
            .isTrue();
        assertThat(createNodeStatements.contains(
            "UNWIND {rows} as row CREATE (n:`Document`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"))
            .isTrue();
        for (Statement statement : statements) {
            rows = (List) statement.getParameters().get("rows");
            if (statement.getStatement().contains("Folder")) {
                assertThat(rows).hasSize(1);
            }
            if (statement.getStatement().contains("Document")) {
                assertThat(rows).hasSize(2);
            }
        }

        //Save doc2
        statements = compiler.createRelationshipsStatements();
        createRelStatements = cypherStatements(statements);
        assertThat(createRelStatements).hasSize(1);
        assertThat(createRelStatements.get(0)).isEqualTo(
            "UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`CONTAINS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type");
        rows = (List) statements.get(0).getParameters().get("rows");
        assertThat(rows).hasSize(2);

        compiler = mapper.map(doc2).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        statements = compiler.createNodesStatements();
        createNodeStatements = cypherStatements(statements);
        assertThat(createNodeStatements).hasSize(2);
        assertThat(createNodeStatements.contains(
            "UNWIND {rows} as row CREATE (n:`Folder`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"))
            .isTrue();
        assertThat(createNodeStatements.contains(
            "UNWIND {rows} as row CREATE (n:`Document`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"))
            .isTrue();
        for (Statement statement : statements) {
            rows = (List) statement.getParameters().get("rows");
            if (statement.getStatement().contains("Folder")) {
                assertThat(rows).hasSize(1);
            }
            if (statement.getStatement().contains("Document")) {
                assertThat(rows).hasSize(2);
            }
        }

        statements = compiler.createRelationshipsStatements();
        createRelStatements = cypherStatements(statements);
        assertThat(createRelStatements).hasSize(1);
        assertThat(createRelStatements.get(0)).isEqualTo(
            "UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`CONTAINS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type");
        rows = (List) statements.get(0).getParameters().get("rows");
        assertThat(rows).hasSize(2);
    }

    @Test
    public void shouldNotBeAbleToCreateDuplicateRelationship() {

        Folder folder = new Folder();
        Document document = new Document();

        document.setFolder(folder);

        // we try to store two identical references to the document object. Although this
        // is supported by the graph, it isn't currently supported by the OGM,
        // therefore we expect only one relationship to be persisted

        folder.getDocuments().add(document);
        folder.getDocuments().add(document);

        assertThat(folder.getDocuments()).hasSize(2);

        //save folder
        Compiler compiler = mapper.map(folder).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        List<Statement> statements = compiler.createNodesStatements();
        List<String> createNodeStatements = cypherStatements(statements);
        assertThat(createNodeStatements).hasSize(2);
        assertThat(createNodeStatements.contains(
            "UNWIND {rows} as row CREATE (n:`Folder`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"))
            .isTrue();
        assertThat(createNodeStatements.contains(
            "UNWIND {rows} as row CREATE (n:`Document`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"))
            .isTrue();
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertThat(rows).hasSize(1);
        }

        statements = compiler.createRelationshipsStatements();
        List<String> createRelStatements = cypherStatements(statements);
        assertThat(createRelStatements).hasSize(1);
        assertThat(createRelStatements.get(0)).isEqualTo(
            "UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`CONTAINS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type");
        List rows = (List) statements.get(0).getParameters().get("rows");
        assertThat(rows).hasSize(1);

        //save document
        compiler = mapper.map(document).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        statements = compiler.createNodesStatements();
        createNodeStatements = cypherStatements(statements);
        assertThat(createNodeStatements).hasSize(2);
        assertThat(createNodeStatements.contains(
            "UNWIND {rows} as row CREATE (n:`Folder`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"))
            .isTrue();
        assertThat(createNodeStatements.contains(
            "UNWIND {rows} as row CREATE (n:`Document`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"))
            .isTrue();
        for (Statement statement : statements) {
            rows = (List) statement.getParameters().get("rows");
            assertThat(rows).hasSize(1);
        }

        statements = compiler.createRelationshipsStatements();
        createRelStatements = cypherStatements(statements);
        assertThat(createRelStatements).hasSize(1);
        assertThat(createRelStatements.get(0)).isEqualTo(
            "UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`CONTAINS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type");
        rows = (List) statements.get(0).getParameters().get("rows");
        assertThat(rows).hasSize(1);
    }

    @Test
    public void shouldBeAbleToCreateDifferentRelationshipsToTheSameDocument() {

        Folder folder = new Folder();
        Document document = new Document();

        document.setFolder(folder);

        folder.getDocuments().add(document);
        folder.getArchived().add(document);

        //save folder
        Compiler compiler = mapper.map(folder).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        List<Statement> statements = compiler.createNodesStatements();
        List<String> createNodeStatements = cypherStatements(statements);
        assertThat(createNodeStatements).hasSize(2);
        assertThat(createNodeStatements.contains(
            "UNWIND {rows} as row CREATE (n:`Folder`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"))
            .isTrue();
        assertThat(createNodeStatements.contains(
            "UNWIND {rows} as row CREATE (n:`Document`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"))
            .isTrue();
        for (Statement statement : statements) {
            List rows = (List) statement.getParameters().get("rows");
            assertThat(rows).hasSize(1);
        }

        statements = compiler.createRelationshipsStatements();
        List<String> createRelStatements = cypherStatements(statements);
        assertThat(createRelStatements).hasSize(2);
        assertThat(createRelStatements.contains(
            "UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`CONTAINS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type"))
            .isTrue();
        assertThat(createRelStatements.contains(
            "UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`ARCHIVED`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type"))
            .isTrue();
        boolean archivedType = false, containsType = false;
        for (Statement statement : statements) {
            if (statement.getStatement().contains("ARCHIVED")) {
                archivedType = true;
            }
            if (statement.getStatement().contains("CONTAINS")) {
                containsType = true;
            }
        }
        assertThat(archivedType).isTrue();
        assertThat(containsType).isTrue();

        //save document
        compiler = mapper.map(document).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        statements = compiler.createNodesStatements();
        createNodeStatements = cypherStatements(statements);
        assertThat(createNodeStatements).hasSize(2);
        assertThat(createNodeStatements.contains(
            "UNWIND {rows} as row CREATE (n:`Folder`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"))
            .isTrue();
        assertThat(createNodeStatements.contains(
            "UNWIND {rows} as row CREATE (n:`Document`) SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type"))
            .isTrue();

        statements = compiler.createRelationshipsStatements();
        createRelStatements = cypherStatements(statements);
        assertThat(createRelStatements).hasSize(2);
        assertThat(createRelStatements.contains(
            "UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`CONTAINS`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type"))
            .isTrue();
        assertThat(createRelStatements.contains(
            "UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MERGE (startNode)-[rel:`ARCHIVED`]->(endNode) RETURN row.relRef as ref, ID(rel) as id, row.type as type"))
            .isTrue();
        archivedType = false;
        containsType = false;
        for (Statement statement : statements) {
            if (statement.getStatement().contains("ARCHIVED")) {
                archivedType = true;
            }
            if (statement.getStatement().contains("CONTAINS")) {
                containsType = true;
            }
        }
        assertThat(archivedType).isTrue();
        assertThat(containsType).isTrue();
    }

    @Test
    public void shouldBeAbleToRemoveTheOnlyRegisteredRelationship() {

        Folder folder = new Folder();
        Document document = new Document();

        folder.getDocuments().add(document);
        document.setFolder(folder);

        folder.setId(0L);
        document.setId(1L);

        mappingContext.addNodeEntity(folder);
        mappingContext.addNodeEntity(document);
        mappingContext.addRelationship(
            new MappedRelationship(folder.getId(), "CONTAINS", document.getId(), Folder.class, Document.class));

        document.setFolder(null);
        folder.getDocuments().clear();

        Compiler compiler = mapper.map(folder).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        List<Statement> statements = compiler.deleteRelationshipStatements();
        assertThat(statements).hasSize(1);
        assertThat(statements.get(0).getStatement()).isEqualTo(
            "UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MATCH (startNode)-[rel:`CONTAINS`]->(endNode) DELETE rel");

        // we need to re-establish the relationship in the mapping context for this expectation, otherwise
        // the previous save will have de-registered the relationship.
        mappingContext.addRelationship(
            new MappedRelationship(folder.getId(), "CONTAINS", document.getId(), Folder.class, Document.class));

        compiler = mapper.map(document).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        statements = compiler.deleteRelationshipStatements();
        assertThat(statements).hasSize(1);
        assertThat(statements.get(0).getStatement()).isEqualTo(
            "UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MATCH (startNode)-[rel:`CONTAINS`]->(endNode) DELETE rel");
    }

    @Test
    public void shouldBeAbleToRemoveAnyRegisteredRelationship() {

        // given
        Folder folder = new Folder();
        Document doc1 = new Document();
        Document doc2 = new Document();

        folder.getDocuments().add(doc1);
        folder.getDocuments().add(doc2);
        doc1.setFolder(folder);
        doc2.setFolder(folder);

        folder.setId(0L);
        doc1.setId(1L);
        doc2.setId(2L);

        mappingContext.addNodeEntity(folder);
        mappingContext.addNodeEntity(doc1);
        mappingContext.addNodeEntity(doc2);
        mappingContext.addRelationship(
            new MappedRelationship(folder.getId(), "CONTAINS", doc1.getId(), Folder.class, Document.class));
        mappingContext.addRelationship(
            new MappedRelationship(folder.getId(), "CONTAINS", doc2.getId(), Folder.class, Document.class));

        // when
        doc2.setFolder(null);
        folder.getDocuments().remove(doc2);

        // then
        assertThat(folder.getDocuments()).hasSize(1);

        Compiler compiler = mapper.map(folder).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        List<Statement> statements = compiler.deleteRelationshipStatements();
        assertThat(statements).hasSize(1);
        assertThat(statements.get(0).getStatement()).isEqualTo(
            "UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MATCH (startNode)-[rel:`CONTAINS`]->(endNode) DELETE rel");

        // we need to re-establish the relationship in the mapping context for this expectation, otherwise
        // the previous save will have de-registered the relationship.
        mappingContext.addRelationship(
            new MappedRelationship(folder.getId(), "CONTAINS", doc2.getId(), Folder.class, Document.class));
        compiler = mapper.map(doc1).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        statements = compiler.deleteRelationshipStatements();
        assertThat(statements).hasSize(1);
        assertThat(statements.get(0).getStatement()).isEqualTo(
            "UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MATCH (startNode)-[rel:`CONTAINS`]->(endNode) DELETE rel");

        // we need to re-establish the relationship in the mapping context for this expectation, otherwise
        // the previous save will have de-registered the relationship.
        mappingContext.addRelationship(
            new MappedRelationship(folder.getId(), "CONTAINS", doc2.getId(), Folder.class, Document.class));
        compiler = mapper.map(doc2).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        statements = compiler.deleteRelationshipStatements();
        assertThat(statements).hasSize(1);
        assertThat(statements.get(0).getStatement()).isEqualTo(
            "UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MATCH (startNode)-[rel:`CONTAINS`]->(endNode) DELETE rel");
    }

    @Test
    public void shouldBeAbleToRemoveContainedRelationshipOnly() {

        // given
        Folder folder = new Folder();
        Document doc1 = new Document();

        folder.getDocuments().add(doc1);
        folder.getArchived().add(doc1);
        doc1.setFolder(folder);

        folder.setId(0L);
        doc1.setId(1L);

        mappingContext.addNodeEntity(folder);
        mappingContext.addNodeEntity(doc1);
        mappingContext.addRelationship(
            new MappedRelationship(folder.getId(), "CONTAINS", doc1.getId(), Folder.class, Document.class));
        mappingContext.addRelationship(
            new MappedRelationship(folder.getId(), "ARCHIVED", doc1.getId(), Folder.class, Document.class));

        // when
        folder.getDocuments().remove(doc1);
        doc1.setFolder(null);
        // then
        assertThat(folder.getDocuments()).isEmpty();
        assertThat(folder.getArchived()).hasSize(1);

        Compiler compiler = mapper.map(folder).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        List<Statement> statements = compiler.deleteRelationshipStatements();
        assertThat(statements).hasSize(1);
        assertThat(statements.get(0).getStatement()).isEqualTo(
            "UNWIND {rows} as row MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode MATCH (endNode) WHERE ID(endNode) = row.endNodeId MATCH (startNode)-[rel:`CONTAINS`]->(endNode) DELETE rel");

        mapper = new EntityGraphMapper(mappingMetadata, mappingContext);
        //There are no more changes to the graph
        compiler = mapper.map(doc1).getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        statements = compiler.deleteRelationshipStatements();
        assertThat(statements).isEmpty();
    }

    private List<String> cypherStatements(List<Statement> statements) {
        List<String> cypher = new ArrayList<>(statements.size());
        for (Statement statement : statements) {
            cypher.add(statement.getStatement());
        }
        return cypher;
    }
}
