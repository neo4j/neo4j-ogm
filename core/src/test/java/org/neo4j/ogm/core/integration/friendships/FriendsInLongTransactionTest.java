/*
 * Copyright (c)  [2011-2015] "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with separate copyright notices and license terms. Your use of the source code for these subcomponents is subject to the terms and conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 *
 */

package org.neo4j.ogm.core.integration.friendships;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.neo4j.ogm.api.driver.Driver;
import org.neo4j.ogm.api.transaction.Transaction;
import org.neo4j.ogm.core.session.Session;
import org.neo4j.ogm.core.session.SessionFactory;
import org.neo4j.ogm.core.session.transaction.DefaultTransactionManager;
import org.neo4j.ogm.api.service.Components;
import org.neo4j.ogm.core.testutil.IntegrationTestRule;
import org.neo4j.ogm.domain.friendships.Person;

import static org.junit.Assert.assertEquals;

/**
 * @author Luanne Misquitta
 */
public class FriendsInLongTransactionTest {

    private static final Driver driver = Components.driver();
    @ClassRule
    public static final TestRule server = new IntegrationTestRule(driver);

	Session session =  new SessionFactory("org.neo4j.ogm.domain.friendships").openSession(driver);

	/**
	 * @see DATAGRAPH-703
	 */
	@Test
	public void createPersonAndFriendsInLongTransaction() {
		DefaultTransactionManager txRequestHandler = new DefaultTransactionManager(driver);
		try (Transaction tx = txRequestHandler.openTransaction()) {
			assertEquals(Transaction.Status.OPEN, tx.status());
			Person john = new Person("John");
			session.save(john);

			Person bob = new Person("Bob");
			session.save(bob);

			Person bill = new Person("Bill");
			session.save(bill);

			john = session.load(Person.class, john.getId());
			bob = session.load(Person.class, bob.getId());
			john.addFriend(bob);
			session.save(john);

			john = session.load(Person.class, john.getId());
			bill = session.load(Person.class, bill.getId());
			john.addFriend(bill);
			session.save(john);

			session.clear();
			session.load(Person.class, john.getId());
			assertEquals(2, john.getFriends().size());
		}

	}

}
