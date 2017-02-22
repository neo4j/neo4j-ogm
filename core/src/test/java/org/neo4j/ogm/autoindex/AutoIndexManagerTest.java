package org.neo4j.ogm.autoindex;

import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.Components;
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


	@After
	public void cleanUp() {
		Components.getConfiguration().setAutoIndex("none");
	}

	@Test
	public void shouldPreserveConfiguration() {
		Configuration configuration = Components.getConfiguration();
		configuration.setAutoIndex("validate");
		Components.configure(configuration);
		assertEquals(AutoIndexMode.VALIDATE.getName(), Components.getConfiguration().getAutoIndex());
	}

	@Test
	public void shouldBeAbleToCreateAndDropConstraintsOnSameDatabaseInstance() {
		createLoginConstraint();
		dropLoginConstraint();
	}

	@Test
	public void testIndexesAreSuccessfullyValidated() {

		createLoginConstraint();

		Components.getConfiguration().setAutoIndex("validate");
		AutoIndexManager indexManager = new AutoIndexManager(metaData, Components.driver());
		assertEquals(AutoIndexMode.VALIDATE.getName(), Components.getConfiguration().getAutoIndex());
		assertEquals(1, indexManager.getIndexes().size());
		indexManager.build();

		dropLoginConstraint();
	}

	@Test(expected = MissingIndexException.class)
	public void testIndexesAreFailValidation() {
		Components.getConfiguration().setAutoIndex("validate");
		AutoIndexManager indexManager = new AutoIndexManager(metaData, Components.driver());
		assertEquals(AutoIndexMode.VALIDATE.getName(), Components.getConfiguration().getAutoIndex());
		indexManager.build();
	}

	@Test
	public void testIndexDumpMatchesDatabaseIndexes() throws IOException {

		createLoginConstraint();

		Components.getConfiguration().setAutoIndex("dump");
		Components.getConfiguration().setDumpDir(".");
		Components.getConfiguration().setDumpFilename("test.cql");

		File file = new File("./test.cql");

		try {
			AutoIndexManager indexManager = new AutoIndexManager(metaData, Components.driver());
			assertEquals(AutoIndexMode.DUMP.getName(), Components.getConfiguration().getAutoIndex());
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

		Components.getConfiguration().setAutoIndex("assert");

		AutoIndexManager indexManager = new AutoIndexManager(metaData, Components.driver());
		assertEquals(AutoIndexMode.ASSERT.getName(), Components.getConfiguration().getAutoIndex());
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
