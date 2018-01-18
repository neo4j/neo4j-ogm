/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.drivers;

import org.junit.Test;
import org.neo4j.ogm.drivers.http.request.HttpRequestException;
import org.neo4j.ogm.exception.ConnectionException;
import org.neo4j.ogm.service.Component;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

/**
 * @see Issue 133
 * @author Luanne Misquitta
 */
public class DriverExceptionTest {

	@Test
	public void cleanUp() {
		// This should cleanup the configuration after this test messes things up
		Components.autoConfigure();
	}

	@Test(expected = ConnectionException.class)
	public void shouldThrowExceptionWhenHttpDriverCannotConnect() {
		Components.configure("ogm-http-invalid.properties");
		SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.social");
		Session session = sessionFactory.openSession();
		session.purgeDatabase();
	}

	@Test(expected = ConnectionException.class)
	public void shouldThrowExceptionWhenEmbeddedDriverCannotConnect() {
		Components.configure("ogm-embedded-invalid.properties");
		SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.social");
		Session session = sessionFactory.openSession();
		session.purgeDatabase();
	}


	@Test(expected = ConnectionException.class)
	public void shouldThrowExceptionWhenBoltDriverCannotConnect() {
		Components.configure("ogm-bolt-invalid.properties");
		SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.social");
		Session session = sessionFactory.openSession();
		session.purgeDatabase();
	}
}
