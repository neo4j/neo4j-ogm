package org.neo4j.ogm.integration.satellite;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.satellites.Program;
import org.neo4j.ogm.domain.satellites.Satellite;
import org.neo4j.ogm.integration.IntegrationTest;
import org.neo4j.ogm.session.SessionFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * This is a full integration test that requires a running neo4j
 * database on localhost.
 */
public class SatelliteIntegrationTest extends IntegrationTest {

    @BeforeClass
    public static void init() throws IOException {
        setUp();
        session = new SessionFactory("org.neo4j.ogm.domain.satellites").openSession("http://localhost:" + neoPort);
        importSatellites();

    }

    @Test
    public void loadPrograms() {
        Collection<Program> programs = session.loadAll(Program.class);

        if (!programs.isEmpty()) {
            assertEquals(4, programs.size());
            for (Program program : programs) {
                System.out.println("program:" + program.getName());
                for (Satellite satellite : program.getSatellites()) {
                    // 1-side of many->1 is auto-hydrated
                    assertEquals(satellite.getProgram(), program);

                    System.out.println("\tsatellite:" + satellite.getName());
                    System.out.println("\t\tprogram:" + satellite.getProgram().getName());
                    System.out.println("\t\t\tnum-satellites:" + satellite.getProgram().getSatellites().size());

                }
            }
        } else {
            fail("Satellite Integration Tests not run: Is there a database?");
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
                System.out.println("\tprogram: " + satellite.getProgram().getName());
                assertEquals(satellite.getRef(), satellite.getName());

            }
        } else {
            fail("Satellite Integration Tests not run: Is there a database?");
        }
    }

    @Test
    public void updateSatellite() {

        Collection<Satellite> satellites = session.loadAll(Satellite.class);

        if (!satellites.isEmpty()) {

            Satellite satellite = satellites.iterator().next();
            Long id = satellite.getId();
            satellite.setName("Updated satellite");

            session.save(satellite);

            Satellite updatedSatellite = session.load(Satellite.class, id);
            assertEquals("Updated satellite", updatedSatellite.getName());


        } else {
            fail("Satellite Integration Tests not run: Is there a database?");
        }
    }

    private static void importSatellites() {
        session.execute(load("org/neo4j/ogm/cql/satellites.cql"));
    }

    private static String load(String cqlFile) {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(cqlFile)));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(" ");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

}
