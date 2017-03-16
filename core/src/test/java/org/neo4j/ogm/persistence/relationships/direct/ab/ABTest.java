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

package org.neo4j.ogm.persistence.relationships.direct.ab;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.persistence.relationships.direct.RelationshipTrait;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Vince Bickers
 */
public class ABTest extends RelationshipTrait {

    private static SessionFactory sessionFactory;
    private Session session;
    private A a;
    private B b;

    @BeforeClass
    public static void oneTimeSetup() {
        sessionFactory = new SessionFactory(baseConfiguration, "org.neo4j.ogm.persistence.relationships.direct.ab");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
        setUpEntityModel();
    }

    @After
    public void cleanup() {
        session.purgeDatabase();
    }

    private void setUpEntityModel() {
        a = new A();
        b = new B();
        a.b = b;
        b.a = a;
    }

    @Test
    public void shouldFindBFromA() {

        session.save(b);

        a = session.load(A.class, a.id);
        assertEquals(b, a.b);

    }

    @Test
    public void shouldFindAFromB() {

        session.save(a);

        b = session.load(B.class, b.id);
        assertEquals(a, b.a);

    }

    @Test
    public void shouldReflectRemovalA() {

        session.save(a);

        // given that we remove relationship from b's side
        b.a = null;
        session.save(b);

        // when we reload a
        a = session.load(A.class, a.id);

        // expect the relationship to have gone.
        assertNull(a.b);

    }

    @NodeEntity(label = "A")
    public static class A extends E {

        @Relationship(type = "EDGE", direction = Relationship.OUTGOING)
        B b;

        public A() {
        }
    }

    @NodeEntity(label = "B")
    public static class B extends E {

        @Relationship(type = "EDGE", direction = Relationship.INCOMING)
        A a;

        public B() {
        }
    }

    /**
     * Can be used as the basic class at the root of any entity for these tests,
     * provides the mandatory id field, a simple to-string method
     * and equals/hashcode.
     * <p/>
     * Note that without an equals/hashcode implementation, reloading
     * an object which already has a collection of items in it
     * will result in the collection items being added again, because
     * of the behaviour of the ogm merge function when handling
     * arrays and iterables.
     */
    public abstract static class E {

        public Long id;
        public String key;

        public E() {
            this.key = UUID.randomUUID().toString();
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + ":" + id + ":" + key;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            return (key.equals(((E) o).key));
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }

}
