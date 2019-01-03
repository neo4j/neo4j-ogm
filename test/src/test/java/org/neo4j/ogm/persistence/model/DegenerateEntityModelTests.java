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
package org.neo4j.ogm.persistence.model;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Result;
import org.neo4j.ogm.domain.filesystem.Document;
import org.neo4j.ogm.domain.filesystem.Folder;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.GraphTestUtils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * These tests are to establish the behaviour of degenerate entity models
 * An entity model is considered degenerate if a relationship that should
 * exist between two entities is only established on one of them.
 * For example if a parent object maintains a list of child objects
 * but a child object maintains a null (or incorrect) reference to its parent
 * the entity model is degenerate.
 * The OGM is designed to cope with such models.
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class DegenerateEntityModelTests extends MultiDriverTestClass {

    private Session session;

    private Folder f;

    private Document a;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.filesystem");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();

        Result executionResult = getGraphDatabaseService().execute(
            "CREATE (f:Folder { name: 'f' } )" +
                "CREATE (a:Document { name: 'a' } ) " +
                "CREATE (b:Document { name: 'b' } ) " +
                "CREATE (f)-[:CONTAINS]->(a) " +
                "CREATE (f)-[:CONTAINS]->(b) " +
                "RETURN id(f) AS fid, id(a) AS aid, id(b) AS bid");

        Map<String, Object> resultSet = executionResult.next();

        a = session.load(Document.class, (Long) resultSet.get("aid"));

        Document b = session.load(Document.class, (Long) resultSet.get("bid"));

        f = session.load(Folder.class, (Long) resultSet.get("fid"));
    }

    @Test
    public void testSaveDegenerateDocument() {

        // set a's f to a new f, but don't remove from the current f's list of documents
        a.setFolder(null);
        session.save(a);

        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (f:Folder {name : 'f' } )" +
                "CREATE (a:Document { name: 'a' } ) " +
                "CREATE (b:Document { name: 'b' } ) " +
                "CREATE (f)-[:CONTAINS]->(b)");
    }

    @Test
    public void testSaveDegenerateFolder() {

        // remove f's documents, but don't clear the documents' f reference
        f.setDocuments(new HashSet<Document>());

        session.save(f);

        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (f:Folder { name: 'f' } )" +
                "CREATE (a:Document { name: 'a' } ) " +
                "CREATE (b:Document { name: 'b' } ) ");
    }

    @Test
    public void testSaveDegenerateDocumentClone() {

        Document clone = new Document();
        clone.setId(a.getId());
        clone.setName(a.getName());
        clone.setFolder(null);

        session.save(clone);

        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (f:Folder { name: 'f' } )" +
                "CREATE (a:Document { name: 'a'} ) " +
                "CREATE (b:Document { name: 'b'} ) " +
                "CREATE (f)-[:CONTAINS]->(b)");
    }

    @Test
    public void testSaveDegenerateFolderClone() {

        Folder clone = new Folder();
        clone.setId(f.getId());
        clone.setName(f.getName());
        clone.setDocuments(new HashSet<Document>());

        session.save(clone);

        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (f:Folder { name: 'f' } )" +
                "CREATE (a:Document { name: 'a' } ) " +
                "CREATE (b:Document { name: 'b' } ) ");
    }

    @Test
    public void testSaveChangedDocument() {

        // set a's f to a new f, but don't remove from the current f's list of documents
        a.setFolder(new Folder());
        a.getFolder().setName("g");

        session.save(a);

        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (f:Folder { name: 'f' } )" +
                "CREATE (g:Folder { name: 'g' } ) " +
                "CREATE (a:Document { name: 'a' }) " +
                "CREATE (b:Document { name: 'b' }) " +
                "CREATE (f)-[:CONTAINS]->(b) " +
                "CREATE (g)-[:CONTAINS]->(a) ");
    }

    @Test
    public void testSaveChangedFolder() {

        Document c = new Document();
        c.setName("c");

        f.getDocuments().add(c);
        f.getDocuments().remove(a);

        session.save(f);

        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (f:Folder { name: 'f' })" +
                "CREATE (a:Document { name: 'a' } ) " +
                "CREATE (b:Document { name: 'b' } ) " +
                "CREATE (c:Document { name: 'c' } ) " +
                "CREATE (f)-[:CONTAINS]->(b) " +
                "CREATE (f)-[:CONTAINS]->(c) ");
    }
}
