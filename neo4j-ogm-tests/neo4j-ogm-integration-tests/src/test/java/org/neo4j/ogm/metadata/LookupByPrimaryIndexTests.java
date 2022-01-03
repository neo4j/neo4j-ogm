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
package org.neo4j.ogm.metadata;

import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.annotations.ids.ValidAnnotations;
import org.neo4j.ogm.domain.annotations.ids.ValidAnnotations.IdAndGenerationType;
import org.neo4j.ogm.domain.autoindex.valid.Invoice;
import org.neo4j.ogm.domain.cineasts.annotated.ExtendedUser;
import org.neo4j.ogm.domain.cineasts.annotated.User;
import org.neo4j.ogm.domain.cineasts.partial.Actor;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Mark Angrish
 * @author Nicolas Mervaillie
 */
public class LookupByPrimaryIndexTests extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(),
            "org.neo4j.ogm.domain.cineasts.annotated",
            "org.neo4j.ogm.domain.annotations.ids",
            "org.neo4j.ogm.domain.autoindex.valid"
        );
    }

    @Before
    public void setUp() {
        session = sessionFactory.openSession();
    }

    @Test
    public void loadUsesIdWhenPresent() {

        ValidAnnotations.Basic entity = new ValidAnnotations.Basic();
        entity.identifier = "id1";
        session.save(entity);

        final Session session2 = sessionFactory.openSession();

        final ValidAnnotations.Basic retrievedEntity = session2.load(ValidAnnotations.Basic.class, "id1");
        assertThat(retrievedEntity).isNotNull();
        assertThat(retrievedEntity.identifier).isEqualTo(entity.identifier);
    }

    @Test
    public void loadUsesIdWhenPresentOnParent() {

        ValidAnnotations.BasicChild entity = new ValidAnnotations.BasicChild();
        entity.identifier = "id1";
        session.save(entity);

        final Session session2 = sessionFactory.openSession();

        final ValidAnnotations.Basic retrievedEntity = session2.load(ValidAnnotations.Basic.class, "id1");
        assertThat(retrievedEntity).isNotNull();
        assertThat(retrievedEntity.identifier).isEqualTo(entity.identifier);
    }

    @Test
    public void saveWithStringUuidGeneration() {

        IdAndGenerationType entity = new IdAndGenerationType();
        session.save(entity);

        assertThat(entity.identifier).isNotNull();

        final Session session2 = sessionFactory.openSession();

        final IdAndGenerationType retrievedEntity = session2.load(IdAndGenerationType.class, entity.identifier);
        assertThat(retrievedEntity).isNotNull();
        assertThat(retrievedEntity.identifier).isNotNull().isEqualTo(entity.identifier);
    }

    @Test
    public void saveWithUuidGeneration() {

        ValidAnnotations.UuidIdAndGenerationType entity = new ValidAnnotations.UuidIdAndGenerationType();
        session.save(entity);

        assertThat(entity.identifier).isNotNull();

        final Session session2 = sessionFactory.openSession();

        final ValidAnnotations.UuidIdAndGenerationType retrievedEntity = session2
            .load(ValidAnnotations.UuidIdAndGenerationType.class, entity.identifier);
        assertThat(retrievedEntity).isNotNull();
        assertThat(retrievedEntity.identifier).isNotNull().isEqualTo(entity.identifier);
    }

    @Test
    public void loadUsesPrimaryIndexWhenPresent() {

        User user1 = new User("login1", "Name 1", "password");
        session.save(user1);

        final Session session2 = sessionFactory.openSession();

        final User retrievedUser1 = session2.load(User.class, "login1");
        assertThat(retrievedUser1).isNotNull();
        assertThat(retrievedUser1.getLogin()).isEqualTo(user1.getLogin());
    }

    @Test
    public void loadAllUsesPrimaryIndexWhenPresent() {

        User user1 = new User("login1", "Name 1", "password");
        session.save(user1);
        User user2 = new User("login2", "Name 2", "password");
        session.save(user2);

        session.clear();
        Collection<User> users = session.loadAll(User.class, asList("login1", "login2"));
        assertThat(users.size()).isEqualTo(2);

        session.clear();
        users = session.loadAll(User.class, asList("login1", "login2"), 0);
        assertThat(users.size()).isEqualTo(2);

        session.clear();
        users = session.loadAll(User.class, asList("login1", "login2"), -1);
        assertThat(users.size()).isEqualTo(2);
    }

    @Test
    public void loadUsesPrimaryIndexWhenPresentOnSuperclass() {

        ExtendedUser user1 = new ExtendedUser("login2", "Name 2", "password");
        session.save(user1);

        final Session session2 = sessionFactory.openSession();

        final User retrievedUser1 = session2.load(ExtendedUser.class, "login2");
        assertThat(retrievedUser1).isNotNull();
        assertThat(retrievedUser1.getLogin()).isEqualTo(user1.getLogin());
    }

    @Test
    public void loadUsesGraphIdWhenPrimaryIndexNotPresent() {

        SessionFactory sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.cineasts.partial");
        Session session1 = sessionFactory.openSession();
        Actor actor = new Actor("David Hasslehoff");
        session1.save(actor);

        final Long id = actor.getId();

        final Session session2 = sessionFactory.openSession();

        final Actor retrievedActor = session2.load(Actor.class, id);
        assertThat(retrievedActor).isNotNull();
        assertThat(retrievedActor.getName()).isEqualTo(actor.getName());
    }

    /**
     * This test makes sure that if the primary key is a Long, it isn't mixed up with the Graph Id.
     */
    @Test
    public void loadUsesPrimaryIndexWhenPresentEvenIfTypeIsLong() {
        Invoice invoice = new Invoice(223L, "Company", 100000L);
        session.save(invoice);

        final Session session2 = sessionFactory.openSession();

        Invoice retrievedInvoice = session2.load(Invoice.class, 223L);
        assertThat(retrievedInvoice).isNotNull();
    }

    /**
     * Case where primary key is of type Long and entity with such graph id exists
     */
    @Test // DATAGRAPH-1008
    public void loadShouldNotMixLongPrimaryIndexAndGraphId() throws Exception {

        Invoice invoice1 = new Invoice(223L, "Company", 100000L);
        session.save(invoice1);

        long graphId = session.query(Long.class, "MATCH (i:Invoice) WHERE i.invoice_number = $invoice_number RETURN id(i)",
            Collections.singletonMap("invoice_number", 223L)).iterator().next();

        // use graph id value as primary key of type long
        Invoice invoice2 = new Invoice(graphId, "Company", 100000L);
        session.save(invoice2);

        // return `invoice 2`, not `invoice 1`
        Invoice loaded = session.load(Invoice.class, invoice2.getNumber());
        assertThat(loaded).isEqualTo(invoice2);

        Collection<Invoice> invoices = session.loadAll(Invoice.class);
        assertThat(invoices).containsOnly(invoice1, invoice2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionRaisedWhenLookupDoneByNonLongKeyAndThereIsNoPrimaryIndex() {
        SessionFactory sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.cineasts.partial");
        Session session1 = sessionFactory.openSession();

        Actor actor = new Actor("David Hasslehoff");
        session1.save(actor);

        session1.load(Actor.class, "david-id");
    }

    @Test
    public void loadShouldNotMixPrimaryKeysOfDifferentLabels() {

        User user = new User("login", "name", "password");
        session.save(user);

        IdAndGenerationType entity = new IdAndGenerationType();
        entity.identifier = "login";
        session.save(entity);

        User loadedUser = session.load(User.class, "login");
        assertThat(loadedUser).isEqualTo(user);

        IdAndGenerationType loadedEntity = session.load(IdAndGenerationType.class, "login");
        assertThat(loadedEntity).isEqualTo(entity);
    }
}
