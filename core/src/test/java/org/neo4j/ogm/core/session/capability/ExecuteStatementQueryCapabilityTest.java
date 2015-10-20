/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.core.session.capability;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.ogm.api.driver.Driver;
import org.neo4j.ogm.api.model.Statistics;
import org.neo4j.ogm.config.Components;
import org.neo4j.ogm.core.domain.cineasts.annotated.Actor;
import org.neo4j.ogm.core.testutil.TestUtils;
import org.neo4j.ogm.core.domain.cineasts.annotated.Actor;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.core.testutil.TestUtils;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Luanne Misquitta
 */
public class ExecuteStatementQueryCapabilityTest {

    private static final Driver driver = Components.driver();

	private Session session;

	@Before
	public void init() throws IOException {
		session = new SessionFactory("org.neo4j.ogm.domain.cineasts.annotated").openSession(driver);
		importCineasts();
	}

	private void importCineasts() {
		session.execute(TestUtils.readCQLFile("org/neo4j/ogm/cql/cineasts.cql").toString(), Utils.map());
	}

	@After
	public void clearDatabase() {
		session.purgeDatabase();
	}

	/**
	 * @see DATAGRAPH-697
	 */
	@Test(expected = RuntimeException.class)
	public void queryWhichReturnsResultShouldNotBePermitted() {
		session.save(new Actor("Jeff"));
		session.save(new Actor("John"));
		session.save(new Actor("Colin"));
		session.execute("MATCH (n:Actor) return n.name", Collections.EMPTY_MAP);
	}

	/**
	 * @see DATAGRAPH-697
	 */
	@Test
	public void queryShouldReturnQueryStats() {
		session.save(new Actor("Jeff"));
		session.save(new Actor("John"));
		session.save(new Actor("Colin"));
		Statistics queryStatistics = session.execute("MATCH (n:Actor) set n.age={age}", MapUtil.map("age", 30));
		assertNotNull(queryStatistics);
		assertEquals(3, queryStatistics.getPropertiesSet());
	}

}
