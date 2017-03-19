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

package org.neo4j.ogm.persistence.examples.satellite;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.domain.satellites.Program;
import org.neo4j.ogm.domain.satellites.Satellite;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.testutil.TestUtils;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author Vince Bickers
 */
public class SatelliteIntegrationTest extends MultiDriverTestClass {

    private static Session session;

    @BeforeClass
    public static void init() throws IOException {
        session = new SessionFactory(baseConfiguration, "org.neo4j.ogm.domain.satellites").openSession();
        importSatellites();
    }

    private static void importSatellites() {
        session.query(TestUtils.readCQLFile("org/neo4j/ogm/cql/satellites.cql").toString(), Utils.map());
    }

    @Test
    public void shouldLoadPrograms() {

        Collection<Program> programs = session.loadAll(Program.class);

        if (!programs.isEmpty()) {
            assertEquals(4, programs.size());
            for (Program program : programs) {
                for (Satellite satellite : program.getSatellites()) {
                    // 1-side of many->1 is auto-hydrated
                    assertNull(satellite.getProgram());
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

            // refetch
            Satellite updatedSatellite = session.load(Satellite.class, id);
            assertEquals("Updated satellite", updatedSatellite.getName());
        }  // transaction will be rolled back
    }


    @Test
    public void shouldRollbackLongTransaction() {

        Long id;
        String name;

        try (Transaction tx = session.beginTransaction()) {

            // load all
            Collection<Satellite> satellites = session.loadAll(Satellite.class);
            assertEquals(11, satellites.size());

            Satellite satellite = satellites.iterator().next();
            id = satellite.getId();
            name = satellite.getName();
            satellite.setName("Updated satellite");

            // update
            session.save(satellite);

            session.clear();

            // refetch
            Satellite updatedSatellite = session.load(Satellite.class, id);
            assertEquals("Updated satellite", updatedSatellite.getName());

            tx.rollback();
        }
        session.clear();

        // fetch - after rollback should not be changed
        // note, that because we aren't starting a new tx, we will be given an autocommit one.
        Satellite reloadedSatellite = session.load(Satellite.class, id);
        assertEquals(name, reloadedSatellite.getName());
    }

    @Test
    public void shouldRollbackClosedAndUnCommittedTransaction() {

        Long id;
        String name;

        try (Transaction tx = session.beginTransaction()) {

            // load all
            Collection<Satellite> satellites = session.loadAll(Satellite.class);
            assertEquals(11, satellites.size());

            Satellite satellite = satellites.iterator().next();
            id = satellite.getId();
            name = satellite.getName();
            satellite.setName("Updated satellite");

            // update
            session.save(satellite);

            session.clear();
            // refetch
            Satellite updatedSatellite = session.load(Satellite.class, id);
            assertEquals("Updated satellite", updatedSatellite.getName());
        }
        session.clear();

        // fetch - after rollback should not be changed
        // note, that because we aren't starting a new tx, we will be given an autocommit one.
        Satellite reloadedSatellite = session.load(Satellite.class, id);
        assertEquals(name, reloadedSatellite.getName());
    }

    @Test
    public void shouldCommitLongTransaction() {

        Long id;

        try (Transaction tx = session.beginTransaction()) {

            // load all
            Collection<Satellite> satellites = session.loadAll(Satellite.class);
            assertEquals(11, satellites.size());

            Satellite satellite = satellites.iterator().next();
            id = satellite.getId();
            satellite.setName("Updated satellite");

            // update
            session.save(satellite);

            session.clear();
            // refetch
            Satellite updatedSatellite = session.load(Satellite.class, id);
            assertEquals("Updated satellite", updatedSatellite.getName());

            tx.commit();
        }

        session.clear();

        // fetch - after commit should be changed
        // note, that because we aren't starting a new tx, we will be given an autocommit one.
        Satellite reloadedSatellite = session.load(Satellite.class, id);
        assertEquals("Updated satellite", reloadedSatellite.getName());
    }

    @Test
    public void shouldReturnSatellitesSortedByRefAsc() {

        Collection<Satellite> satellites = session.loadAll(Satellite.class, new SortOrder().add("ref"));

        Iterator<Satellite> iter = satellites.iterator();
        Satellite first = iter.next();
        while (iter.hasNext()) {
            Satellite next = iter.next();
            assertTrue(first.getRef().compareTo(next.getRef()) < 0);
            first = next;
        }
    }

    @Test
    public void shouldReturnProgramsSortedByRefDesc() {

        Collection<Program> objects = session.loadAll(Program.class, new SortOrder().add(SortOrder.Direction.DESC, "ref"));

        Iterator<Program> iter = objects.iterator();
        Program first = iter.next();
        while (iter.hasNext()) {
            Program next = iter.next();
            assertTrue(first.getRef().compareTo(next.getRef()) > 0);
            first = next;
        }
    }

    @Test
    public void shouldLoadActiveSatellitesByPropertySorted() {

        Collection<Satellite> satellites = session.loadAll(Satellite.class, new Filter("manned", ComparisonOperator.EQUALS, "Y"), new SortOrder().add("ref"));

        Iterator<Satellite> iter = satellites.iterator();
        Satellite first = iter.next();
        while (iter.hasNext()) {
            Satellite next = iter.next();
            assertTrue(first.getRef().compareTo(next.getRef()) < 0);
            first = next;
        }
    }
}
