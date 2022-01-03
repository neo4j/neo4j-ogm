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
package org.neo4j.ogm.persistence.session.lifecycle;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.filesystem.Document;
import org.neo4j.ogm.domain.filesystem.Folder;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * These tests define the behaviour of the OGM with regard to
 * stale object detection.
 * <p/>
 * The actual test cases are the same as the ones in DegenerateEntityModelTests
 * which are known to correctly register the underlying database.
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
public class StaleObjectTest extends TestContainersTestBase {

    private Folder f;
    private Document a;
    private Document b;

    private Session session;

    @Before
    public void init() throws IOException {
        SessionFactory sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.filesystem");
        session = sessionFactory.openSession();
        session.purgeDatabase();

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

        assertThat(a.toString()).isEqualTo("Document{folder=Folder{name='f', documents=2, archived=0}, name='a'}");
        assertThat(b.toString()).isEqualTo("Document{folder=Folder{name='f', documents=2, archived=0}, name='b'}");
    }

    @Test
    public void testSaveDegenerateDocument() {

        // note that we don't clear the current folder object.
        a.setFolder(null);

        session.save(a);

        Folder p = session.load(Folder.class, f.getId());

        assertThat(p.toString()).isEqualTo("Folder{name='f', documents=1, archived=0}");

        // the document object loaded into the session by virtue of reloading f is no longer b. we guarantee to fetch the latest version of all reachable objects
        // and we overwrite objects in the mapping context.

        // directly after a save all objects in the save tree are guaranteed to not be dirty
        // directly after a load, all objects in the load tree are guaranteed to not be dirty

        assertThat(p.getDocuments().iterator().next() == b).isFalse();

        assertThat(a.toString()).isEqualTo("Document{folder=null, name='a'}");
        assertThat(b.toString()).isEqualTo(
            "Document{folder=Folder{name='f', documents=2, archived=0}, name='b'}");   // b is attached to f, which hasn't been saved or reloaded, so is unchanged

        assertThat(p.getDocuments().iterator().next().toString())
            .isEqualTo("Document{folder=Folder{name='f', documents=1, archived=0}, name='b'}");
    }

    @Test
    public void testSaveDegenerateFolder() {

        // note that we don't clear any of the document object's folder references.
        f.getDocuments().clear();

        session.save(f);

        assertThat(f.toString()).isEqualTo("Folder{name='f', documents=0, archived=0}");
        assertThat(a.toString()).isEqualTo("Document{folder=Folder{name='f', documents=0, archived=0}, name='a'}");
        assertThat(b.toString()).isEqualTo("Document{folder=Folder{name='f', documents=0, archived=0}, name='b'}");

        Document aa = session.load(Document.class, a.getId());
        Document bb = session.load(Document.class, b.getId());

        assertThat(aa.toString()).isEqualTo("Document{folder=null, name='a'}");
        assertThat(bb.toString()).isEqualTo("Document{folder=null, name='b'}");
    }
}
