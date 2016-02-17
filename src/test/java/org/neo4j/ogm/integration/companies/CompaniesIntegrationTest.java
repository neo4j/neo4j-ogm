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

package org.neo4j.ogm.integration.companies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.neo4j.ogm.domain.companies.Company;
import org.neo4j.ogm.domain.companies.Device;
import org.neo4j.ogm.domain.companies.Person;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.Neo4jIntegrationTestRule;

/**
 * @author Luanne Misquitta
 */
public class CompaniesIntegrationTest {
	@ClassRule
	public static Neo4jIntegrationTestRule databaseServerRule = new Neo4jIntegrationTestRule();

	private static Session session;

	@BeforeClass
	public static void init() throws IOException {
		session = new SessionFactory("org.neo4j.ogm.domain.companies").openSession(databaseServerRule.url());
	}

	@After
	public void teardown() {
		session.purgeDatabase();
	}

	/**
	 * @see issue #85
	 */
	@Test
	public void employeesShouldNotBeSetAsOwnersWhenLoadingCompanies() {
		Company company = new Company("GraphAware");
		Person michal = new Person("Michal");
		Person daniela = new Person("Daniela");
		Set<Person> employees = new HashSet<>();
		employees.add(michal);
		employees.add(daniela);
		company.setEmployees(employees);
		session.save(company);
		session.clear();

		company = session.load(Company.class, company.getId());
		assertNotNull(company);
		assertEquals(2, company.getEmployees().size());
		assertNull(company.getOwners());

		for (Person employee : company.getEmployees()) {
			assertNotNull(employee.getEmployer());
			assertNull(employee.getOwns());
		}

	}

	/**
	 * @see issue #85
	 */
	@Test
	public void employeesAndOwnersShouldBeLoaded() {
		Company company = new Company("GraphAware");
		Person michal = new Person("Michal");
		Person daniela = new Person("Daniela");
		michal.setOwns(Collections.singleton(company));
		daniela.setOwns(Collections.singleton(company));
		Set<Person> employees = new HashSet<>();
		employees.add(michal);
		employees.add(daniela);
		company.setEmployees(employees);
		company.setOwners(employees);
		session.save(company);
		session.clear();

		company = session.load(Company.class, company.getId());
		assertNotNull(company);
		assertEquals(2, company.getEmployees().size());
		assertEquals(2, company.getOwners().size());

		for (Person employee : company.getEmployees()) {
			assertEquals(company.getId(), employee.getEmployer().getId());
			assertEquals(1, employee.getOwns().size());
			assertEquals(company.getId(), employee.getOwns().iterator().next().getId());
		}

	}

	/**
	 * @see Issue 112
	 */
	@Test
	public void testUndirectedRelationshipCanBeRemoved() {
		Person person = new Person();
		Device device = new Device();
		person.addDevice(device);
		session.save(person);
		person.removeDevice(device);
		assertEquals(0,person.getDevices().size());
		session.save(person);

		session.clear();
		person = session.load(Person.class, person.getId());
		assertNotNull(person);
		assertNull(person.getDevices());
	}
}
