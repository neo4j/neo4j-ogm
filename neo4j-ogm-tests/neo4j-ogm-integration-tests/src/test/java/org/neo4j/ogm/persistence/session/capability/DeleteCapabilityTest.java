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
package org.neo4j.ogm.persistence.session.capability;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.domain.music.Recording;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author vince
 */
public class DeleteCapabilityTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.music");
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void shouldNotFailIfDeleteNodeEntityAgainstEmptyDatabase() {
        session.deleteAll(Album.class);
    }

    @Test
    public void shouldNotFailIfDeleteRelationshipEntityAgainstEmptyDatabase() {
        session.deleteAll(Recording.class);
    }

    @Test
    public void canDeleteSingleEntry() {
        Album album = new Album();
        session.save(album);
        assertEntityCount(1);

        session.delete(album);
        assertEntityCount(0);
    }

    @Test
    public void canDeleteEntityCollection() {
        Album album1 = new Album();
        Album album2 = new Album();
        session.save(album1);
        session.save(album2);
        assertEntityCount(2);

        List<Object> albumList = new ArrayList<>();
        albumList.add(album1);
        albumList.add(album2);

        session.delete(albumList);
        assertEntityCount(0);
    }

    @Test // GH-509
    public void canDeleteEntityArray() {
        Album album1 = new Album();
        Album album2 = new Album();
        session.save(album1);
        session.save(album2);
        assertEntityCount(2);

        List<Object> albumList = new ArrayList<>();
        albumList.add(album1);
        albumList.add(album2);

        session.delete(albumList.toArray());
        assertEntityCount(0);
    }

    private void assertEntityCount(int count) {
        session.clear(); // Ensure that no data is cached...
        long entityCount = session.countEntitiesOfType(Album.class);
        assertThat(entityCount).isEqualTo(count);
        session.clear(); // ...also for the subsequent calls in the test methods
    }
}
