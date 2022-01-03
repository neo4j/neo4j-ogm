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
package org.neo4j.ogm.persistence.relationships.direct.aa;

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
 * @author Luanne Misquitta
 */
@SuppressWarnings({"HiddenField", "MultipleVariableDeclarations"})
public class AATest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;
    private A a1, a2, a3, a4;
    private A loadedA1, loadedA2, loadedA3, loadedA4;

    @BeforeClass
    public static void oneTimeSetup() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.persistence.relationships.direct.aa");
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
        a4 = new A();

        a1.a = a2;
        a2.a = a3;
        a3.a = a4;
    }

    /**
     * @see DATAGRAPH-594
     */
    @Test
    public void shouldFindStartAFromEndA() {

        session.save(a1);
        session.save(a2);
        session.save(a3);
        session.save(a4);

        loadedA1 = session.load(A.class, a1.id);
        loadedA2 = session.load(A.class, a2.id);
        loadedA3 = session.load(A.class, a3.id);
        loadedA4 = session.load(A.class, a4.id);

        assertThat(loadedA1.a).isEqualTo(a2);
        assertThat(loadedA2.a).isEqualTo(a3);
        assertThat(loadedA3.a).isEqualTo(a4);
        assertThat(loadedA4.a).isNull();
    }

    /**
     * @see DATAGRAPH-594
     */
    @Test
    public void shouldFindEndAFromStartA() {

        session.save(a1);
        session.save(a2);
        session.save(a3);
        session.save(a4);

        loadedA1 = session.load(A.class, a1.id);
        loadedA2 = session.load(A.class, a2.id);
        loadedA3 = session.load(A.class, a3.id);
        loadedA4 = session.load(A.class, a4.id);

        assertThat(a1.a).isEqualTo(loadedA2);
        assertThat(a2.a).isEqualTo(loadedA3);
        assertThat(a3.a).isEqualTo(loadedA4);
    }

    /**
     * @see DATAGRAPH-594
     */
    @Test
    public void shouldPreserveAAfterReflectRemovalOtherA() {

        session.save(a1);
        session.save(a2);
        session.save(a3);
        session.save(a4);

        // it is our responsibility to keep the domain entities synchronized
        a2.a = null;

        session.save(a2);

        //when we reload a2
        loadedA2 = session.load(A.class, a2.id);
        // expect its relationships have gone.
        assertThat(loadedA2.a).isNull();

        // when we reload a1
        loadedA1 = session.load(A.class, a1.id);
        // expect the original relationship to remain intact.
        assertThat(loadedA1.a).isEqualTo(a2);

        // when we reload a3
        loadedA3 = session.load(A.class, a3.id);
        // expect the original relationship to remain intact.
        assertThat(loadedA3.a).isEqualTo(a4);

        //when we reload a4
        loadedA4 = session.load(A.class, a4.id);
        //expect the original relationships to remain intact.
        assertThat(loadedA4.a).isNull();
    }

    @NodeEntity(label = "A")
    public static class A extends E {

        @Relationship(type = "EDGE", direction = Relationship.Direction.INCOMING)
        A a;
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
