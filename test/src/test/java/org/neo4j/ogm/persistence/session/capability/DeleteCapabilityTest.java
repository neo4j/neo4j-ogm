/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author vince
 */
public class DeleteCapabilityTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.music");
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
