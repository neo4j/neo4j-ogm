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
package org.neo4j.ogm.persistence.transaction;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.gh868.Actor;
import org.neo4j.ogm.domain.gh868.Movie;
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.domain.music.Artist;
import org.neo4j.ogm.domain.music.Recording;
import org.neo4j.ogm.domain.music.Studio;
import org.neo4j.ogm.exception.TransactionException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author Luanne Misquitta
 */
public class TransactionTest extends TestContainersTestBase {

    private Session session;

    @Before
    public void init() throws IOException {
        SessionFactory sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.music", "org.neo4j.ogm.domain.gh868");
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @After
    public void clearDatabase() {
        session.purgeDatabase();
    }

    @Test
    public void shouldNotCommitWhenTransactionIsManaged() {
        Transaction tx = session.beginTransaction();
        Studio emi = new Studio("EMI Studios, London");

        Artist theBeatles = new Artist("The Beatles");
        Album please = new Album("Please Please Me");
        Recording pleaseRecording = new Recording(please, emi, 1963);
        please.setRecording(pleaseRecording);
        theBeatles.getAlbums().add(please);
        please.setArtist(theBeatles);
        session.save(theBeatles);

        tx.rollback(); //the previous saves shouldn't have been committed

        assertThat(session.countEntitiesOfType(Artist.class)).isEqualTo(0);
    }

    @Test // GH-126
    public void shouldBeAbleToRetrySaveOnTransactionRollback() {

        Transaction tx = session.beginTransaction();

        Studio emi = new Studio("EMI Studios, London");
        Artist theBeatles = new Artist("The Beatles");
        Album please = new Album("Please Please Me");
        Recording pleaseRecording = new Recording(please, emi, 1963);

        please.setRecording(pleaseRecording);
        theBeatles.getAlbums().add(please);
        please.setArtist(theBeatles);
        session.save(theBeatles);

        tx.rollback();

        session.save(theBeatles);

        session.clear();

        theBeatles = session.loadAll(Artist.class, -1).iterator().next();
        assertThat(theBeatles.getName()).isEqualTo("The Beatles");
        assertThat(theBeatles.getAlbums()).hasSize(1);
        assertThat(theBeatles.getAlbums().iterator().next().getName()).isEqualTo("Please Please Me");
        assertThat(theBeatles.getAlbums().iterator().next().getRecording().getStudio().getName())
            .isEqualTo("EMI Studios, London");
    }

    @Test
    public void shouldNotBeReadOnlyByDefault() {

        try (Transaction tx = session.beginTransaction()) {
            assertThat(tx.isReadOnly()).isFalse();
        }
    }

    @Test
    public void shouldBeAbleToCreateReadOnlyTransaction() {

        try (Transaction tx = session.beginTransaction(Transaction.Type.READ_ONLY)) {
            assertThat(tx.isReadOnly()).isTrue();
        }
    }

    @Test
    public void shouldNotBeAbleToExtendAReadTransactionWithAReadWriteInnerTransaction() {

        try (
            Transaction tx1 = session.beginTransaction(Transaction.Type.READ_ONLY);
            Transaction tx2 = session.beginTransaction(Transaction.Type.READ_WRITE)) {
            fail("Should not have allowed transaction extension of different type");
        } catch (TransactionException tme) {
            assertThat(tme.getLocalizedMessage())
                .isEqualTo("Incompatible transaction type specified: must be 'READ_ONLY'");
        }
    }

    @Test
    public void shouldNotBeAbleToExtendAReadWriteTransactionWithAReadOnlyInnerTransaction() {

        try (
            Transaction tx1 = session.beginTransaction(Transaction.Type.READ_WRITE);
            Transaction tx2 = session.beginTransaction(Transaction.Type.READ_ONLY)) {
            fail("Should not have allowed transaction extension of different type");
        } catch (TransactionException tme) {
            assertThat(tme.getLocalizedMessage())
                .isEqualTo("Incompatible transaction type specified: must be 'READ_WRITE'");
        }
    }

    @Test
    public void shouldAutomaticallyExtendAReadOnlyTransactionWithAReadOnlyExtension() {

        try (
            Transaction tx1 = session.beginTransaction(Transaction.Type.READ_ONLY);
            Transaction tx2 = session.beginTransaction()) {
            assertThat(tx2.isReadOnly()).isTrue();
        }
    }

    @Test
    public void shouldAutomaticallyExtendAReadWriteTransactionWithAReadWriteExtension() {

        try (Transaction tx1 = session.beginTransaction(Transaction.Type.READ_WRITE);
            Transaction tx2 = session.beginTransaction()) {
            assertThat(tx2.isReadOnly()).isFalse();
        }
    }

    @Test
    public void defaultTransactionShouldWorkAfterManagedTransaction() {
        Transaction tx = session.beginTransaction();
        Studio emi = new Studio("EMI Studios, London");
        session.save(emi);

        tx.commit();
        tx.close();

        session.purgeDatabase();
    }

    @Test
    public void defaultTransactionShouldWorkAfterDefaultTransaction() {
        Studio emi = new Studio("EMI Studios, London");
        session.save(emi);

        session.purgeDatabase();
    }

    @Test // GH-868
    public void shouldNotLoadAlreadyLoadedRelationshipEntityAgain() {

        Movie movie = new Movie("Lord of the rings");
        Actor actor = new Actor("Christopher Lee");
        actor.addPlayedIn("Saruman", movie);

        session.save(actor);
        assertThat(actor).isNotNull();
        assertThat(actor.getId()).isNotNull();
        assertThat(actor.getPlayedIn()).hasSize(1);

        runReadQuery(actor);
        assertThat(actor.getPlayedIn()).hasSize(1);

        // this second save detaches an entity or relationshipEntity
        // we'll get a bad result on next query
        actor.setName("name changed");
        session.save(actor);
        assertThat(actor).isNotNull();
        assertThat(actor.getId()).isNotNull();
        assertThat(actor.getPlayedIn()).hasSize(1);

        runReadQuery(actor);
        // fails, we return childs = 2
        assertThat(actor.getPlayedIn()).hasSize(1);
    }

    private void runReadQuery(Actor entity) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", entity.getId());
        session.query("MATCH /*+ OGM_READ_ONLY */ path = (a:Actor)-[:PLAYED_IN*0..]->()"
            + " WHERE id(a) = $id"
            + " RETURN nodes(path), relationships(path)", params);
    }
}
