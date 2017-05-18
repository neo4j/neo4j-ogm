package org.neo4j.ogm.persistence.model;

import static org.junit.Assert.*;
import static org.neo4j.ogm.testutil.GraphTestUtils.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Vector;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Result;
import org.neo4j.ogm.domain.social.Individual;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.GraphTestUtils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

public class ArraysMappingTest extends MultiDriverTestClass {
	private static SessionFactory sessionFactory;

	private Session session;

	@BeforeClass
	public static void oneTimeSetUp() {
		sessionFactory = new SessionFactory(getBaseConfiguration().build(), "org.neo4j.ogm.domain.social");
	}

	@Before
	public void setUpMapper() {
		session = sessionFactory.openSession();
		session.purgeDatabase();
	}

	@Test
	public void shouldGenerateCypherToPersistArraysOfPrimitives() {
		Individual individual = new Individual();
		individual.setName("Jeff");
		individual.setAge(41);
		individual.setBankBalance(1000.50f);
		individual.setPrimitiveIntArray(new int[]{1, 6, 4, 7, 2});

		session.save(individual);

		GraphTestUtils.assertSameGraph(getGraphDatabaseService(), "CREATE (:Individual {name:'Jeff', age:41, bankBalance: 1000.50, code:0, primitiveIntArray:[1,6,4,7,2]})");

		session.clear();

		Individual loadedIndividual = session.load(Individual.class, individual.getId());
		assertArrayEquals(individual.getPrimitiveIntArray(), loadedIndividual.getPrimitiveIntArray());
	}

	@Test
	public void shouldGenerateCypherToPersistByteArray() {
		Individual individual = new Individual();
		individual.setAge(41);
		individual.setBankBalance(1000.50f);
		individual.setPrimitiveByteArray(new byte[]{1, 2, 3, 4, 5});

		session.save(individual);
		GraphTestUtils.assertSameGraph(getGraphDatabaseService(), "CREATE (:Individual {age:41, bankBalance: 1000.50, code:0, primitiveByteArray:'AQIDBAU='})");

		Result executionResult = getGraphDatabaseService().execute("MATCH (i:Individual) RETURN i.primitiveByteArray AS bytes");
		Map<String, Object> result = executionResult.next();
		executionResult.close();
		assertEquals("The array wasn't persisted as the correct type", "AQIDBAU=", result.get("bytes")); //Byte arrays are converted to Base64 Strings
	}

	@Test
	public void shouldGenerateCypherToPersistCollectionOfBoxedPrimitivesToArrayOfPrimitives() {
		Individual individual = new Individual();
		individual.setName("Gary");
		individual.setAge(36);
		individual.setBankBalance(99.25f);
		individual.setFavouriteRadioStations(new Vector<>(Arrays.asList(97.4, 105.4, 98.2)));

		session.save(individual);
		assertSameGraph(getGraphDatabaseService(),
				"CREATE (:Individual {name:'Gary', age:36, bankBalance:99.25, code:0, favouriteRadioStations:[97.4, 105.4, 98.2]})");
	}

}
