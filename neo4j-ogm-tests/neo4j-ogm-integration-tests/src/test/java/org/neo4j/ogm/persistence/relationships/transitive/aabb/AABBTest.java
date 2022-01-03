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
package org.neo4j.ogm.persistence.relationships.transitive.aabb;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
@SuppressWarnings({"HiddenField", "MultipleVariableDeclarations", "RequireThis"})
public class AABBTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;
    private A a1, a2, a3;
    private B b1, b2, b3;
    private R r1, r2, r3, r4, r5, r6;

    @BeforeClass
    public static void oneTimeSetup() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.persistence.relationships.transitive.aabb");
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
        // three source nodes
        a1 = new A();
        a2 = new A();
        a3 = new A();

        // three target nodes
        b1 = new B();
        b2 = new B();
        b3 = new B();

        // six relationships
        r1 = new R(a1, b1);
        r2 = new R(a1, b2); //problem
        r3 = new R(a2, b1);
        r4 = new R(a2, b3);
        r5 = new R(a3, b2);
        r6 = new R(a3, b3);

        // assign relationships to both sides to ensure entity graph is fully connected
        a1.r = new R[] { r1, r2 };
        a2.r = new R[] { r3, r4 };
        a3.r = new R[] { r5, r6 };

        b1.r = new R[] { r1, r3 };
        b2.r = new R[] { r2, r5 };
        b3.r = new R[] { r4, r6 };
    }

    @Test
    public void shouldFindBFromA() {

        // because the graph is fully connected, we should be able to save any object to fully populate the graph
        session.save(b1);

        a1 = session.load(A.class, a1.id);
        a2 = session.load(A.class, a2.id);
        a3 = session.load(A.class, a3.id);

        assertThat(a1.r).extracting(x -> x.b).containsExactlyInAnyOrder(b1, b2);
        assertThat(a2.r).extracting(x -> x.b).containsExactlyInAnyOrder(b1, b3);
        assertThat(a3.r).extracting(x -> x.b).containsExactlyInAnyOrder(b2, b3);
    }

    @Test
    public void shouldFindAFromB() {

        // because the graph is fully connected, we should be able to save any object to fully populate the graph
        session.save(a1);

        b1 = session.load(B.class, b1.id);
        b2 = session.load(B.class, b2.id);
        b3 = session.load(B.class, b3.id);

        assertThat(b1.r.length).isEqualTo(2);
        assertThat(b2.r.length).isEqualTo(2);
        assertThat(b3.r.length).isEqualTo(2);
        assertThat(b1.r).extracting(b -> b.a).containsExactlyInAnyOrder(a1, a2);
        assertThat(b2.r).extracting(b -> b.a).containsExactlyInAnyOrder(a1, a3);
        assertThat(b3.r).extracting(b -> b.a).containsExactlyInAnyOrder(a2, a3);
    }

    @Test
    public void shouldReflectRemovalA() {

        // because the graph is fully connected, we should be able to save any object to fully populate the graph
        session.save(a1);

        // it is programmer's responsibility to keep the domain entities synchronized
        b2.r = null;
        a1.r = new R[] { r1 };
        a3.r = new R[] { r6 };

        session.save(b2);

        // when we reload a1
        a1 = session.load(A.class, a1.id);
        // expect the b2 relationship to have gone.
        assertThat(a1.r.length).isEqualTo(1);
        assertThat(a1.r[0].b).isEqualTo(b1);

        // when we reload a3
        a3 = session.load(A.class, a3.id);
        // expect the b2 relationship to have gone.
        assertThat(a3.r[0].b).isEqualTo(b3);

        // and when we reload a2
        a2 = session.load(A.class, a2.id);
        // expect its relationships to be intact.
        assertThat(a2.r).extracting(x -> x.b).containsExactlyInAnyOrder(b1, b3);
    }

    @Test
    @Ignore
    public void shouldHandleAddNewRelationshipBetweenASingleABPair() {
        // fully connected, will persist everything
        session.save(a1);

        R r7 = new R(a1, b1);

        a1.r = new R[] { r2, r7 };
        b1.r = new R[] { r3, r7 };

        session.save(a1);

        b1 = session.load(B.class, b1.id);

        assertThat(b1.r).containsExactlyInAnyOrder(r1, r3, r7);
        assertThat(a1.r).containsExactlyInAnyOrder(r1, r2, r7);
    }

    /**
     * @see DATAGRAPH-611
     */
    @Test
    public void shouldSaveRelationsForA1InTheCorrectDirection() {
        session.save(a1);

        session.clear();

        a1 = session.load(A.class, a1.id);
        assertThat(a1.r).containsExactlyInAnyOrder(r1, r2);
    }

    /**
     * @see DATAGRAPH-611
     */
    @Test
    public void shouldSaveRelationsForA2TheCorrectDirection() {
        session.save(a2);

        session.clear();

        a2 = session.load(A.class, a2.id);
        assertThat(a2.r).containsExactlyInAnyOrder(r3, r4);
    }

    /**
     * @see DATAGRAPH-611
     */
    @Test
    public void shouldSaveRelationsForA3TheCorrectDirection() {
        session.save(a3);

        session.clear();

        a3 = session.load(A.class, a3.id);
        assertThat(a3.r).containsExactlyInAnyOrder(r5, r6);
    }

    /**
     * @see DATAGRAPH-611
     */
    @Test
    public void shouldSaveRelationsForB1TheCorrectDirection() {
        session.save(b1);

        session.clear();

        b1 = session.load(B.class, b1.id);
        assertThat(b1.r).containsExactlyInAnyOrder(r1, r3);
    }

    /**
     * @see DATAGRAPH-611
     */
    @Test
    public void shouldSaveRelationsForB2TheCorrectDirection() {
        session.save(b2);

        session.clear();

        b2 = session.load(B.class, b2.id);
        assertThat(b2.r).containsExactlyInAnyOrder(r2, r5);
    }

    /**
     * @see DATAGRAPH-611
     */
    @Test
    public void shouldSaveRelationsForB3TheCorrectDirection() {
        session.save(b3);

        session.clear();

        b3 = session.load(B.class, b3.id);
        assertThat(b3.r).containsExactlyInAnyOrder(r4, r6);
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
        b3.r = new R[] { r3 };

        session.save(a1);
        r3.number = 2;
        session.save(a1);

        session.clear();
        b3 = session.load(B.class, b3.id);
        assertThat(b3.r[0].number).isEqualTo(2);
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
        b3.r = new R[] { r3 };

        session.save(a1);
        r3.number = 2;
        session.save(b3);

        session.clear();
        b3 = session.load(B.class, b3.id);
        assertThat(b3.r[0].number).isEqualTo(2);
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
        b3.r = new R[] { r3 };

        session.save(a1);
        r3.number = 2;
        session.save(r3);

        session.clear();
        b3 = session.load(B.class, b3.id);
        assertThat(b3.r[0].number).isEqualTo(2);
    }

    @NodeEntity(label = "A")
    public static class A extends E {

        @Relationship(type = "EDGE", direction = Relationship.Direction.OUTGOING)
        R[] r;
    }

    @NodeEntity(label = "B")
    public static class B extends E {

        @Relationship(type = "EDGE", direction = Relationship.Direction.INCOMING)
        R[] r;
    }

    @RelationshipEntity(type = "EDGE")
    public static class R extends E {

        //        Long id;

        @StartNode
        A a;
        @EndNode
        B b;

        int number;

        public R(A a, B b) {
            this.a = a;
            this.b = b;
        }

        public R() {
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + ":" + a.id + "->" + b.id;
        }
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
}
