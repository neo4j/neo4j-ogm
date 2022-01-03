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
package org.neo4j.ogm.persistence.relationships.direct.abb;

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
public class ABBTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;
    private A a;
    private B b1, b2;

    @BeforeClass
    public static void oneTimeSetup() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.persistence.relationships.direct.abb");
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
        b1 = new B();
        b2 = new B();

        a.b = new B[] { b1, b2 };
        b1.a = a;
        b2.a = a;
    }

    @Test
    public void shouldFindAFromB() {

        session.save(a);

        b1 = session.load(B.class, b1.id);
        b2 = session.load(B.class, b2.id);

        assertThat(b1.a).isEqualTo(a);
        assertThat(b2.a).isEqualTo(a);
    }

    @Test
    public void shouldFindBFromA() {

        session.save(b1);
        session.save(b2);

        a = session.load(A.class, a.id);

        assertThat(a.b).containsExactlyInAnyOrder(b1, b2);
    }

    @Test
    public void shouldReflectRemovalA() {

        session.save(a);

        // local model must be self-consistent
        b1.a = null;
        a.b = new B[] { b2 };

        session.save(b1);
        session.save(b2);

        // when we reload a
        a = session.load(A.class, a.id);

        // expect the b1 relationship to have gone.
        assertThat(a.b).containsExactlyInAnyOrder(new B[] { b2 });
    }

    @Test
    public void shouldBeAbleToAddAnotherB() {
        session.save(a);

        B b3 = new B();
        b3.a = a;
        a.b = new B[] { b1, b2, b3 };

        // fully connected graph, should be able to save any object
        session.save(b3);

        a = session.load(A.class, a.id);

        assertThat(a.b).containsExactlyInAnyOrder(b1, b2, b3);
    }

    @NodeEntity(label = "A")
    public static class A extends E {

        @Relationship(type = "EDGE", direction = Relationship.Direction.OUTGOING)
        B[] b;
    }

    @NodeEntity(label = "B")
    public static class B extends E {

        @Relationship(type = "EDGE", direction = Relationship.Direction.INCOMING)
        A a;
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
