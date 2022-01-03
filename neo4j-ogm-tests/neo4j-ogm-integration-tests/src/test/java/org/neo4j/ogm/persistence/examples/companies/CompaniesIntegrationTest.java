/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.persistence.examples.companies;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.companies.annotated.Company;
import org.neo4j.ogm.domain.companies.annotated.Device;
import org.neo4j.ogm.domain.companies.annotated.Person;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Luanne Misquitta
 * @author Vince Bickers
 */
public class CompaniesIntegrationTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void init() throws IOException {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.companies.annotated");
    }

    @Before
    public void setUp() {
        session = sessionFactory.openSession();
    }

    @After
    public void teardown() {
        session.purgeDatabase();
    }

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
        assertThat(company).isNotNull();
        assertThat(company.getEmployees()).hasSize(2);
        assertThat(company.getOwners()).isNull();

        for (Person employee : company.getEmployees()) {
            assertThat(employee.getEmployer()).isNotNull();
            assertThat(employee.getOwns()).isNull();
        }
    }

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
        assertThat(company).isNotNull();
        assertThat(company.getEmployees()).hasSize(2);
        assertThat(company.getOwners()).hasSize(2);

        for (Person employee : company.getEmployees()) {
            assertThat(employee.getEmployer().getId()).isEqualTo(company.getId());
            assertThat(employee.getOwns()).hasSize(1);
            assertThat(employee.getOwns().iterator().next().getId()).isEqualTo(company.getId());
        }
    }

    /**
     * @see Issue 112
     */
    @Test
    public void shouldDeleteUndirectedRelationship() {
        Person person = new Person();
        Device device = new Device();
        person.addDevice(device);
        session.save(person);
        person.removeDevice(device);
        assertThat(person.getDevices()).isEmpty();
        session.save(person);

        session.clear();
        person = session.load(Person.class, person.getId());
        assertThat(person).isNotNull();
        assertThat(person.getDevices()).isNull();
    }
}
