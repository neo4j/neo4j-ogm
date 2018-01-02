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

package org.neo4j.ogm.persistence.session.lifecycle;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.filesystem.Document;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author vince
 */
public class DirtyObjectsTest extends MultiDriverTestClass {

    private Neo4jSession session;
    private SessionFactory sessionFactory;

    @Before
    public void init() throws IOException {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.filesystem");
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
        session.query("MATCH (n) SET n.name='Document'", Utils.map());

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
