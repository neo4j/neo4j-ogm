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

package org.neo4j.ogm.integration.satellite;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.domain.satellites.Program;
import org.neo4j.ogm.domain.satellites.Satellite;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.testutil.Neo4jIntegrationTestRule;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * This is a full integration test that requires a running neo4j
 * database on localhost.
 *
 * @author Vince Bickers
 */
public class SatelliteIntegrationTest
{
    @ClassRule
    public static Neo4jIntegrationTestRule neo4jRule = new Neo4jIntegrationTestRule();

    private static Session session;


    @BeforeClass
    public static void init() throws IOException {
        session = new SessionFactory("org.neo4j.ogm.domain.satellites").openSession(neo4jRule.url());
        importSatellites();
    }

    private static void importSatellites() {
        neo4jRule.loadClasspathCypherScriptFile("org/neo4j/ogm/cql/satellites.cql");
    }

    @Test
    public void shouldLoadPrograms() {

        Collection<Program> programs = session.loadAll(Program.class);

        if (!programs.isEmpty()) {
            assertEquals(4, programs.size());
            for (Program program : programs) {

                System.out.println("program:" + program.getName());

                for (Satellite satellite : program.getSatellites()) {
                    // 1-side of many->1 is auto-hydrated
                    assertNull(satellite.getProgram());

                    System.out.println("\tsatellite:" + satellite.getName());
                }
            }
        } else {
            fail("Satellite Integration Tests not run: Is there a database?");
        }
    }

    @Test
    public void shouldLoadSatellites() {


        Collection<Satellite> satellites = session.loadAll(Satellite.class);
        if (!satellites.isEmpty()) {
            assertEquals(11, satellites.size());

            for (Satellite satellite : satellites) {

                System.out.println("satellite:" + satellite.getName());
                System.out.println("\tname:" + satellite.getName());
                System.out.println("\tlaunched:" + satellite.getLaunched());
                System.out.println("\tmanned:" + satellite.getManned());
                System.out.println("\tupdated:" + satellite.getUpdated());

                System.out.println("\tlocation:" + satellite.getLocation().getRef());
                System.out.println("\torbit:" + satellite.getOrbit().getName());
                System.out.println("\tprogram: " + satellite.getProgram());
                assertEquals(satellite.getRef(), satellite.getName());

            }
        } else {
            fail("Satellite Integration Tests not run: Is there a database?");
        }
    }

    @Test
    public void shouldUpdateSatellite() {

        Collection<Satellite> satellites = session.loadAll(Satellite.class);

        if (!satellites.isEmpty()) {

            Satellite satellite = satellites.iterator().next();
            Long id = satellite.getId();

            satellite.setName("Updated satellite");
            Date date = new Date();
            satellite.setUpdated(date);

            session.save(satellite);

            Satellite updatedSatellite = session.load(Satellite.class, id);
            assertEquals("Updated satellite", updatedSatellite.getName());
            assertEquals(date, updatedSatellite.getUpdated());


        } else {
            fail("Satellite Integration Tests not run: Is there a database?");
        }
    }

    @Test
    public void shouldUseLongTransaction() {


        try (Transaction tx = session.beginTransaction()) {

            // load all
            Collection<Satellite> satellites = session.loadAll(Satellite.class);
            assertEquals(11, satellites.size());

            Satellite satellite = satellites.iterator().next();
            Long id = satellite.getId();
            satellite.setName("Updated satellite");

            // update
            session.save(satellite);
            System.out.println("saved");

            // refetch
            Satellite updatedSatellite = session.load(Satellite.class, id);
            assertEquals("Updated satellite", updatedSatellite.getName());

        }
    }


    @Test
    public void shouldRollbackLongTransaction() {

        try (Transaction tx = session.beginTransaction()) {

            // load all
            Collection<Satellite> satellites = session.loadAll(Satellite.class);
            assertEquals(11, satellites.size());

            Satellite satellite = satellites.iterator().next();
            Long id = satellite.getId();
            String name = satellite.getName();
            satellite.setName("Updated satellite");

            // update
            session.save(satellite);

            // refetch
            Satellite updatedSatellite = session.load(Satellite.class, id);
            assertEquals("Updated satellite", updatedSatellite.getName());

            tx.rollback();

            //Transaction tx2 = session.beginTransaction();
            // fetch - after rollback should not be changed
            // note, that because we aren't starting a new tx, we will be given an autocommit one.
            updatedSatellite = session.load(Satellite.class, id);
            assertEquals(name, updatedSatellite.getName());

            //tx2.close();
        }
    }

    @Test
    public void shouldCommitLongTransaction() {

        try (Transaction tx = session.beginTransaction()) {

            // load all
            Collection<Satellite> satellites = session.loadAll(Satellite.class);
            assertEquals(11, satellites.size());

            Satellite satellite = satellites.iterator().next();
            Long id = satellite.getId();
            satellite.setName("Updated satellite");

            // update
            session.save(satellite);

            // refetch
            Satellite updatedSatellite = session.load(Satellite.class, id);
            assertEquals("Updated satellite", updatedSatellite.getName());

            tx.commit();

            // fetch - after commit should be changed
            // note, that because we aren't starting a new tx, we will be given an autocommit one.
            updatedSatellite = session.load(Satellite.class, id);
            assertEquals("Updated satellite", updatedSatellite.getName());

        }
    }

    @Test
    public void shouldReturnSatellitesSortedByRefAsc() {

        Collection<Satellite> satellites = session.loadAll(Satellite.class, new SortOrder().add("ref"));

        Iterator<Satellite> iter = satellites.iterator();
        Satellite first = iter.next();
        System.out.println(first.getId() + ":" + first.getRef());
        while (iter.hasNext()) {
            Satellite next = iter.next();
            System.out.println(next.getId() + ":" + next.getRef());
            assertTrue(first.getRef().compareTo(next.getRef()) < 0);
            first = next;
        }
    }

    @Test
    public void shouldReturnProgramsSortedByRefDesc() {

        Collection<Program> objects = session.loadAll(Program.class, new SortOrder().add(SortOrder.Direction.DESC, "ref"));

        Iterator<Program> iter = objects.iterator();
        Program first = iter.next();
        System.out.println(first.getId() + ":" + first.getRef());
        while (iter.hasNext()) {
            Program next = iter.next();
            System.out.println(next.getId() + ":" + next.getRef());
            assertTrue(first.getRef().compareTo(next.getRef()) > 0);
            first = next;
        }
    }

    @Test
    public void shouldLoadActiveSatellitesByPropertySorted() {

        Collection<Satellite> satellites = session.loadAll(Satellite.class, new Filter("manned", "Y"), new SortOrder().add("ref"));

        Iterator<Satellite> iter = satellites.iterator();
        Satellite first = iter.next();
        System.out.println(first.getId() + ":" + first.getRef());
        while (iter.hasNext()) {
            Satellite next = iter.next();
            System.out.println(next.getId() + ":" + next.getRef());
            assertTrue(first.getRef().compareTo(next.getRef()) < 0);
            first = next;
        }

    }
}
