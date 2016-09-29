package org.neo4j.ogm.index;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * Make sure this tests works across all drivers supporting Neo4j 3.x and above.
 *
 * FIXME: Configuration makes trying to set up these tests very difficult. We need to change the way Configuration/Components works.
 *
 * @author Mark Angrish
 */
public class IndexManagerTest extends MultiDriverTestClass {

	private Configuration configuration;

	@Before
	public void beforeMethod() {
		assumeTrue(Components.neo4jVersion() >= 3.0);
		configuration = Components.getConfiguration();
	}

	@Test
	public void testValidateIndexes() {

		getGraphDatabaseService().execute("CREATE CONSTRAINT ON (login:`Login`) ASSERT login.`userName` IS UNIQUE");
		configuration.autoIndexConfiguration().setAutoIndex("validate");
		new SessionFactory(configuration, "org.neo4j.ogm.domain.forum");
	}

	@Test
	public void testDumpIndexes() {

		getGraphDatabaseService().execute("CREATE CONSTRAINT ON (login:`Login`) ASSERT login.`userName` IS UNIQUE");
		configuration.autoIndexConfiguration().setAutoIndex("dump");
		configuration.autoIndexConfiguration().setDumpDir(".");
		configuration.autoIndexConfiguration().setDumpFilename("test.cql");
		new SessionFactory(configuration, "org.neo4j.ogm.domain.forum");
		File file = new File("./test.cql");
		assertTrue(file.exists());
		file.delete();
	}

	@Test
	public void testAssertIndexes() {

		getGraphDatabaseService().execute("CREATE CONSTRAINT ON (login:`Login`) ASSERT login.`userName` IS UNIQUE");
		configuration.autoIndexConfiguration().setAutoIndex("assert");
		new SessionFactory(configuration, "org.neo4j.ogm.domain.forum");
	}
}
