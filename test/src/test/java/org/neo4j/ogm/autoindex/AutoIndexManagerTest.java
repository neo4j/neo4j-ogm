package org.neo4j.ogm.autoindex;

import static org.assertj.core.api.Assertions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.neo4j.ogm.config.AutoIndexMode;
import org.neo4j.ogm.config.Configuration;
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
    public void shouldPreserveNoneConfiguration() {
        Configuration configuration = getBaseConfiguration().autoIndex("none").build();
        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.NONE);
    }

    @Test
    public void shouldPreserveAssertConfiguration() {
        Configuration configuration = getBaseConfiguration().autoIndex("assert").build();
        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.ASSERT);
    }

    @Test
    public void shouldPreserveValidateConfiguration() {
        Configuration configuration = getBaseConfiguration().autoIndex("validate").build();
        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.VALIDATE);
    }

    @Test
    public void shouldPreserveDumpConfiguration() {
        Configuration configuration = getBaseConfiguration().autoIndex("dump").build();
        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.DUMP);
    }

    @Test
    public void testIndexesAreSuccessfullyValidated() {

        createLoginConstraint();

        Configuration configuration = getBaseConfiguration().autoIndex("validate").build();
        AutoIndexManager indexManager = new AutoIndexManager(metaData, driver, configuration);
        assertThat(indexManager.getIndexes()).hasSize(1);
        indexManager.build();

        dropLoginConstraint();
    }

    @Test(expected = MissingIndexException.class)
    public void testIndexesAreFailValidation() {
        Configuration configuration = getBaseConfiguration().autoIndex("validate").build();
        AutoIndexManager indexManager = new AutoIndexManager(metaData, driver, configuration);
        indexManager.build();
    }

    @Test
    public void testIndexDumpMatchesDatabaseIndexes() throws IOException {

        createLoginConstraint();
        File file = new File("./test.cql");

        try {
            Configuration configuration = getBaseConfiguration()
                    .autoIndex("dump")
                    .generatedIndexesOutputDir(".")
                    .generatedIndexesOutputFilename("test.cql")
                    .build();

            AutoIndexManager indexManager = new AutoIndexManager(metaData, driver, configuration);
            assertThat(indexManager.getIndexes()).hasSize(1);
            indexManager.build();

            assertThat(file.exists()).isTrue();
            try (InputStream is = new FileInputStream("./test.cql")) {
                String actual = IOUtils.toString(is);
                assertThat(actual).isEqualToIgnoringWhitespace(CREATE_LOGIN_CONSTRAINT_CYPHER);
            }

        } finally {
            file.delete();
        }

        dropLoginConstraint();
    }

    @Test
    public void testIndexesAreSuccessfullyAsserted() {

        createLoginConstraint();

        Configuration configuration = getBaseConfiguration().autoIndex("assert").build();
        AutoIndexManager indexManager = new AutoIndexManager(metaData, driver, configuration);

        assertThat(indexManager.getIndexes()).hasSize(1);
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
