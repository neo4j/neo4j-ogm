package org.neo4j.ogm.testutil;

import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;

/**
 * Base class for all kinds of Neo4j integration tests.
 * <p/>
 * Creates an {@link org.neo4j.test.ImpermanentGraphDatabase} (by default) at the beginning of each test and allows
 * subclasses to populate it by overriding the {@link #populateDatabase(org.neo4j.graphdb.GraphDatabaseService)} method,
 * which is guaranteed to run in a transaction.
 * <p/>
 * Shuts the database down at the end of each test.
 */
public abstract class DatabaseIntegrationTest {

    private GraphDatabaseService database;

    @Before
    public void setUp() throws Exception {
        database = createDatabase();
    }

    @After
    public void tearDown() throws Exception {
        database.shutdown();
    }

    /**
     * Instantiate a database. By default this will be {@link org.neo4j.test.ImpermanentGraphDatabase}.
     *
     * @return new database.
     */
    protected GraphDatabaseService createDatabase() {
        final GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                database.shutdown();
            }
        });
        return database;
    }

    /**
     * Get the database instantiated for this test.
     *
     * @return database.
     */
    protected GraphDatabaseService getDatabase() {
        return database;
    }

}
