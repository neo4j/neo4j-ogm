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

package org.neo4j.ogm.mapper.transitive.ab;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.IntegrationTestRule;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class ABTest
{

    private static final Driver driver = Components.driver();
    @ClassRule
    public static final TestRule server = new IntegrationTestRule(driver);

    private Session session;

    private A a;
    private B b;
    private R r;

    private static SessionFactory sessionFactory;

    @Before
    public void init() throws IOException {
        sessionFactory = new SessionFactory("org.neo4j.ogm.mapper.transitive.ab");
        session = sessionFactory.openSession(driver);
        setUpEntityModel();
    }

    private void setUpEntityModel() {

        a = new A();
        b = new B();
        r = new R();

        r.a = a;
        r.b = b;

        a.r = r;
        b.r = r;

    }

    @Test
    public void shouldFindBFromA() {

        session.save(b);

        a = session.load(A.class, a.id);

        assertEquals(b, a.r.b);

    }

    @Test
    public void shouldFindAFromB() {

        session.save(a);

        b = session.load(B.class, b.id);

        assertEquals(a, b.r.a);

    }

    @Test
    public void shouldReflectRemovalA() {

        session.save(a);

        // given that we remove the relationship

        b.r = null;
        a.r = null;

        session.save(b);

        // when we reload a
        a = session.load(A.class, a.id);

        // expect the relationship to have gone.
        assertNull(a.r);

    }

    /**
     * @see DATAGRAPH-714
     */
    @Test
    public void shouldBeAbleToUpdateRBySavingA() {
        A a1 = new A();
        B b3 = new B();
        R r3 = new R();
        r3.a = a1;
        r3.b = b3;
        r3.number = 1;
        a1.r = r3;
        b3.r = r3;

        session.save(a1);
        r3.number = 2;
        session.save(a1);

        session.clear();
        b3 = session.load(B.class, b3.id);
        assertEquals(2, b3.r.number);
    }

    /**
     * @see DATAGRAPH-714
     */
    @Test
    public void shouldBeAbleToUpdateRBySavingB() {
        A a1 = new A();
        B b3 = new B();
        R r3 = new R();
        r3.a = a1;
        r3.b = b3;
        r3.number = 1;
        a1.r = r3;
        b3.r = r3;

        session.save(a1);
        r3.number = 2;
        session.save(b3);

        session.clear();
        b3 = session.load(B.class, b3.id);
        assertEquals(2, b3.r.number);
    }

    /**
     * @see DATAGRAPH-714
     */
    @Test
    public void shouldBeAbleToUpdateRBySavingR() {
        A a1 = new A();
        B b3 = new B();
        R r3 = new R();
        r3.a = a1;
        r3.b = b3;
        r3.number = 1;
        a1.r = r3;
        b3.r = r3;

        session.save(a1);
        r3.number = 2;
        session.save(r3);

        session.clear();
        b3 = session.load(B.class, b3.id);
        assertEquals(2, b3.r.number);
    }

    @NodeEntity(label="A")
    public static class A extends E {
        @Relationship(type="EDGE", direction = Relationship.OUTGOING)
        R r;
    }

    @NodeEntity(label="B")
    public static class B extends E {
        @Relationship(type="EDGE", direction = Relationship.INCOMING)
        R r;
    }

    @RelationshipEntity(type="EDGE")
    public static class R {

        Long id;

        @StartNode
        A a;
        @EndNode
        B b;

        int number;

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + ":" + a.id + "->" + b.id;
        }

    }

    /**
     * Can be used as the basic class at the root of any entity for these tests,
     * provides the mandatory id field, a unique ref, a simple to-string method
     * and equals/hashcode implementation.
     *
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

            return (key.equals(((E)o).key));
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }

}
