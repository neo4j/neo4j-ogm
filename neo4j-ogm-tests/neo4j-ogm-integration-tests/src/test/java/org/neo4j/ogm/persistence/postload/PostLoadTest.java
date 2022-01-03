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
package org.neo4j.ogm.persistence.postload;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.generic_hierarchy.AnotherEntity;
import org.neo4j.ogm.domain.generic_hierarchy.ChildA;
import org.neo4j.ogm.domain.generic_hierarchy.ChildB;
import org.neo4j.ogm.domain.generic_hierarchy.ChildC;
import org.neo4j.ogm.domain.postload.User;
import org.neo4j.ogm.domain.postload.UserWithBetterPostLoadMethod;
import org.neo4j.ogm.domain.postload.UserWithBrokenMethodDeclaration;
import org.neo4j.ogm.exception.core.MetadataException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * Test for {@link org.neo4j.ogm.annotation.PostLoad} annotation behaviour
 *
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class PostLoadTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.postload",
            "org.neo4j.ogm.domain.generic_hierarchy");
    }

    @Before
    public void setUp() throws Exception {
        session = sessionFactory.openSession();
        session.purgeDatabase();
        User.resetPostLoadCount();
    }

    @Test
    public void shouldCallPostLoadMethod() throws Exception {
        User user = new User();
        session.save(user);

        session.clear();

        User loaded = session.load(User.class, user.getId());
        assertThat(loaded).isNotNull();

        assertThat(User.getPostLoadCount()).isEqualTo(1);
    }

    @Test
    public void shouldCallPostLoadMethodWhenEntityIsInSession() throws Exception {
        User user = new User();
        session.save(user);

        session.load(User.class, user.getId());
        session.load(User.class, user.getId());

        assertThat(User.getPostLoadCount()).isEqualTo(2);
    }

    @Test
    public void shouldCallPostLoadForEachEntityOnce() throws Exception {
        User commonFriend = new User();
        User u1 = new User();
        User u2 = new User();

        commonFriend.addFriend(u1);
        commonFriend.addFriend(u2);

        session.save(commonFriend);
        session.clear();

        // this returns multiple rows (2^3 = 8 rows), but should execute post load only once when all entities are
        // hydrated
        session.query("MATCH (u:User)-[rel]-(friend:User) RETURN u,rel,friend", Collections.emptyMap());

        assertThat(User.getPostLoadCount()).isEqualTo(3);
    }

    @Test // #516
    public void shouldCallNonPublicFinalPostLoad() throws Exception {
        UserWithBetterPostLoadMethod user = new UserWithBetterPostLoadMethod();
        session.save(user);

        session.clear();

        UserWithBetterPostLoadMethod loaded = session.load(UserWithBetterPostLoadMethod.class, user.getId());
        assertThat(loaded).isNotNull();
        assertThat(loaded.getRandomName()).isNotEqualTo(user.getRandomName());
    }

    @Test // #516
    public void shouldPreventAmbiguousPostLoadScenario() {

        UserWithBrokenMethodDeclaration user = new UserWithBrokenMethodDeclaration();
        session.save(user);

        assertThatExceptionOfType(MetadataException.class)
            .isThrownBy(() -> session.loadAll(UserWithBrokenMethodDeclaration.class))
            .withMessage("Cannot have more than one post load method annotated with @PostLoad for class '%s'",
                UserWithBrokenMethodDeclaration.class.getName());
    }

    @Test // #414
    public void shouldRecognizeOverwrittenPostLoadFromSuperClass() {
        ChildA parent = new ChildA();
        parent.add(new ChildB());
        parent.add(new ChildB());
        parent.add(new ChildC());
        session.save(parent);
        session.clear();

        Set<AnotherEntity> children = session.load(ChildA.class, parent.getUuid()).getChildren();
        assertThat(children).isNotEmpty()
            .filteredOn(ChildB.class::isInstance)
            .allSatisfy(child -> assertThat(((ChildB) child).getValue()).isNotNull());
    }
}
