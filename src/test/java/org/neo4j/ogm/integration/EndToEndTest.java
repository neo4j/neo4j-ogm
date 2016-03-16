/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.neo4j.ogm.domain.bike.Bike;
import org.neo4j.ogm.domain.bike.Frame;
import org.neo4j.ogm.domain.bike.Saddle;
import org.neo4j.ogm.domain.bike.Wheel;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.session.result.ConnectionException;
import org.neo4j.ogm.session.result.Result;
import org.neo4j.ogm.session.result.ResultProcessingException;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.session.transaction.TransactionException;
import org.neo4j.ogm.session.transaction.TransactionManager;
import org.neo4j.ogm.testutil.Neo4jIntegrationTestRule;

/**
 * @author Michal Bachman
 */
public class EndToEndTest {

    @ClassRule
    public static Neo4jIntegrationTestRule databaseServerRule = new Neo4jIntegrationTestRule();

    private Session session;

    @Before
    public void init() throws IOException {
        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.bike");
        session = sessionFactory.openSession(databaseServerRule.url());
    }

    @After
    public void clearDatabase() {
        databaseServerRule.clearDatabase();
    }

    @Test
    public void canSimpleQueryDatabase() {
        Saddle expected = new Saddle();
        expected.setPrice(29.95);
        expected.setMaterial("Leather");
        Wheel frontWheel = new Wheel();
        Wheel backWheel = new Wheel();
        Bike bike = new Bike();
        bike.setBrand("Huffy");
        bike.setWheels(Arrays.asList(frontWheel, backWheel));
        bike.setSaddle(expected);
        session.save(bike);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("material", "Leather");
        Saddle actual = session.queryForObject(Saddle.class, "MATCH (saddle:Saddle{material: {material}}) RETURN saddle", parameters);

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getMaterial(), actual.getMaterial());

        HashMap<String, Object> parameters2 = new HashMap<>();
        parameters2.put("brand", "Huffy");
        Bike actual2 = session.queryForObject(Bike.class, "MATCH (bike:Bike{brand: {brand}}) RETURN bike", parameters2);

        assertEquals(bike.getId(), actual2.getId());
        assertEquals(bike.getBrand(), actual2.getBrand());

    }


    @Test
    public void canSimpleScalarQueryDatabase() {
        Saddle expected = new Saddle();
        expected.setPrice(29.95);
        expected.setMaterial("Leather");
        session.save(expected);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("material", "Leather");
        int actual = session.queryForObject(Integer.class, "MATCH (saddle:Saddle{material: {material}}) RETURN COUNT(saddle)", parameters);

        assertEquals(1, actual);
    }

    @Test
    public void canComplexQueryDatabase() {
        Saddle saddle = new Saddle();
        saddle.setPrice(29.95);
        saddle.setMaterial("Leather");
        Wheel frontWheel = new Wheel();
        Wheel backWheel = new Wheel();
        Bike bike = new Bike();
        bike.setBrand("Huffy");
        bike.setWheels(Arrays.asList(frontWheel, backWheel));
        bike.setSaddle(saddle);

        session.save(bike);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("brand", "Huffy");
        Bike actual = session.queryForObject(Bike.class, "MATCH (bike:Bike{brand:{brand}})-[rels]-() RETURN bike, COLLECT(DISTINCT rels) as rels", parameters);

        assertEquals(bike.getId(), actual.getId());
        assertEquals(bike.getBrand(), actual.getBrand());
        assertEquals(bike.getWheels().size(), actual.getWheels().size());
        assertNotNull(actual.getSaddle());
    }

    @Test
    public void canComplexExecute() {
        Saddle saddle = new Saddle();
        saddle.setPrice(29.95);
        saddle.setMaterial("Leather");
        Wheel frontWheel = new Wheel();
        Wheel backWheel = new Wheel();
        Bike bike = new Bike();
        bike.setBrand("Huffy");
        bike.setWheels(Arrays.asList(frontWheel, backWheel));
        bike.setSaddle(saddle);

        session.save(bike);

        Saddle newSaddle = new Saddle();
        newSaddle.setPrice(19.95);
        newSaddle.setMaterial("Vinyl");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("brand", "Huffy");
        parameters.put("saddle", newSaddle);
        session.execute("MATCH (bike:Bike{brand:{brand}})-[r]-(s:Saddle) SET s = {saddle}", parameters);

        HashMap<String, Object> parameters2 = new HashMap<>();
        parameters2.put("brand", "Huffy");
        Bike actual = session.queryForObject(Bike.class, "MATCH (bike:Bike{brand:{brand}})-[rels]-() RETURN bike, COLLECT(DISTINCT rels) as rels", parameters2);

        assertEquals(bike.getId(), actual.getId());
        assertEquals(bike.getBrand(), actual.getBrand());
        assertEquals(bike.getWheels().size(), actual.getWheels().size());
        assertEquals(actual.getSaddle().getMaterial(), "Vinyl");
    }

    @Test
    public void canSaveNewObjectTreeToDatabase() {

        Wheel frontWheel = new Wheel();
        Wheel backWheel = new Wheel();
        Bike bike = new Bike();

        bike.setFrame(new Frame());
        bike.setSaddle(new Saddle());
        bike.setWheels(Arrays.asList(frontWheel, backWheel));

        assertNull(frontWheel.getId());
        assertNull(backWheel.getId());
        assertNull(bike.getId());
        assertNull(bike.getFrame().getId());
        assertNull(bike.getSaddle().getId());

        session.save(bike);

        assertNotNull(frontWheel.getId());
        assertNotNull(backWheel.getId());
        assertNotNull(bike.getId());
        assertNotNull(bike.getFrame().getId());
        assertNotNull(bike.getSaddle().getId());

    }

    @Test
    public void shouldCloseTransactionResourcesAfterFailure() {

        Transaction tx = session.beginTransaction();

        for (int i = 0; i < 2; i++)
        {
            try {
                Result r = session.query( String.valueOf( i ), Utils.map() );
                fail ("Should not get here");
            } catch (Exception e) {
                try {
                    tx.rollback();
                } catch ( ResultProcessingException rpe ) {
                    // expected, Neo4j has automatically rolled back the transaction
                } catch ( TransactionException txe) {
                    assertEquals( 1, i );
                    // expected, we have discarded this transaction already
                }
            }
        }
        if (!tx.status().equals( Transaction.Status.ROLLEDBACK )) {
            tx.commit();
        }

        TransactionManager txManager = (( Neo4jSession ) session).transactionManager();

        assertNull( txManager.getCurrentTransaction() );

    }

    @Test
    public void shouldCloseTransactionResourcesAfterSuccess() {

        Transaction tx = session.beginTransaction();

        for (int i = 0; i < 2; i++)
        {
            try {
                Result r = session.query( "MATCH n RETURN n", Utils.map() );
                assertNotNull(r);
            } catch (Exception e) {
                if (!tx.status().equals( Transaction.Status.ROLLEDBACK )) {
                    tx.rollback();
                }
            }
        }
        if (!tx.status().equals( Transaction.Status.ROLLEDBACK )) {
            tx.commit();
        }

        TransactionManager txManager = (( Neo4jSession ) session).transactionManager();

        assertNull(txManager.getCurrentTransaction());

    }

	/**
     * @see Issue 133
     */
    @Test(expected = ConnectionException.class)
    public void shouldThrowConnectionExceptionWhenConnectionIsUnavailable() {
        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.bike");
        session = sessionFactory.openSession("http://invalid@invalid:invalid:8484");
        session.purgeDatabase();
    }

}
