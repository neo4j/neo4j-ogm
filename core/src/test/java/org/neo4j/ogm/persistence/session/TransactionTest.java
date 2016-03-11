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

package org.neo4j.ogm.persistence.session;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.domain.music.Artist;
import org.neo4j.ogm.domain.music.Recording;
import org.neo4j.ogm.domain.music.Studio;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.transaction.Transaction;

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
    public void shouldHandleDeadlock() throws InterruptedException {

        Artist theBeatles = new Artist("The Beatles");
        session.save(theBeatles);

        int numThreads = Runtime.getRuntime().availableProcessors() + 1; // more threads than available connections
        long id = theBeatles.getId();

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        String query = "MATCH (n) where id(n) = " + id + " set n.name = 'Updated'";

        for (int i = 0; i < numThreads; i++) {
            executor.submit(new QueryRunner(latch, query));
        }
        latch.await(); // pause until the count reaches 0

        // force termination of all threads
        executor.shutdownNow();

    }

    class QueryRunner implements Runnable {

        private final CountDownLatch latch;
        private final String query;

        public QueryRunner( CountDownLatch latch, String query ) {
            this.query = query;
            this.latch = latch;
        }

        @Override
        public void run() {
            Transaction tx = session.beginTransaction();
            try
            {
                session.query( query, Utils.map() );
                System.out.println( Thread.currentThread().getId() + ": updated" );
                tx.commit();
                System.out.println( Thread.currentThread().getId() + ": committed" );
            } catch (Exception e)
            {
                System.out.println( Thread.currentThread().getId() + ": failed: " + e.getLocalizedMessage());
                tx.rollback();
            }

            finally {
                System.out.println( Thread.currentThread().getId() + ": finished" );
                latch.countDown();
            }

            while(!Thread.currentThread().isInterrupted()){
                try{
                    Thread.sleep(100);
                } catch(InterruptedException e){
                    System.out.println( Thread.currentThread().getId() + ": interrupted" );
                    Thread.currentThread().interrupt(); //propagate interrupt
                }
            }

            System.out.println( Thread.currentThread().getId() + ": stopping" );


        }
    }

}
