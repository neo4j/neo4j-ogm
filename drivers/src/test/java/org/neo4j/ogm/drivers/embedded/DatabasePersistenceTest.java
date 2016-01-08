package org.neo4j.ogm.drivers.embedded;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author vince
 */
public class DatabasePersistenceTest {

    @Before
    public void setUp() throws Exception {
        File f = new File("/tmp/graph.db");
        if (f.exists()) {
            if (f.isDirectory()) {
                FileUtils.deleteDirectory(f);
            } else {
                f.delete();
            }
        }
        assertFalse(f.exists());
    }


    @Test
    public void shouldCloseDatabaseDownCorrectly() {

        GraphDatabaseService graphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/graph.db");
        graphDatabaseService.shutdown();

        File f = new File("/tmp/graph.db/neostore.nodestore.db");
        assertTrue(f.exists());
        assertNotNull(f.length());
        assertTrue(f.length() > 0);

    }


}
