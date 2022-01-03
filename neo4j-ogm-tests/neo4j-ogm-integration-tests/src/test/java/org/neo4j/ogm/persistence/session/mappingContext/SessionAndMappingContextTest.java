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
package org.neo4j.ogm.persistence.session.mappingContext;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.context.MappingContext;
import org.neo4j.ogm.domain.cineasts.annotated.Actor;
import org.neo4j.ogm.domain.cineasts.annotated.Knows;
import org.neo4j.ogm.domain.gh817.Bike;
import org.neo4j.ogm.domain.gh817.Rider;
import org.neo4j.ogm.domain.gh817.Trip;
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.domain.music.Artist;
import org.neo4j.ogm.domain.music.Recording;
import org.neo4j.ogm.domain.music.ReleaseFormat;
import org.neo4j.ogm.domain.music.Studio;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author Mihai Raulea
 * @author Michael J. Simons
 */
public class SessionAndMappingContextTest extends TestContainersTestBase {

    // i need a Neo4jSession because the session interface does not define the context() method
    private Neo4jSession session;

    private Album album1;
    private Album album2;
    private Album album3;
    private Artist artist1;
    private ReleaseFormat releaseFormat;

    private Actor actor1;
    private Actor actor2;
    private Knows knows;
    private Knows knows2;

    private static SessionFactory sessionFactory;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.music",
            "org.neo4j.ogm.domain.cineasts.annotated", "org.neo4j.ogm.domain.gh817");
    }

    @Before
    public void init() throws IOException {
        session = (Neo4jSession) sessionFactory.openSession();

        artist1 = new Artist();
        artist1.setName("MainArtist");

        Artist artist2 = new Artist();
        artist2.setName("GuestArtist");

        album1 = new Album();
        album1.setName("First");
        album1.setGuestArtist(artist2);

        album2 = new Album();
        album2.setName("Second");

        album3 = new Album();
        album3.setName("Third");

        artist1.addAlbum(album1);
        artist1.addAlbum(album2);
        artist1.addAlbum(album3);

        Studio studio = new Studio();
        studio.setName("Studio");

        Recording recording = new Recording();
        recording.setAlbum(album1);
        recording.setAlbum(album2);
        recording.setAlbum(album3);
        recording.setStudio(studio);
        recording.setYear(2001);

        session.save(artist1);

        actor1 = new Actor("Actor1");
        actor2 = new Actor("Actor2");
        knows = new Knows();
        knows.setFirstActor(actor1);
        knows.setSecondActor(actor2);
        actor1.knows.add(knows);
        session.save(actor1);

        Actor actor3 = new Actor("Actor3");
        Actor actor4 = new Actor("Actor4");
        knows2 = new Knows();
        knows2.setFirstActor(actor3);
        knows2.setSecondActor(actor4);
        actor3.knows.add(knows2);
        session.save(actor3);
    }

    @After
    public void teardown() {
        session.purgeDatabase();
    }

    @Test
    public void disposeFromMappingContextOnDeleteWithTransientRelationshipTest() {
        MappingContext mappingContext = session.context();
        assertThat(mappingContext.getNodeEntity(artist1.getId()).getClass() == Artist.class).isTrue();
        session.delete(artist1);

        // check that the mapping context does not hold a reference to the deleted entity anymore
        Object object = mappingContext.getNodeEntity(artist1.getId());
        assertThat(object == null).isTrue();

        // check that objects with references to the deleted object have been cleared
        // check for TransientRelationship, where the object connected to the deleted object holds ref in a Set
        Album retrievedAlbum1 = (Album) mappingContext.getNodeEntity(album1.getId());
        assertThat(retrievedAlbum1.getArtist() == null).isTrue();

        Album retrievedAlbum2 = (Album) mappingContext.getNodeEntity(album2.getId());
        assertThat(retrievedAlbum2.getArtist() == null).isTrue();

        Album retrievedAlbum3 = (Album) mappingContext.getNodeEntity(album3.getId());
        assertThat(retrievedAlbum3.getArtist() == null).isTrue();
    }

    @Test
    public void disposeFromMappingContextOnDeleteWithRelationshipEntityTest() {
        long actorId = session.context().nativeId(actor1);

        assertThat(session.context().getNodeEntity(actorId).getClass() == Actor.class).isTrue();
        Object objectRel = session.context().getRelationshipEntity(knows.id);
        assertThat(objectRel.getClass() == Knows.class).isTrue();

        session.delete(actor1);

        Result result = session.query("MATCH (n) RETURN n", Collections.EMPTY_MAP);
        // check that the mapping context does not hold a reference to the deleted entity anymore
        Object object = session.context().getNodeEntity(actorId);
        assertThat(object == null).isTrue();
        // check for a defined RelationshipEntity; the relationship should also be removed from the mappingContext
        objectRel = session.context().getRelationshipEntity(knows.id);
        assertThat(objectRel == null).isTrue();
        assertThat(session.context().getNodeEntity(actorId) == null).isTrue();
        // does it exist in the session?
        Knows inSessionKnows = session.load(Knows.class, knows.id);
        assertThat(inSessionKnows == null).isTrue();
        // the other knows relationship should not have been deleted
        Knows inSessionKnows2 = session.load(Knows.class, knows2.id);
        assertThat(inSessionKnows2 != null).isTrue();
    }

    @Test
    public void testEntityRelationshipProperlyRemoved() {
        session.delete(knows);
        Knows testKnows = session.load(Knows.class, knows.id);
        assertThat(testKnows == null).isTrue();
    }

    @Test
    public void testDetachNode() {
        long actorId = session.context().nativeId(actor1);
        assertThat(session.detachNodeEntity(actorId)).isTrue();
        assertThat(session.detachNodeEntity(actorId)).isFalse();
    }

    @Test
    public void testDetachNode2() {
        long actorId = session.context().nativeId(actor2);
        assertThat(session.detachNodeEntity(actorId)).isTrue();
        assertThat(session.detachNodeEntity(actorId)).isFalse();
    }

    @Test
    public void testDetachRelationshipEntity() {
        assertThat(session.detachRelationshipEntity(knows.id)).isTrue();
        assertThat(session.detachRelationshipEntity(knows.id)).isFalse();
    }

    @Test
    public void shouldRollbackRelationshipEntityWithDifferentStartAndEndNodes() {

        Actor mary = new Actor("Mary");
        Actor john = new Actor("John");

        Knows maryKnowsJohn = new Knows();

        maryKnowsJohn.setFirstActor(mary);
        maryKnowsJohn.setSecondActor(john);

        try (Transaction tx = session.beginTransaction()) {

            session.save(maryKnowsJohn);

            assertThat(mary.getUuid()).isNotNull();
            assertThat(maryKnowsJohn.id).isNotNull();
            assertThat(john.getUuid()).isNotNull();

            tx.rollback();

            assertThat(mary.getUuid()).isNull();
            assertThat(maryKnowsJohn.id).isNull();
            assertThat(john.getUuid()).isNull();
        }
    }

    @Test
    public void shouldNotThrowConcurrentModificationException() {

        try (Transaction tx = session.beginTransaction()) {
            session.save(new Actor("Mary"));
            session.deleteAll(Actor.class);
        }
    }

    @Test // GH-817
    public void shouldRefreshUpdatedEntities() {
        sessionFactory.openSession().purgeDatabase();

        Session sessionForCreation = sessionFactory.openSession();
        try (Transaction tx = sessionForCreation.beginTransaction()) {
            Stream.of(new Bike("Bike1"), new Bike("Bike2")).forEach(sessionForCreation::save);
            tx.commit();
        }

        Session sessionForLoadingAndUpdate = sessionFactory.openSession();
        Iterable<Bike> loadedBikes = sessionForLoadingAndUpdate.loadAll(Bike.class);
        assertThat(loadedBikes).hasSize(2).extracting(Bike::isDamaged).containsOnly(false);

        Iterable<Bike> updatedBikes = sessionForLoadingAndUpdate
            .query(Bike.class, "MATCH (c:Bike) SET c.damaged = true RETURN c", Collections.emptyMap());

        assertThat(updatedBikes).hasSize(2).extracting(Bike::isDamaged).containsOnly(true);
    }

    @Test // GH-817
    public void shouldFlushSessionWithoutReturningNodes() {
        sessionFactory.openSession().purgeDatabase();

        Session sessionForCreation = sessionFactory.openSession();
        try (Transaction tx = sessionForCreation.beginTransaction()) {
            Stream.of(new Bike("Bike1"), new Bike("Bike2")).forEach(sessionForCreation::save);
            tx.commit();
        }

        Session sessionForLoadingAndUpdate = sessionFactory.openSession();
        Iterable<Bike> loadedBikes = sessionForLoadingAndUpdate.loadAll(Bike.class);
        assertThat(loadedBikes).hasSize(2).extracting(Bike::isDamaged).containsOnly(false);

        sessionForLoadingAndUpdate.query("MATCH (c:Bike) SET c.damaged = true", Collections.emptyMap());

        loadedBikes = sessionForLoadingAndUpdate.loadAll(Bike.class);
        assertThat(loadedBikes).hasSize(2).extracting(Bike::isDamaged).containsOnly(true);
    }

    @Test // GH-817
    public void shouldRefreshUpdatedRelationshipEntities() {
        sessionFactory.openSession().purgeDatabase();

        Session sessionForCreation = sessionFactory.openSession();
        Rider rider = new Rider("Michael");
        Bike bike1 = new Bike("Bike1");
        Bike bike2 = new Bike("Bike2");
        rider.getTrips().add(new Trip(rider, bike1, "n/a"));
        rider.getTrips().add(new Trip(rider, bike2, "n/a"));
        try (Transaction tx = sessionForCreation.beginTransaction()) {
            sessionForCreation.save(rider);
            tx.commit();
        }

        Session sessionForLoadingAndUpdate = sessionFactory.openSession();
        Iterable<Trip> loadedBikes = sessionForLoadingAndUpdate.loadAll(Trip.class);
        assertThat(loadedBikes).hasSize(2).extracting(Trip::getName).containsOnly("n/a");

        String name = "A nice trip";
        Iterable<Trip> updatedBikes = sessionForLoadingAndUpdate
            .query(Trip.class, "MATCH (r:Rider) - [t:RODE] -> (c:Bike) SET t.name = $newName RETURN *", Collections.singletonMap("newName", name));

        Rider loadedRider = sessionForLoadingAndUpdate.load(Rider.class, rider.getId());

        assertThat(updatedBikes).hasSize(2).extracting(Trip::getName).containsOnly(name);
        assertThat(loadedRider.getTrips()).hasSize(2).extracting(Trip::getName).containsOnly(name);
    }
}
