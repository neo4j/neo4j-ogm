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

package org.neo4j.ogm.unit.session.lifecycle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.filesystem.Document;
import org.neo4j.ogm.domain.filesystem.Folder;
import org.neo4j.ogm.session.SessionFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * These tests define the behaviour of the OGM with regard to
 * stale object detection.
 *
 * The actual test cases are the same as the ones in DegenerateEntityModelTests
 * which are known to correctly configure the underlying database.
 *
 * Because the OGM uses an object cache (to detect dirty objects, and/or deleted
 * relationships), we must ensure that changes to the database by a save() are always
 * accurately reflected by the corresponding get()
 *
 * Example:
 *
 *    f: { name: 'f', documents : [ { name: 'a'},  { name: 'b' } ] }
 *    a: { name: 'a', folder : { name: 'f' }}
 *    b: { name: 'b', folder : { name: 'f' }}
 *
 * If we now deleted 'a's reference to 'f' and saved a, we should
 * expect that when we retrieve 'f' it won't hold a reference to 'a'
 * any longer.
 *
 */
public class StaleObjectTest extends LifecycleTest {

    private static SessionFactory sessionFactory;

    private Folder f;
    private Document a;
    private Document b;

    @Before
    public void init() throws IOException {
        setUp();

        sessionFactory = new SessionFactory("org.neo4j.ogm.domain.filesystem");
        session = sessionFactory.openSession("http://localhost:" + neoPort);

        a = new Document();
        a.setName("a");

        b = new Document();
        b.setName("b");

        f = new Folder();
        f.setName("f");

        f.getDocuments().add(a);
        f.getDocuments().add(b);

        a.setFolder(f);
        b.setFolder(f);

        session.save(f);
        //session.clear();


    }

    @After
    public void tearDownTest() {
        tearDown();
    }

    @Test
    public void testSaveDegenerateDocument() {

        // note that we don't clear the current folder object.
        a.setFolder(null);

        session.save(a);

        Folder p = session.load(Folder.class, f.getId());

        assertEquals("Folder{name='f', documents=1}", p.toString());

    }

    @Test
    public void testSaveDegenerateFolder() {

        // note that we don't clear the current document object's folder references.
        f.getDocuments().clear();

        session.save(f);

        Document aa = session.load(Document.class, a.getId());
        Document bb = session.load(Document.class, b.getId());

        assertEquals("Document{folder=null, name='a'}", aa.toString());
        assertEquals("Document{folder=null, name='b'}", bb.toString());


    }
}
