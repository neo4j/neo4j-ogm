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

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.companies.partial.Company;
import org.neo4j.ogm.domain.companies.partial.Person;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Frantisek Hartman
 */
public class PartialCompaniesIntegrationTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void init() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.companies.partial");
    }

    @Before
    public void setUp() {
        session = sessionFactory.openSession();
        session.purgeDatabase();

    }

    @Test
    public void whenSaveAndLoadCompany_thenShouldCorrectlySetPersonFields() {
        Person alice = new Person("Alice the Founder");
        Person bob = new Person("Bob the employee");

        Company company = new Company("The Unicorn");
        company.setFounder(alice);
        company.setEmployees(singleton(bob));

        session.save(company);

        session.clear();

        Company loaded = session.load(Company.class, company.getId());
        assertThat(loaded.getFounder().getName()).isEqualTo("Alice the Founder");

        assertThat(loaded.getEmployees()).hasSize(1);
        Person employee = loaded.getEmployees().iterator().next();
        assertThat(employee.getName()).isEqualTo("Bob the employee");

    }
}
