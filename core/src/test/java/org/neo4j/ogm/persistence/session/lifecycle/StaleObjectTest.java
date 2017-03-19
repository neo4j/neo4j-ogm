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

package org.neo4j.ogm.persistence.session.lifecycle;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.filesystem.Document;
import org.neo4j.ogm.domain.filesystem.Folder;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * These tests define the behaviour of the OGM with regard to
 * stale object detection.
 * <p/>
 * The actual test cases are the same as the ones in DegenerateEntityModelTests
 * which are known to correctly configure the underlying database.
 * <p/>
 * Because the OGM uses an object cache (to detect dirty objects, and/or deleted
 * relationships), we must ensure that changes to the database by a save() are always
 * accurately reflected by the corresponding get()
 * <p/>
 * Example:
 * <p/>
 * f: { name: 'f', documents : [ { name: 'a'},  { name: 'b' } ] }
 * a: { name: 'a', folder : { name: 'f' }}
 * b: { name: 'b', folder : { name: 'f' }}
 * <p/>
 * If we now deleted 'a's reference to 'f' and saved a, we should
 * expect that when we retrieve 'f' it won't hold a reference to 'a'
 * any longer.
 *
 * @author Vince Bickers
 */
public class StaleObjectTest extends MultiDriverTestClass {

    private Folder f;
    private Document a;
    private Document b;

    private Session session;

    @Before
    public void init() throws IOException {
        SessionFactory sessionFactory = new SessionFactory(baseConfiguration, "org.neo4j.ogm.domain.filesystem");
        session = sessionFactory.openSession();

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

        assertEquals("Document{folder=Folder{name='f', documents=2, archived=0}, name='a'}", a.toString());
        assertEquals("Document{folder=Folder{name='f', documents=2, archived=0}, name='b'}", b.toString());
    }

    @Test
    public void testSaveDegenerateDocument() {

        // note that we don't clear the current folder object.
        a.setFolder(null);

        session.save(a);

        Folder p = session.load(Folder.class, f.getId());

        assertEquals("Folder{name='f', documents=1, archived=0}", p.toString());

        // the document object loaded into the session by virtue of reloading f is no longer b. we guarantee to fetch the latest version of all reachable objects
        // and we overwrite objects in the mapping context.

        // directly after a save all objects in the save tree are guaranteed to not be dirty
        // directly after a load, all objects in the load tree are guaranteed to not be dirty

        assertFalse(p.getDocuments().iterator().next() == b);

        assertEquals("Document{folder=null, name='a'}", a.toString());
        assertEquals("Document{folder=Folder{name='f', documents=2, archived=0}, name='b'}", b.toString());   // b is attached to f, which hasn't been saved or reloaded, so is unchanged

        assertEquals("Document{folder=Folder{name='f', documents=1, archived=0}, name='b'}", p.getDocuments().iterator().next().toString());
    }

    @Test
    public void testSaveDegenerateFolder() {

        // note that we don't clear any of the document object's folder references.
        f.getDocuments().clear();

        session.save(f);

        assertEquals("Folder{name='f', documents=0, archived=0}", f.toString());
        assertEquals("Document{folder=Folder{name='f', documents=0, archived=0}, name='a'}", a.toString());
        assertEquals("Document{folder=Folder{name='f', documents=0, archived=0}, name='b'}", b.toString());

        Document aa = session.load(Document.class, a.getId());
        Document bb = session.load(Document.class, b.getId());

        assertEquals("Document{folder=null, name='a'}", aa.toString());
        assertEquals("Document{folder=null, name='b'}", bb.toString());
    }
}
