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

package org.neo4j.ogm.persistence.transaction;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.domain.music.Artist;
import org.neo4j.ogm.domain.music.Recording;
import org.neo4j.ogm.domain.music.Studio;
import org.neo4j.ogm.exception.TransactionException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author Luanne Misquitta
 */
public class TransactionTest extends MultiDriverTestClass {

    private Session session;

    @Before
    public void init() throws IOException {
        SessionFactory sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.music");
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

    /**
     * @see Issue 126
     */
    @Test
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
        assertThat(theBeatles.getAlbums().iterator().next().getRecording().getStudio().getName()).isEqualTo("EMI Studios, London");
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
            assertThat(tme.getLocalizedMessage()).isEqualTo("Incompatible transaction type specified: must be 'READ_ONLY'");
        }
    }

    @Test
    public void shouldNotBeAbleToExtendAReadWriteTransactionWithAReadOnlyInnerTransaction() {

        try (
                Transaction tx1 = session.beginTransaction(Transaction.Type.READ_WRITE);
                Transaction tx2 = session.beginTransaction(Transaction.Type.READ_ONLY)) {
            fail("Should not have allowed transaction extension of different type");
        } catch (TransactionException tme) {
            assertThat(tme.getLocalizedMessage()).isEqualTo("Incompatible transaction type specified: must be 'READ_WRITE'");
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
}
