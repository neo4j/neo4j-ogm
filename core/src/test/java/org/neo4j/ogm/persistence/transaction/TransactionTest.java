/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Assert;
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

import java.io.IOException;

/**
 * @author Luanne Misquitta
 */
public class TransactionTest extends MultiDriverTestClass {
    private Session session;

    @Before
    public void init() throws IOException {
        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.music");
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

        assertEquals(0, session.countEntitiesOfType(Artist.class));
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
        assertEquals("The Beatles", theBeatles.getName());
        assertEquals(1, theBeatles.getAlbums().size());
        assertEquals("Please Please Me", theBeatles.getAlbums().iterator().next().getName());
        assertEquals("EMI Studios, London", theBeatles.getAlbums().iterator().next().getRecording().getStudio().getName());
    }

    @Test
    public void shouldNotBeReadOnlyByDefault() {

        try (Transaction tx = session.beginTransaction()) {
            Assert.assertFalse(tx.isReadOnly());
        }
    }

    @Test
    public void shouldBeAbleToCreateReadOnlyTransaction() {

        try (Transaction tx = session.beginTransaction(Transaction.Type.READ_ONLY)) {
            Assert.assertTrue(tx.isReadOnly());
        }
    }

    @Test
    public void shouldNotBeAbleToExtendAReadTransactionWithAReadWriteInnerTransaction() {

        try (
                Transaction tx1 = session.beginTransaction(Transaction.Type.READ_ONLY);
                Transaction tx2 = session.beginTransaction(Transaction.Type.READ_WRITE)) {
            fail("Should not have allowed transaction extension of different type");
        } catch (TransactionException tme) {
            Assert.assertEquals("Incompatible transaction type specified: must be 'READ_ONLY'", tme.getLocalizedMessage());
        }
    }

    @Test
    public void shouldNotBeAbleToExtendAReadWriteTransactionWithAReadOnlyInnerTransaction() {

        try (
                Transaction tx1 = session.beginTransaction(Transaction.Type.READ_WRITE);
                Transaction tx2 = session.beginTransaction(Transaction.Type.READ_ONLY)) {
            fail("Should not have allowed transaction extension of different type");
        } catch (TransactionException tme) {
            Assert.assertEquals("Incompatible transaction type specified: must be 'READ_WRITE'", tme.getLocalizedMessage());
        }
    }

    @Test
    public void shouldAutomaticallyExtendAReadOnlyTransactionWithAReadOnlyExtension() {

        try (
                Transaction tx1 = session.beginTransaction(Transaction.Type.READ_ONLY);
                Transaction tx2 = session.beginTransaction()) {
            Assert.assertTrue(tx2.isReadOnly());
        }
    }

    @Test
    public void shouldAutomaticallyExtendAReadWriteTransactionWithAReadWriteExtension() {

        try (Transaction tx1 = session.beginTransaction(Transaction.Type.READ_WRITE);
             Transaction tx2 = session.beginTransaction()) {
            Assert.assertFalse(tx2.isReadOnly());
        }
    }

}
