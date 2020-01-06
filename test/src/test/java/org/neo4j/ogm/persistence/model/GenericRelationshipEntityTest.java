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

package org.neo4j.ogm.persistence.model;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Frantisek Hartman
 */
public class GenericRelationshipEntityTest extends MultiDriverTestClass {

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.persistence.model");
    }

    @Before
    public void setUp() throws Exception {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void shouldBeAbleToSaveAndLoadRelationshipEntityWithBaseGenericStartAndEnd() throws Exception {
        User michal = new User("Michal");
        User frantisek = new User("Frantisek");

        michal.addSlave(frantisek);

        session.save(michal);
        session.clear();

        User loaded = session.load(User.class, michal.id);
        assertThat(loaded.name).isEqualTo("Michal");
        assertThat(michal.slaves.get(0).target.name).isEqualTo("Frantisek");
    }

    public static class User {
        public Long id;

        public String name;

        @Relationship(type = "OWNS", direction = "OUTGOING")
        List<Owns> slaves;

        public User() {
        }

        public User(String name) {

            this.name = name;
        }

        public void addSlave(User user) {
            if (slaves == null) {
                slaves = new ArrayList<>();
            }
            slaves.add(new Owns(this, user));
        }
    }

    public static class RelationEntity<O, T> {
        @GraphId
        public Long id;

        @StartNode
        public O origin;

        @EndNode
        public T target;

    }

    @RelationshipEntity(type = "OWNS")
    public static class Owns extends RelationEntity<User, User> {
        public Boolean isProfile;

        public Owns() {
        }

        public Owns(User owner, User target) {
            origin = owner;
            this.target = target;
        }
    }

}
