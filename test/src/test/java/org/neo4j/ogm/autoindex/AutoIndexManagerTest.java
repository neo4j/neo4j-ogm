package org.neo4j.ogm.autoindex;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.config.AutoIndexMode;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.DriverManager;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * Make sure this tests works across all drivers supporting Neo4j 3.x and above.
 *
 * @author Mark Angrish
 */
public class AutoIndexManagerTest extends MultiDriverTestClass {

    private MetaData metaData = new MetaData("org.neo4j.ogm.domain.forum");

    private static final String CREATE_LOGIN_CONSTRAINT_CYPHER = "CREATE CONSTRAINT ON ( login:Login ) ASSERT login.userName IS UNIQUE";
    private static final String DROP_LOGIN_CONSTRAINT_CYPHER = "DROP CONSTRAINT ON (login:Login) ASSERT login.userName IS UNIQUE";

    @Test
    public void shouldPreserveConfiguration() {
        assertEquals(AutoIndexMode.VALIDATE, getBaseConfiguration().autoIndex("validate").build().getAutoIndex());
    }

    @Test
    public void testIndexesAreSuccessfullyValidated() {

        createLoginConstraint();

        final Configuration configuration = getBaseConfiguration().autoIndex("validate").build();
        AutoIndexManager indexManager = new AutoIndexManager(metaData, driver, configuration);
        assertEquals(AutoIndexMode.VALIDATE, configuration.getAutoIndex());
        assertEquals(1, indexManager.getIndexes().size());
        indexManager.build();

        dropLoginConstraint();
    }

    @Test(expected = MissingIndexException.class)
    public void testIndexesAreFailValidation() {
        final Configuration configuration = getBaseConfiguration().autoIndex("validate").build();
        AutoIndexManager indexManager = new AutoIndexManager(metaData, driver, configuration);
        assertEquals(AutoIndexMode.VALIDATE, configuration.getAutoIndex());
        indexManager.build();
    }

    @Test
    public void testIndexDumpMatchesDatabaseIndexes() throws IOException {

        createLoginConstraint();
        File file = new File("./test.cql");

        try {
            final Configuration configuration = getBaseConfiguration().autoIndex("dump").generatedIndexesOutputDir(".").generatedIndexesOutputFilename("test.cql").build();
            AutoIndexManager indexManager = new AutoIndexManager(metaData, driver, configuration);
            assertEquals(AutoIndexMode.DUMP, configuration.getAutoIndex());
            assertEquals(1, indexManager.getIndexes().size());
            indexManager.build();
            assertTrue(file.exists());
            assertTrue(file.length() > 0);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String actual = reader.readLine();
            assertEquals(CREATE_LOGIN_CONSTRAINT_CYPHER, actual);
            reader.close();
        } finally {
            file.delete();
        }

        dropLoginConstraint();
    }

    @Test
    public void testIndexesAreSuccessfullyAsserted() {

        createLoginConstraint();

        final Configuration configuration = getBaseConfiguration().autoIndex("assert").build();
        AutoIndexManager indexManager = new AutoIndexManager(metaData, driver, configuration);
        assertEquals(AutoIndexMode.ASSERT, configuration.getAutoIndex());
        assertEquals(1, indexManager.getIndexes().size());
        indexManager.build();

        dropLoginConstraint();
    }

    private void createLoginConstraint() {
        getGraphDatabaseService().execute(CREATE_LOGIN_CONSTRAINT_CYPHER);
    }

    private void dropLoginConstraint() {
        getGraphDatabaseService().execute(DROP_LOGIN_CONSTRAINT_CYPHER);
    }
}
