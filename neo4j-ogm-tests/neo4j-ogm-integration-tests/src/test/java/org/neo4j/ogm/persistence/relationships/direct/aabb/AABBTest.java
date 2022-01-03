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
package org.neo4j.ogm.persistence.relationships.direct.aabb;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Vince Bickers
 */
@SuppressWarnings({"HiddenField", "MultipleVariableDeclarations"})
public class AABBTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;
    private A a1, a2, a3;
    private B b1, b2, b3;

    @BeforeClass
    public static void oneTimeSetup() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.persistence.relationships.direct.aabb");
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
        a1 = new A();
        a2 = new A();
        a3 = new A();

        b1 = new B();
        b2 = new B();
        b3 = new B();

        a1.b = new B[] { b1, b2 };
        a2.b = new B[] { b1, b3 };
        a3.b = new B[] { b2, b3 };

        b1.a = new A[] { a1, a2 };
        b2.a = new A[] { a1, a3 };
        b3.a = new A[] { a2, a3 };
    }

    @Test
    public void shouldFindAFromB() {

        session.save(a1);
        session.save(a2);
        session.save(a3);

        b1 = session.load(B.class, b1.id);
        b2 = session.load(B.class, b2.id);
        b3 = session.load(B.class, b3.id);

        assertThat(b1.a).containsExactlyInAnyOrder(a1, a2);
        assertThat(b2.a).containsExactlyInAnyOrder(a1, a3);
        assertThat(b3.a).containsExactlyInAnyOrder(a2, a3);
    }

    @Test
    public void shouldFindBFromA() {

        session.save(b1);
        session.save(b2);
        session.save(b3);

        a1 = session.load(A.class, a1.id);
        a2 = session.load(A.class, a2.id);
        a3 = session.load(A.class, a3.id);

        assertThat(a1.b).containsExactlyInAnyOrder(b1, b2);
        assertThat(a2.b).containsExactlyInAnyOrder(b1, b3);
        assertThat(a3.b).containsExactlyInAnyOrder(b2, b3);
    }

    @Test
    public void shouldReflectRemovalA() {

        session.save(a1);
        session.save(a2);
        session.save(a3);

        // it is our responsibility to keep the domain entities synchronized
        b2.a = null;
        a1.b = new B[] { b1 };
        a3.b = new B[] { b3 };

        session.save(b2);

        // when we reload a1
        a1 = session.load(A.class, a1.id);

        // expect the b2 relationship to have gone.
        assertThat(a1.b).containsExactlyInAnyOrder(b1);

        // when we reload a3
        a3 = session.load(A.class, a3.id);

        // expect the b2 relationship to have gone.
        assertThat(a3.b).containsExactlyInAnyOrder(b3);

        // but when we reload a2
        //session.clear();

        a2 = session.load(A.class, a2.id);

        // expect its relationships to be intact.
        assertThat(a2.b).containsExactlyInAnyOrder(b1, b3);
    }

    @Test
    public void shouldBeAbleToAddAnotherB() {
        session.save(a1);

        B b3 = new B();
        b3.a = new A[] { a1 };
        a1.b = new B[] { b1, b2, b3 };

        // fully connected graph, should be able to save anu object
        session.save(b3);

        a1 = session.load(A.class, a1.id);

        assertThat(a1.b).containsExactlyInAnyOrder(b1, b2, b3);
    }

    @NodeEntity(label = "A")
    public static class A extends E {

        @Relationship(type = "EDGE", direction = Relationship.Direction.OUTGOING)
        B[] b;
    }

    @NodeEntity(label = "B")
    public static class B extends E {

        @Relationship(type = "EDGE", direction = Relationship.Direction.INCOMING)
        A[] a;
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
