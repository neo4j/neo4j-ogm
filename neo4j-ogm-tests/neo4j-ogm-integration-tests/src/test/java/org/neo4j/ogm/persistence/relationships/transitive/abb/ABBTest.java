/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.ogm.persistence.relationships.transitive.abb;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
@SuppressWarnings({"HiddenField", "MultipleVariableDeclarations"})
public class ABBTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;
    private Session session;
    private A a;
    private B b1, b2;
    private R r1, r2;

    @BeforeClass
    public static void oneTimeSetup() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.persistence.relationships.transitive.abb");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        setUpEntityModel();
    }

    @After
    public void cleanup() {
        session.purgeDatabase();
    }

    private void setUpEntityModel() {

        a = new A();

        b1 = new B();
        b2 = new B();

        r1 = new R();
        r2 = new R();

        r1.a = a;
        r1.b = b1;

        r2.a = a;
        r2.b = b2;

        a.r = new R[] { r1, r2 };
        b1.r = r1;
        b2.r = r2;
    }

    @Test
    public void shouldFindBFromA() {

        session.save(b1);

        a = session.load(A.class, a.id);

        assertThat(a.r.length).isEqualTo(2);
        assertThat(a.r).extracting(x -> x.b).containsExactlyInAnyOrder(b1, b2);
    }

    @Test
    public void shouldFindAFromB() {

        session.save(a);

        b1 = session.load(B.class, b1.id);
        b2 = session.load(B.class, b2.id);

        assertThat(b1.r.a).isEqualTo(a);
        assertThat(b2.r.a).isEqualTo(a);
    }

    @Test
    public void shouldReflectRemovalA() {

        session.save(a);

        // local model must be self-consistent
        b1.r = null;
        a.r = new R[] { r2 };

        session.save(b1);

        // when we reload a
        a = session.load(A.class, a.id);

        // expect the b1 relationship to have gone.
        assertThat(a.r.length).isEqualTo(1);
        assertThat(new B[] { a.r[0].b }).isEqualTo(new B[] { b2 });
    }

    @Test
    public void shouldBeAbleToAddNewB() {

        session.save(a);

        B b3 = new B();
        R r3 = new R();

        r3.a = a;
        r3.b = b3;
        b3.r = r3;
        a.r = new R[] { r1, r2, r3 };

        // fully connected graph, should be able to save any object
        session.save(a);

        // try others?

        b3 = session.load(B.class, b3.id);

        assertThat(new A[] { b3.r.a }).isEqualTo(new A[] { a });
        assertThat(new A[] { b3.r.a }).isEqualTo(new A[] { a });
    }

    @Test
    public void shouldBeAbleToAddNewR() {

        session.save(a);

        B b3 = new B();
        R r3 = new R();

        r3.a = a;
        r3.b = b3;
        b3.r = r3;
        a.r = new R[] { r1, r2, r3 };

        // fully connected graph, should be able to save any object
        session.save(r3);

        b3 = session.load(B.class, b3.id);

        assertThat(new A[] { b3.r.a }).isEqualTo(new A[] { a });
        assertThat(a.r).isEqualTo(new R[] { r1, r2, r3 });
        assertThat(new B[] { a.r[0].b, a.r[1].b, a.r[2].b }).isEqualTo(new B[] { b1, b2, b3 });
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
        a1.r = new R[] { r3 };
        b3.r = r3;

        session.save(a1);
        r3.number = 2;
        session.save(a1);

        session.clear();
        b3 = session.load(B.class, b3.id);
        assertThat(b3.r.number).isEqualTo(2);
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
        a1.r = new R[] { r3 };
        b3.r = r3;

        session.save(a1);
        r3.number = 2;
        session.save(b3);

        session.clear();
        b3 = session.load(B.class, b3.id);
        assertThat(b3.r.number).isEqualTo(2);
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
        a1.r = new R[] { r3 };
        b3.r = r3;

        session.save(a1);
        r3.number = 2;
        session.save(r3);

        session.clear();
        b3 = session.load(B.class, b3.id);
        assertThat(b3.r.number).isEqualTo(2);
    }

    @NodeEntity(label = "A")
    public static class A extends E {

        @Relationship(type = "EDGE", direction = Relationship.OUTGOING)
        R[] r;
    }

    @NodeEntity(label = "B")
    public static class B extends E {

        @Relationship(type = "EDGE", direction = Relationship.INCOMING)
        R r;
    }

    /**
     * Can be used as the basic class at the root of any entity for these tests,
     * provides the mandatory id field, a unique ref, a simple to-string method
     * and equals/hashcode implementation.
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

            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            return (key.equals(((E) o).key));
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }

    @RelationshipEntity(type = "EDGE")
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
}
