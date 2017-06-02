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

package org.neo4j.ogm.persistence.examples.companies;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.companies.partial.Company;
import org.neo4j.ogm.domain.companies.partial.Person;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;

/**
 * @author Frantisek Hartman
 */
public class PartialCompaniesIntegrationTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void init() throws IOException {
        sessionFactory = new SessionFactory("org.neo4j.ogm.domain.companies.partial");
    }

    @Before
    public void setUp() throws Exception {
        session = sessionFactory.openSession();
        session.purgeDatabase();

    }

    @Test
    public void whenSaveAndLoadCompany_thenShouldCorrectlySetPersonFields() throws Exception {
        Person alice = new Person("Alice the Founder");
        Person bob = new Person("Bob the employee");

        Company company = new Company("The Unicorn");
        company.setFounder(alice);
        company.setEmployees(singleton(bob));

        session.save(company);

        session.clear();

        Company loaded = session.load(Company.class, company.getId());
        assertEquals("Alice the Founder", loaded.getFounder().getName());

        assertEquals(1, loaded.getEmployees().size());
        Person employee = loaded.getEmployees().iterator().next();
        assertEquals("Bob the employee", employee.getName());

    }
}
