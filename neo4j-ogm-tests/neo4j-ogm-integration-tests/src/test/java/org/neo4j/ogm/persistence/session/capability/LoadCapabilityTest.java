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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.context.MappingContext;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.domain.education.DomainObject;
import org.neo4j.ogm.domain.education.School;
import org.neo4j.ogm.domain.education.Student;
import org.neo4j.ogm.domain.gh368.User;
import org.neo4j.ogm.domain.gh787.EntityWithCustomIdConverter;
import org.neo4j.ogm.domain.gh787.MyVeryOwnIdType;
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.domain.music.Artist;
import org.neo4j.ogm.domain.music.Recording;
import org.neo4j.ogm.domain.music.Studio;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class LoadCapabilityTest extends TestContainersTestBase {

    private SessionFactory sessionFactory;
    private Session session;
    private Long pleaseId;
    private Long beatlesId;

    @Before
    public void init() throws IOException {

        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.music", "org.neo4j.ogm.domain.gh368");
        session = sessionFactory.openSession();
        session.purgeDatabase();

        //Create some data
        Artist theBeatles = new Artist("The Beatles");
        Album please = new Album("Please Please Me");
        theBeatles.getAlbums().add(please);
        please.setArtist(theBeatles);
        session.save(theBeatles);

        pleaseId = please.getId();
        beatlesId = theBeatles.getId();
    }

    @After
    public void clearDatabase() {
        session.purgeDatabase();
    }

    @Test // DATAGRAPH-707
    public void loadAllShouldRespectEntityType() {
        Collection<Artist> artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId));
        assertThat(artists).hasSize(1);
        assertThat(artists.iterator().next().getName()).isEqualTo("The Beatles");

        Collection<Album> albums = session.loadAll(Album.class, Collections.singletonList(beatlesId));
        assertThat(albums).isEmpty();

        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), 0);
        assertThat(artists).hasSize(1);
        assertThat(artists.iterator().next().getName()).isEqualTo("The Beatles");

        albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), 0);
        assertThat(albums).isEmpty();

        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new SortOrder().add("name"));
        assertThat(artists).hasSize(1);
        assertThat(artists.iterator().next().getName()).isEqualTo("The Beatles");

        albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new SortOrder().add("name"));
        assertThat(albums).isEmpty();

        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new SortOrder().add("name"), 0);
        assertThat(artists).hasSize(1);
        assertThat(artists.iterator().next().getName()).isEqualTo("The Beatles");

        albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new SortOrder().add("name"), 0);
        assertThat(albums).isEmpty();

        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new Pagination(0, 5));
        assertThat(artists).hasSize(1);
        assertThat(artists.iterator().next().getName()).isEqualTo("The Beatles");

        albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new Pagination(0, 5));
        assertThat(albums).isEmpty();

        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new Pagination(0, 5), 0);
        assertThat(artists).hasSize(1);
        assertThat(artists.iterator().next().getName()).isEqualTo("The Beatles");

        albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new Pagination(0, 5), 0);
        assertThat(albums).isEmpty();

        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new SortOrder().add("name"),
            new Pagination(0, 5));
        assertThat(artists).hasSize(1);
        assertThat(artists.iterator().next().getName()).isEqualTo("The Beatles");

        albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new SortOrder().add("name"),
            new Pagination(0, 5));
        assertThat(albums).isEmpty();

        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new SortOrder().add("name"),
            new Pagination(0, 5), 0);
        assertThat(artists).hasSize(1);
        assertThat(artists.iterator().next().getName()).isEqualTo("The Beatles");

        Artist bonJovi = new Artist("Bon Jovi");
        session.save(bonJovi);

        artists = session
            .loadAll(Artist.class, Arrays.asList(beatlesId, pleaseId, bonJovi.getId()), new SortOrder().add("name"),
                new Pagination(0, 5), 0);
        assertThat(artists).hasSize(2);

        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new SortOrder().add("name"),
            new Pagination(0, 5), 0);
        assertThat(artists).hasSize(1);
        assertThat(artists.iterator().next().getName())
            .isEqualTo("The Beatles"); //make sure Bon Jovi isn't returned as well

        albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new SortOrder().add("name"),
            new Pagination(0, 5), 0);
        assertThat(albums).isEmpty();
    }

    @Test // DATAGRAPH-707
    public void loadOneShouldRespectEntityType() {
        Artist artist = session.load(Artist.class, beatlesId);
        assertThat(artist.getName()).isEqualTo("The Beatles");

        Album album = session.load(Album.class, beatlesId);
        assertThat(album).isNull();

        artist = session.load(Artist.class, beatlesId, 0);
        assertThat(artist.getName()).isEqualTo("The Beatles");

        album = session.load(Album.class, beatlesId, 0);
        assertThat(album).isNull();

        artist = session.load(Artist.class, Long.MAX_VALUE); //ID does not exist
        assertThat(artist).isNull();
    }

    @Test // GH-170
    public void shouldBeAbleToLoadEntitiesToDifferentDepthsInDifferentSessions() {
        Artist pinkFloyd = new Artist("Pink Floyd");
        Album divisionBell = new Album("The Division Bell");
        divisionBell.setArtist(pinkFloyd);
        Studio studio = new Studio("Britannia Row Studios");
        Recording recording = new Recording(divisionBell, studio, 1994);
        divisionBell.setRecording(recording);
        pinkFloyd.addAlbum(divisionBell);
        session.save(pinkFloyd);
        session.clear();

        //Load Pink Floyd to depth 1 in a new session
        Session session1 = sessionFactory.openSession();
        Artist pinkfloyd1 = session1.load(Artist.class, pinkFloyd.getId(), 1);
        assertThat(pinkfloyd1).isNotNull();
        assertThat(pinkfloyd1.getAlbums()).hasSize(1);
        assertThat(pinkfloyd1.getAlbums().iterator().next().getRecording()).isNull();

        //Load Pink Floyd to depth -1 in a new session
        Session session2 = sessionFactory.openSession();
        Artist pinkfloyd2 = session2.load(Artist.class, pinkFloyd.getId(), -1);
        assertThat(pinkfloyd2).isNotNull();
        assertThat(pinkfloyd2.getAlbums()).hasSize(1);
        assertThat(pinkfloyd2.getAlbums().iterator().next().getRecording()).isNotNull();

        //Load Pink Floyd to depth -1 in an existing session which has loaded it to depth 1 previously
        Artist pinkfloyd_1_1 = session1.load(Artist.class, pinkFloyd.getId(), -1);
        assertThat(pinkfloyd_1_1).isNotNull();
        assertThat(pinkfloyd_1_1.getAlbums()).hasSize(1);
        assertThat(pinkfloyd2.getAlbums().iterator().next().getRecording()).isNotNull();
    }

    @Test
    public void shouldNotRefreshPropertiesOnEntityReload() {
        Artist pinkFloyd = new Artist("Pink Floyd");
        session.save(pinkFloyd);
        session.clear();

        //Load Pink Floyd in a new session, session1
        Session session1 = sessionFactory.openSession();
        Artist pinkfloyd1 = session1.load(Artist.class, pinkFloyd.getId(), 1);
        assertThat(pinkfloyd1).isNotNull();
        assertThat(pinkfloyd1.getName()).isEqualTo("Pink Floyd");

        //Load Pink Floyd to in another new session, session2
        Session session2 = sessionFactory.openSession();
        Artist pinkfloyd2 = session2.load(Artist.class, pinkFloyd.getId(), -1);
        assertThat(pinkfloyd2).isNotNull();
        assertThat(pinkfloyd2.getName()).isEqualTo("Pink Floyd");
        //update the name property
        pinkfloyd2.setName("Purple Floyd");
        //and save it in session2. Now the name in the graph is Purple Floyd
        session2.save(pinkfloyd2);

        //Reload Pink Floyd in session1
        Artist pinkfloyd_1_1 = session1.load(Artist.class, pinkFloyd.getId(), -1);
        assertThat(pinkfloyd_1_1).isNotNull();
        assertThat(pinkfloyd_1_1.getName()).isEqualTo("Pink Floyd"); //the name should be refreshed from the graph
    }

    @Test // GH-177
    public void shouldNotBeDirtyOnLoadEntityThenSaveThenReload() {

        MappingContext context = ((Neo4jSession) session).context();

        Artist pinkFloyd = new Artist("Pink Floyd");
        assertThat(context.isDirty(pinkFloyd)).isTrue();     // new object not saved is always dirty

        session.save(pinkFloyd);
        assertThat(context.isDirty(pinkFloyd)).isFalse();    // object hash updated by save.

        session.clear();                            // forget everything we've done

        pinkFloyd = session.load(Artist.class, pinkFloyd.getId());
        assertThat(context.isDirty(pinkFloyd)).isFalse();    // a freshly loaded object is never dirty

        pinkFloyd.setName("Purple Floyd");
        assertThat(context.isDirty(pinkFloyd)).isTrue();     // we changed the name so it is now dirty

        session.save(pinkFloyd);
        assertThat(context.isDirty(pinkFloyd)).isFalse();    // object saved, no longer dirty

        Artist purpleFloyd = session.load(Artist.class, pinkFloyd.getId()); // load the same identity, but to a copy ref
        assertThat(context.isDirty(purpleFloyd)).isFalse();  // nothing has changed, so it should not be dirty

        assertThat(pinkFloyd == purpleFloyd).isTrue();       // two refs pointing to the same object
    }

    @Test // GH-177
    public void shouldNotBeDirtyOnLoadRelationshipEntityThenSaveThenReload() {

        MappingContext context = ((Neo4jSession) session).context();

        Artist pinkFloyd = new Artist("Pink Floyd");
        Album divisionBell = new Album("The Division Bell");
        divisionBell.setArtist(pinkFloyd);
        Studio studio = new Studio("Britannia Row Studios");
        Recording recording = new Recording(divisionBell, studio, 1994);
        divisionBell.setRecording(recording);
        pinkFloyd.addAlbum(divisionBell);

        assertThat(context.isDirty(recording)).isTrue();     // new object not saved is always dirty

        session.save(recording);
        assertThat(context.isDirty(recording)).isFalse();    // object hash updated by save.

        session.clear();                            // forget everything we've done

        recording = session.load(Recording.class, recording.getId(), 2);
        assertThat(context.isDirty(recording)).isFalse();    // a freshly loaded object is never dirty

        recording.setYear(1995);
        assertThat(context.isDirty(recording)).isTrue();     // we changed the year so it is now dirty

        session.save(recording);
        assertThat(context.isDirty(recording)).isFalse();    // object saved, no longer dirty

        Recording recording1995 = session
            .load(Recording.class, recording.getId(), 2); // load the same identity, but to a copy ref
        assertThat(context.isDirty(recording1995)).isFalse();  // nothing has changed, so it should not be dirty

        assertThat(recording == recording1995).isTrue();       // two refs pointing to the same object
    }

    @Test // DATAGRAPH-642, GH-174
    public void shouldRetainPreviouslyLoadedRelationshipsWhenDepthIsReduced() {
        Artist led = new Artist("Led Zeppelin");
        Album album = new Album("Led Zeppelin IV");
        Studio studio = new Studio("Island Studios");
        Recording recording = new Recording(album, studio, 1970);
        led.addAlbum(album);
        album.setArtist(led);
        album.setRecording(recording);
        session.save(led);

        session.clear();

        //Load Artist to depth 2
        Artist ledZeppelin = session.load(Artist.class, led.getId(), 2);
        assertThat(ledZeppelin).isNotNull();
        assertThat(ledZeppelin.getName()).isEqualTo(led.getName());
        assertThat(ledZeppelin.getAlbums()).hasSize(1);
        Album ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertThat(ledZeppelinIV.getName()).isEqualTo(album.getName());
        assertThat(ledZeppelinIV.getArtist()).isEqualTo(ledZeppelin);
        assertThat(ledZeppelinIV.getRecording()).isNotNull();
        assertThat(ledZeppelinIV.getRecording().getStudio().getName()).isEqualTo(studio.getName());

        //Now load to depth 1
        ledZeppelin = session.load(Artist.class, led.getId(), 0);
        assertThat(ledZeppelin).isNotNull();
        assertThat(ledZeppelin.getName()).isEqualTo(led.getName());
        assertThat(ledZeppelin.getAlbums()).hasSize(1);
        ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertThat(ledZeppelinIV.getName()).isEqualTo(album.getName());
        assertThat(ledZeppelinIV.getArtist()).isEqualTo(ledZeppelin);
        assertThat(ledZeppelinIV.getRecording()).isNotNull();
        assertThat(ledZeppelinIV.getRecording().getStudio().getName()).isEqualTo(studio.getName());

        //Now load to depth 0
        ledZeppelin = session.load(Artist.class, led.getId(), 0);
        assertThat(ledZeppelin).isNotNull();
        assertThat(ledZeppelin.getName()).isEqualTo(led.getName());
        assertThat(ledZeppelin.getAlbums()).hasSize(1);
        ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertThat(ledZeppelinIV.getName()).isEqualTo(album.getName());
        assertThat(ledZeppelinIV.getArtist()).isEqualTo(ledZeppelin);
        assertThat(ledZeppelinIV.getRecording()).isNotNull();
        assertThat(ledZeppelinIV.getRecording().getStudio().getName()).isEqualTo(studio.getName());
    }

    @Test // DATAGRAPH-642, GH-174
    public void shouldAddRelationshipsWhenDepthIsIncreased() {
        Artist led = new Artist("Led Zeppelin");
        Album album = new Album("Led Zeppelin IV");
        Studio studio = new Studio("Island Studios");
        Recording recording = new Recording(album, studio, 1970);
        led.addAlbum(album);
        album.setArtist(led);
        album.setRecording(recording);
        session.save(led);

        session.clear();

        //Load to depth 0
        Artist ledZeppelin = session.load(Artist.class, led.getId(), 0);
        assertThat(ledZeppelin).isNotNull();
        assertThat(ledZeppelin.getName()).isEqualTo(led.getName());
        assertThat(ledZeppelin.getAlbums()).isEmpty();

        //Load to depth 1
        ledZeppelin = session.load(Artist.class, led.getId(), 1);
        assertThat(ledZeppelin).isNotNull();
        assertThat(ledZeppelin.getName()).isEqualTo(led.getName());
        assertThat(ledZeppelin.getAlbums()).hasSize(1);
        Album ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertThat(ledZeppelinIV.getName()).isEqualTo(album.getName());
        assertThat(ledZeppelinIV.getArtist()).isEqualTo(ledZeppelin);
        assertThat(ledZeppelinIV.getRecording()).isNull();

        //Load to depth 2
        ledZeppelin = session.load(Artist.class, led.getId(), 2);
        assertThat(ledZeppelin).isNotNull();
        assertThat(ledZeppelin.getName()).isEqualTo(led.getName());
        assertThat(ledZeppelin.getAlbums()).hasSize(1);
        ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertThat(ledZeppelinIV.getName()).isEqualTo(album.getName());
        assertThat(ledZeppelinIV.getArtist()).isEqualTo(ledZeppelin);
        assertThat(ledZeppelinIV.getRecording()).isNotNull();
        assertThat(ledZeppelinIV.getRecording().getStudio().getName()).isEqualTo(studio.getName());
    }

    @Test // GH-173
    public void shouldNotModifyPreviouslyLoadedNodesWhenDepthIsIncreased() {
        Artist led = new Artist("Led Zeppelin");
        Album album = new Album("Led Zeppelin IV");
        Studio studio = new Studio("Island Studios");
        Recording recording = new Recording(album, studio, 1970);
        led.addAlbum(album);
        album.setArtist(led);
        album.setRecording(recording);
        session.save(led);

        session.clear();

        //Load the Artist to depth 0 in the first session
        Artist ledZeppelin = session.load(Artist.class, led.getId(), 0);
        assertThat(ledZeppelin).isNotNull();
        assertThat(ledZeppelin.getName()).isEqualTo(led.getName());
        assertThat(ledZeppelin.getAlbums()).isEmpty();

        //In session 2, update the name of the artist
        Session session2 = sessionFactory.openSession();
        Artist ledZepp = session2.load(Artist.class, led.getId(), 0);
        ledZepp.setName("Led Zepp");
        session2.save(ledZepp);

        //Back in the first session, load the artist to depth 1
        ledZeppelin = session.load(Artist.class, led.getId(), 1);
        assertThat(ledZeppelin).isNotNull();
        assertThat(ledZeppelin.getName()).isEqualTo(led.getName());
        assertThat(ledZeppelin.getAlbums()).hasSize(1);
        Album ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertThat(ledZeppelinIV.getName()).isEqualTo(album.getName());
        assertThat(ledZeppelinIV.getArtist()).isEqualTo(ledZeppelin);
        assertThat(ledZeppelinIV.getRecording()).isNull();
    }

    @Test // GH-173
    public void shouldNotModifyPreviouslyLoadedNodesWhenDepthIsReduced() {
        Artist led = new Artist("Led Zeppelin");
        Album album = new Album("Led Zeppelin IV");
        Studio studio = new Studio("Island Studios");
        Recording recording = new Recording(album, studio, 1970);
        led.addAlbum(album);
        album.setArtist(led);
        album.setRecording(recording);
        session.save(led);

        session.clear();

        //Load the Artist to depth 1 in the first session
        Artist ledZeppelin = session.load(Artist.class, led.getId(), 1);
        assertThat(ledZeppelin).isNotNull();
        assertThat(ledZeppelin.getName()).isEqualTo(led.getName());
        assertThat(ledZeppelin.getAlbums()).hasSize(1);
        Album ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertThat(ledZeppelinIV.getName()).isEqualTo(album.getName());
        assertThat(ledZeppelinIV.getArtist()).isEqualTo(ledZeppelin);
        assertThat(ledZeppelinIV.getRecording()).isNull();

        //In session 2, update the name of the artist
        Session session2 = sessionFactory.openSession();
        Artist ledZepp = session2.load(Artist.class, led.getId(), 0);
        ledZepp.setName("Led Zepp");
        session2.save(ledZepp);

        //Back in the first session, load the artist to depth 0
        ledZeppelin = session.load(Artist.class, led.getId(), 0);
        assertThat(ledZeppelin).isNotNull();
        assertThat(ledZeppelin.getName()).isEqualTo(led.getName());
        assertThat(ledZeppelin.getAlbums()).hasSize(1);
    }

    @Test // DATAGRAPH-642, GH-174
    public void shouldBeAbleToLoadEntityToDifferentDepthsInDifferentSessions() {
        Artist led = new Artist("Led Zeppelin");
        Album album = new Album("Led Zeppelin IV");
        Studio studio = new Studio("Island Studios");
        Recording recording = new Recording(album, studio, 1970);
        led.addAlbum(album);
        album.setArtist(led);
        album.setRecording(recording);
        session.save(led);

        session.clear();

        //In the first session, load the artist to depth 2
        Artist ledZeppelin = session.load(Artist.class, led.getId(), 2);
        assertThat(ledZeppelin).isNotNull();
        assertThat(ledZeppelin.getName()).isEqualTo(led.getName());
        assertThat(ledZeppelin.getAlbums()).hasSize(1);
        Album ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertThat(ledZeppelinIV.getName()).isEqualTo(album.getName());
        assertThat(ledZeppelinIV.getArtist()).isEqualTo(ledZeppelin);
        assertThat(ledZeppelinIV.getRecording()).isNotNull();
        assertThat(ledZeppelinIV.getRecording().getStudio().getName()).isEqualTo(studio.getName());

        //In the second session, load the artist to depth 0
        Session session2 = sessionFactory.openSession();
        Artist ledZeppelin0 = session2.load(Artist.class, led.getId(), 0);
        assertThat(ledZeppelin0).isNotNull();
        assertThat(ledZeppelin0.getName()).isEqualTo(led.getName());
        assertThat(ledZeppelin0.getAlbums()).isEmpty();
    }

    @Test // GH-173
    public void shouldNotModifyPreviouslyLoadedNodesWhenEntityIsReloaded() {
        Artist led = new Artist("Led Zeppelin");
        Album album = new Album("Led Zeppelin IV");
        Studio studio = new Studio("Island Studios");
        Recording recording = new Recording(album, studio, 1970);
        led.addAlbum(album);
        album.setArtist(led);
        album.setRecording(recording);
        session.save(led);

        session.clear();

        //Load the Artist to depth 1 in the first session
        Artist ledZeppelin = session.load(Artist.class, led.getId(), 1);
        assertThat(ledZeppelin).isNotNull();
        assertThat(ledZeppelin.getName()).isEqualTo(led.getName());
        assertThat(ledZeppelin.getAlbums()).hasSize(1);
        Album ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertThat(ledZeppelinIV.getName()).isEqualTo(album.getName());
        assertThat(ledZeppelinIV.getArtist()).isEqualTo(ledZeppelin);
        assertThat(ledZeppelinIV.getRecording()).isNull();

        //In session 2, update the name of the artist
        Session session2 = sessionFactory.openSession();
        Artist ledZepp = session2.load(Artist.class, led.getId(), 0);
        ledZepp.setName("Led Zepp");
        session2.save(ledZepp);

        //Reload the artist in the first session
        ledZeppelin = session.load(Artist.class, led.getId(), 1);
        assertThat(ledZeppelin).isNotNull();
        assertThat(ledZeppelin.getName()).isEqualTo(led.getName());
        assertThat(ledZeppelin.getAlbums()).hasSize(1);
        ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertThat(ledZeppelinIV.getName()).isEqualTo(album.getName());
        assertThat(ledZeppelinIV.getArtist()).isEqualTo(ledZeppelin);
        assertThat(ledZeppelinIV.getRecording()).isNull();
    }

    @Test // GH-173
    public void shouldMapNewNodesAndRelationshipsWhenEntityIsReloaded() {
        Artist led = new Artist("Led Zeppelin");
        Album album = new Album("Led Zeppelin IV");
        Studio studio = new Studio("Island Studios");
        Recording recording = new Recording(album, studio, 1970);
        led.addAlbum(album);
        album.setArtist(led);
        album.setRecording(recording);
        session.save(led);

        session.clear();

        //Load the Artist to depth 2 in the first session
        Artist ledZeppelin = session.load(Artist.class, led.getId(), 2);
        assertThat(ledZeppelin).isNotNull();
        assertThat(ledZeppelin.getName()).isEqualTo(led.getName());
        assertThat(ledZeppelin.getAlbums()).hasSize(1);
        Album ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertThat(ledZeppelinIV.getName()).isEqualTo(album.getName());
        assertThat(ledZeppelinIV.getArtist()).isEqualTo(ledZeppelin);
        assertThat(ledZeppelinIV.getRecording()).isNotNull();
        assertThat(ledZeppelinIV.getRecording().getStudio().getName()).isEqualTo(studio.getName());

        //In session 2, add an album and recording
        Session session2 = sessionFactory.openSession();
        Artist ledZepp = session2.load(Artist.class, led.getId());
        Album houses = new Album("Houses of the Holy");
        Studio studio2 = new Studio("Island Studios");
        Recording housesRec = new Recording(houses, studio2, 1972);
        ledZepp.addAlbum(houses);
        houses.setArtist(ledZepp);
        houses.setRecording(housesRec);
        session2.save(ledZepp);

        //Reload the artist in the first session
        ledZeppelin = session.load(Artist.class, led.getId(), 2);
        assertThat(ledZeppelin).isNotNull();
        assertThat(ledZeppelin.getName()).isEqualTo(led.getName());
        assertThat(ledZeppelin.getAlbums()).hasSize(2);
        for (Album loadedAlbum : ledZeppelin.getAlbums()) {
            assertThat(loadedAlbum.getArtist()).isEqualTo(ledZeppelin);
            assertThat(loadedAlbum.getRecording()).isNotNull();
            assertThat(loadedAlbum.getRecording().getStudio()).isNotNull();
        }
    }

    @Test // GH-173
    public void shouldRefreshEntityStateWhenReloadedOnCleanSession() {
        Artist led = new Artist("Led Zeppelin");
        Album album = new Album("Led Zeppelin IV");
        Studio studio = new Studio("Island Studios");
        Recording recording = new Recording(album, studio, 1970);
        led.addAlbum(album);
        album.setArtist(led);
        album.setRecording(recording);
        session.save(led);

        session.clear();

        //Load the Artist to depth 1 in the first session
        Artist ledZeppelin = session.load(Artist.class, led.getId(), 1);
        assertThat(ledZeppelin).isNotNull();
        assertThat(ledZeppelin.getName()).isEqualTo(led.getName());
        assertThat(ledZeppelin.getAlbums()).hasSize(1);
        Album ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertThat(ledZeppelinIV.getName()).isEqualTo(album.getName());
        assertThat(ledZeppelinIV.getArtist()).isEqualTo(ledZeppelin);
        assertThat(ledZeppelinIV.getRecording()).isNull();

        //In session 2, update the name of the artist, delete the album
        Session session2 = sessionFactory.openSession();
        Artist ledZepp = session2.load(Artist.class, led.getId(), 1);
        ledZepp.setName("Led Zepp");
        ledZepp.getAlbums().iterator().next().setArtist(null);
        ledZepp.getAlbums().clear();
        session2.save(ledZepp);

        //Reload the artist in a clean session
        session.clear();
        ledZeppelin = session.load(Artist.class, led.getId(), 1);
        assertThat(ledZeppelin).isNotNull();
        assertThat(ledZeppelin.getName()).isEqualTo(ledZepp.getName());
        assertThat(ledZeppelin.getAlbums()).isEmpty();
    }

    @Test // GH-302
    public void shouldMaintainSortOrderWhenLoadingByIds() {
        Artist led = new Artist("Led Zeppelin");
        session.save(led);
        Artist bonJovi = new Artist("Bon Jovi");
        session.save(bonJovi);

        List<Long> ids = Arrays.asList(beatlesId, led.getId(), bonJovi.getId());
        SortOrder sortOrder = new SortOrder();
        sortOrder.add(SortOrder.Direction.ASC, "name");
        Iterable<Artist> artists = session.loadAll(Artist.class, ids, sortOrder);
        assertThat(artists).isNotNull();
        List<String> artistNames = new ArrayList<>();
        for (Artist artist : artists) {
            artistNames.add(artist.getName());
        }
        assertThat(artistNames.get(0)).isEqualTo("Bon Jovi");
        assertThat(artistNames.get(1)).isEqualTo("Led Zeppelin");
        assertThat(artistNames.get(2)).isEqualTo("The Beatles");

        sortOrder = new SortOrder();
        sortOrder.add(SortOrder.Direction.DESC, "name");
        artists = session.loadAll(Artist.class, ids, sortOrder);
        assertThat(artists).isNotNull();
        artistNames = new ArrayList<>();
        for (Artist artist : artists) {
            artistNames.add(artist.getName());
        }
        assertThat(artistNames.get(0)).isEqualTo("The Beatles");
        assertThat(artistNames.get(1)).isEqualTo("Led Zeppelin");
        assertThat(artistNames.get(2)).isEqualTo("Bon Jovi");
    }

    @Test
    public void loadAllByIdsShouldSortByIdsIfSortOrderIsNotProvided() throws Exception {
        Artist beatles = session.load(Artist.class, beatlesId);

        Artist led = new Artist("Led Zeppelin");
        session.save(led);
        Artist bonJovi = new Artist("Bon Jovi");
        session.save(bonJovi);

        Long ledId = led.getId();
        Long bonJoviId = bonJovi.getId();

        Collection<Artist> artists;

        artists = session.loadAll(Artist.class, Arrays.asList(beatlesId, ledId, bonJoviId));
        assertThat(artists).containsExactly(beatles, led, bonJovi);

        artists = session.loadAll(Artist.class, Arrays.asList(ledId, beatlesId, bonJoviId));
        assertThat(artists).containsExactly(led, beatles, bonJovi);

        artists = session.loadAll(Artist.class, Arrays.asList(ledId, bonJoviId, beatlesId));
        assertThat(artists).containsExactly(led, bonJovi, beatles);
    }

    @Test
    public void loadAllByInstancesShouldSortByIdsIfSortOrderIsNotProvided() throws Exception {
        Artist beatles = session.load(Artist.class, beatlesId);

        Artist led = new Artist("Led Zeppelin");
        session.save(led);
        Artist bonJovi = new Artist("Bon Jovi");
        session.save(bonJovi);

        Long ledId = led.getId();
        Long bonJoviId = bonJovi.getId();

        Collection<Artist> artists;

        artists = session.loadAll(Arrays.asList(beatles, led, bonJovi));
        assertThat(artists).containsExactly(beatles, led, bonJovi);

        artists = session.loadAll(Arrays.asList(led, beatles, bonJovi));
        assertThat(artists).containsExactly(led, beatles, bonJovi);

        artists = session.loadAll(Arrays.asList(led, bonJovi, beatles));
        assertThat(artists).containsExactly(led, bonJovi, beatles);
    }

    @Test
    public void loadAllByInstancesShouldLoadAllClasses() {
        SessionFactory sf = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.education");
        Session sessionWithEducationDomain = sf.openSession();

        School school = new School();
        Student student = new Student();
        sessionWithEducationDomain.save(school);
        sessionWithEducationDomain.save(student);

        Collection<DomainObject> loaded = sessionWithEducationDomain.loadAll(Arrays.asList(school, student));
        assertThat(loaded).contains(school, student);
    }

    @Test // GH-368
    public void shouldKeepOrder() {
        User anna = new User("noone@nowhere.com", "Anna", "Doe");
        User bob = new User("noone@nowhere.com", "Bob", "Doe");
        User charlie = new User("noone@nowhere.com", "Charlie", "Doe");

        session.save(charlie);
        session.save(anna);
        session.save(bob);

        anna.setFriends(Collections.singleton(charlie));
        session.save(anna);

        Collection<User> allUsers = session.loadAll(User.class,
            new SortOrder().add("lastName").add("firstName"),
            1);

        assertThat(allUsers)
            .extracting(User::getFirstName)
            .containsExactly("Anna", "Bob", "Charlie");
    }

    @Test // GH-787
    public void shouldLoadSingleEntityWithCustomId() {
        SessionFactory sf = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.gh787");
        Session sessionForIdConverter = sf.openSession();

        MyVeryOwnIdType id = new MyVeryOwnIdType("1234");
        sessionForIdConverter.save(new EntityWithCustomIdConverter(id));

        sessionForIdConverter.clear();

        assertThat(sessionForIdConverter.load(EntityWithCustomIdConverter.class, id)).isNotNull();
    }

    @Test // GH-787
    public void shouldLoadMultipleEntitiesWithCustomId() {
        SessionFactory sf = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.gh787");
        Session sessionForIdConverter = sf.openSession();

        MyVeryOwnIdType id = new MyVeryOwnIdType("1234");
        sessionForIdConverter.save(new EntityWithCustomIdConverter(id));

        sessionForIdConverter.clear();

        assertThat(sessionForIdConverter.loadAll(EntityWithCustomIdConverter.class, Collections.singleton(id))).hasSize(1);
    }
}
