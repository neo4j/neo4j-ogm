package org.neo4j.ogm.mapper.model.satellite;

import org.junit.Test;
import org.neo4j.ogm.mapper.domain.satellites.Program;
import org.neo4j.ogm.mapper.domain.satellites.Satellite;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * This is a full integration test that requires a running neo4j
 * database on localhost, populated with satellite data.
 */
public class SatelliteIntegrationTest {

    // initialise the repository
    private final SessionFactory sessionFactory=new SessionFactory("org.neo4j.ogm.mapper.domain.satellites");
    private final Session session = sessionFactory.openSession("http://localhost:7474/db/data/transaction/commit");

    @Test
    public void loadPrograms() {
        Collection<Program> programs = session.loadAll(Program.class);

        if (!programs.isEmpty()) {
            assertEquals(4, programs.size());
            for (Program program : programs) {

                System.out.println("program:" + program.getName());

                // hydrate the satellites' relationships
                session.loadAll(program.getSatellites());

                for (Satellite satellite : program.getSatellites()) {
                    System.out.println("\tsatellite:" + satellite.getName());
                    System.out.println("\t\tlocation:" + satellite.getLocation().getName());
                    System.out.println("\t\torbit:" + satellite.getOrbit().getName());
                    System.out.println("\t\tprogram: " + satellite.getProgram());

                }
            }
        }
    }

    @Test
    public void loadSatellites() {


        Collection<Satellite> satellites = session.loadAll(Satellite.class);
        if (!satellites.isEmpty()) {
            assertEquals(11, satellites.size());

            for (Satellite satellite : satellites) {

                System.out.println("satellite:" + satellite.getName());
                System.out.println("\tname:" + satellite.getName());
                System.out.println("\tlaunched:" + satellite.getLaunched());
                System.out.println("\tmanned:" + satellite.getManned());

                System.out.println("\tlocation:" + satellite.getLocation().getRef());
                System.out.println("\torbit:" + satellite.getOrbit().getName());
                System.out.println("\tprogram: " + satellite.getProgram());
                assertEquals(satellite.getRef(), satellite.getName());

            }
        } else {
            System.out.println("Satellite Integration Test not run: Is there a database?");
        }
    }


}
