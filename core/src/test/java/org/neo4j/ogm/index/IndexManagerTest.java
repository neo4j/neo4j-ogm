package org.neo4j.ogm.index;

import static org.junit.Assume.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.neo4j.ogm.config.Components;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * Make sure this tests works across all drivers supporting Neo4j 3.x and above.
 *
 * TODO: Not finished. No assertions. Have to refactor to find a way to expose indexes.
 *
 * Created by markangrish on 16/09/2016.
 */
public class IndexManagerTest extends MultiDriverTestClass {

	@Mock

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
		configuration.autoIndexConfiguration().setDumpDir("tmp");
		configuration.autoIndexConfiguration().setDumpFilename("test.cql");
		new SessionFactory(configuration, "org.neo4j.ogm.domain.forum");
		File file = new File("./tmp/test.cql");
		file.delete();
	}

	@Test
	public void testAssertIndexes() {

		getGraphDatabaseService().execute("CREATE CONSTRAINT ON (login:`Login`) ASSERT login.`userName` IS UNIQUE");
		configuration.autoIndexConfiguration().setAutoIndex("assert");
		new SessionFactory(configuration, "org.neo4j.ogm.domain.forum");
	}
}
