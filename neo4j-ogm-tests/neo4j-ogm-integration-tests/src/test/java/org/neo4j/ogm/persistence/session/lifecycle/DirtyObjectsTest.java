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
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.filesystem.Document;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Vince Bickers
 */
public class DirtyObjectsTest extends TestContainersTestBase {

    private Neo4jSession session;
    private SessionFactory sessionFactory;

    @Before
    public void init() throws IOException {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.filesystem");
        session = (Neo4jSession) sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void newObjectShouldBeDirty() {

        Document d = new Document();
        assertThat(session.context().isDirty(d)).isTrue();
    }

    @Test
    public void savedObjectShouldNotBeDirty() {

        Document d = new Document();
        session.save(d);
        assertThat(session.context().isDirty(d)).isFalse();
    }

    @Test
    public void modifiedLoadedObjectShouldBeDirty() {

        Document d = new Document();
        session.save(d);
        d.setName("Document");
        assertThat(session.context().isDirty(d)).isTrue();
    }

    @Test
    public void unmodifiedLoadedObjectShouldNotBeDirty() {

        Document d = new Document();
        session.save(d);
        d = session.load(Document.class, d.getId());
        assertThat(session.context().isDirty(d)).isFalse();
    }

    @Test
    public void evictedObjectShouldBeDirty() {

        Document d = new Document();
        session.save(d);
        session.clear();
        assertThat(session.context().isDirty(d)).isTrue();
    }

    @Test
    public void evictedObjectThatIsIdenticalToTheLoadedObjectShouldNotBeDirty() {

        Document d = new Document();

        session.save(d);
        session.clear();
        Document d2 = session.load(Document.class, d.getId());

        assertThat(session.context().isDirty(d)).isFalse();
        assertThat(session.context().isDirty(d2)).isFalse();
    }

    @Test
    public void reloadingAnObjectReturnsTheCachedInstance() {

        Document d = new Document();

        session.save(d);
        assertThat(session.load(Document.class, d.getId())).isSameAs(d);
    }

    @Test
    public void reloadingAnObjectReturnsTheCachedInstanceEvenIfItIsChangedInTheDatabase() {

        Document d = new Document();
        session.save(d);

        // perform an out-of-session update on the object
        sessionFactory.openSession().query("MATCH (n) SET n.name='Document'", Collections.emptyMap());

        // get a copy of the document into a different session
        Document d2 = sessionFactory.openSession().load(Document.class, d.getId());
        assertThat(d2.getName()).isEqualTo("Document");

        // now get a copy of the object from the original session
        Document d3 = session.load(Document.class, d.getId());

        // it does not reflect the changes made in the database
        assertThat(d3.getName()).isEqualTo(null);
        // and in fact, d3 and d are the same object.
        assertThat(d3).isSameAs(d);
    }
}
