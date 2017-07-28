/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.persistence.model;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    @RelationshipEntity(type = "R")
    public static class RelationEntity<O,T> {
        @GraphId
        public Long id;

        @StartNode
        public O origin;

        @EndNode
        public T target;

    }

    @RelationshipEntity(type = "OWNS")
    public static class Owns extends RelationEntity<User,User> {
        public Boolean isProfile;

        public Owns() {
        }

        public Owns(User owner, User target) {
            origin = owner;
            this.target = target;
        }
    }

}
