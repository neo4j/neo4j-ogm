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

package org.neo4j.ogm.integration.friendships;

import static org.junit.Assert.*;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.ClassRule;
import org.junit.Test;
import org.neo4j.ogm.domain.friendships.Person;
import org.neo4j.ogm.driver.Drivers;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.session.transaction.TransactionManager;
import org.neo4j.ogm.testutil.Neo4jIntegrationTestRule;

/**
 * @author Luanne Misquitta
 */
public class FriendsInLongTransactionTest {

	@ClassRule
	public static Neo4jIntegrationTestRule neo4jRule = new Neo4jIntegrationTestRule();

	Session session =  new SessionFactory("org.neo4j.ogm.domain.friendships").openSession(neo4jRule.url());

	/**
	 * @see DATAGRAPH-703
	 */
	@Test
	public void createPersonAndFriendsInLongTransaction() {
		TransactionManager txRequestHandler = new TransactionManager(Drivers.HTTP, neo4jRule.url());
		try (Transaction tx = txRequestHandler.openTransaction(null)) {
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
