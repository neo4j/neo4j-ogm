package org.neo4j.ogm.index;

import static org.junit.Assert.*;
import static org.junit.Assume.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import junit.extensions.PA;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.model.RestModel;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.request.*;
import org.neo4j.ogm.request.DefaultRequest;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.session.request.*;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * Make sure this tests works across all drivers supporting Neo4j 3.x and above.
 *
 * @author Mark Angrish
 */
public class IndexManagerTest extends MultiDriverTestClass {

	private Configuration configuration;
	private MetaData metaData;

	@Before
	public void beforeMethod() {
		assumeTrue(Components.neo4jVersion() >= 3.0);
		configuration = new Configuration();
		configuration.copyFrom(Components.getConfiguration());
		metaData = new MetaData("org.neo4j.ogm.domain.forum");
	}

	// Success if does not fail
	@Test
	public void testIndexesAreSuccessfullyValidated() {

		getGraphDatabaseService().execute("CREATE CONSTRAINT ON ( login:Login ) ASSERT login.userName IS UNIQUE");
		configuration.autoIndexConfiguration().setAutoIndex("validate");
		Components.configure(configuration);
		IndexManager indexManager = new IndexManager(metaData, Components.driver());
		assertEquals(AutoIndexMode.VALIDATE, Components.autoIndexMode());
		indexManager.build();
	}

	@Test(expected = MissingIndexException.class)
	public void testIndexesAreFailValidation() {

		configuration.autoIndexConfiguration().setAutoIndex("validate");
		Components.configure(configuration);
		IndexManager indexManager = new IndexManager(metaData, Components.driver());
		assertEquals(AutoIndexMode.VALIDATE, Components.autoIndexMode());
		indexManager.build();
	}

	@Test
	public void testIndexDumpMatchesDatabaseIndexes() throws IOException {

		final String expected = "CREATE CONSTRAINT ON ( login:Login ) ASSERT login.userName IS UNIQUE";
		getGraphDatabaseService().execute(expected);

		configuration.autoIndexConfiguration().setAutoIndex("dump");
		configuration.autoIndexConfiguration().setDumpDir(".");
		configuration.autoIndexConfiguration().setDumpFilename("test.cql");

		File file = new File("./test.cql");

		try {
			Components.configure(configuration);
			IndexManager indexManager = new IndexManager(metaData, Components.driver());
			assertEquals(AutoIndexMode.DUMP, Components.autoIndexMode());
			indexManager.build();
			assertTrue(file.exists());
			assertTrue(file.length() > 0);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String actual = reader.readLine();
			assertEquals(expected, actual);
			reader.close();
		} finally {
			if (!file.delete()) {
				fail("Could not delete generated file. Check files system permissions");
			}
		}
	}

	// Success if does not fail.
	@Test
	public void testIndexesAreSuccessfullyAsserted() {

		getGraphDatabaseService().execute("CREATE CONSTRAINT ON (login:`Login`) ASSERT login.`userName` IS UNIQUE");
		configuration.autoIndexConfiguration().setAutoIndex("assert");
		Components.configure(configuration);
		IndexManager indexManager = new IndexManager(metaData, Components.driver());
		assertEquals(AutoIndexMode.ASSERT, Components.autoIndexMode());

		indexManager.build();
	}
}
