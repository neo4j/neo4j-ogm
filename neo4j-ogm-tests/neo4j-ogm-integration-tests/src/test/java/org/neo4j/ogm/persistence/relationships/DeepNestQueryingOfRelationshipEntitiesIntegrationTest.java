/*
 * Copyright (c) 2002-2020 "Neo4j,"
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
package org.neo4j.ogm.persistence.relationships;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.domain.gh824.Address;
import org.neo4j.ogm.domain.gh824.City;
import org.neo4j.ogm.domain.gh824.GroupMember;
import org.neo4j.ogm.domain.gh824.User;
import org.neo4j.ogm.domain.gh824.UserGroup;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Michael J. Simons
 */
public class DeepNestQueryingOfRelationshipEntitiesIntegrationTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.gh824");
    }

    @Before
    public void createData() {

        Address a1 = new Address();
        a1.setCode("0001");
        Address a2 = new Address();
        a2.setCode("0002");
        a2.setCity(new City("Aachen"));
        User u1 = new User();
        u1.setAddress(a1);
        User u2 = new User("Mr. User");
        u2.setAddress(a1);
        User u3 = new User();
        u3.setAddress(a2);
        UserGroup ug1 = new UserGroup();
        UserGroup ug2 = new UserGroup();
        List<GroupMember> members = Arrays
            .asList(new GroupMember(u1, ug1), new GroupMember(u2, ug2), new GroupMember(u3, ug2));

        Session session = sessionFactory.openSession();
        members.forEach(session::save);
    }

    @After
    public void purgeData() {

        sessionFactory.openSession().purgeDatabase();
    }

    @Test
    public void flattenedPathSegmentsShouldWork() {

        Filter playsFilter = new Filter("name", ComparisonOperator.EQUALS, "Mr. User");
        playsFilter.setOwnerEntityType(GroupMember.class);

        playsFilter.setNestedPath(
            new Filter.NestedPathSegment("user", User.class)
        );

        Collection<GroupMember> films = sessionFactory.openSession().loadAll(GroupMember.class, playsFilter);
        assertThat(films).hasSize(1);
    }

    @Test
    public void nPathSegmentsShouldWork() {

        Filter playsFilter = new Filter("code", ComparisonOperator.EQUALS, "0001");
        playsFilter.setOwnerEntityType(GroupMember.class);

        playsFilter.setNestedPath(
            new Filter.NestedPathSegment("user", User.class),
            new Filter.NestedPathSegment("address", Address.class)
        );

        Collection<GroupMember> films = sessionFactory.openSession().loadAll(GroupMember.class, playsFilter);
        assertThat(films).hasSize(2);
    }

    @Test
    public void nPlus1PathSegmentsShouldWork() {

        Filter playsFilter = new Filter("name", ComparisonOperator.EQUALS, "Aachen");
        playsFilter.setOwnerEntityType(GroupMember.class);

        playsFilter.setNestedPath(
            new Filter.NestedPathSegment("user", User.class),
            new Filter.NestedPathSegment("address", Address.class),
            new Filter.NestedPathSegment("city", City.class)
        );

        Collection<GroupMember> films = sessionFactory.openSession().loadAll(GroupMember.class, playsFilter);
        assertThat(films).hasSize(1);
    }
}
