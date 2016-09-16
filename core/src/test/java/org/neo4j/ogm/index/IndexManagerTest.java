package org.neo4j.ogm.index;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.session.SessionFactory;

/**
 * Created by markangrish on 16/09/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class IndexManagerTest {

	@Mock Driver driver;

	@Test
	public void testValidateIndexes() {
		Configuration configuration = new Configuration();
		configuration.autoIndexConfiguration().setAutoIndex("validate");
		SessionFactory sessionFactory = new SessionFactory(configuration, "org.neo4j.ogm.domain.forum");
	}
}
