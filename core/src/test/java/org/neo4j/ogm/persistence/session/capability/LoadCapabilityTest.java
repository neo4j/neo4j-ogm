/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.domain.music.Artist;
import org.neo4j.ogm.domain.music.Recording;
import org.neo4j.ogm.domain.music.Studio;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Luanne Misquitta
 */
public class LoadCapabilityTest extends MultiDriverTestClass {

    private SessionFactory sessionFactory;
    private Session session;
    private Long pleaseId;
    private Long beatlesId;

    @Before
    public void init() throws IOException {

        sessionFactory = new SessionFactory("org.neo4j.ogm.domain.music");
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

    /**
     * @see DATAGRAPH-707
     */
    @Test
    public void loadAllShouldRespectEntityType() {
        Collection<Artist> artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId));
        assertEquals(1, artists.size());
        assertEquals("The Beatles", artists.iterator().next().getName());

        Collection<Album> albums = session.loadAll(Album.class, Collections.singletonList(beatlesId));
        assertEquals(0, albums.size());

        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), 0);
        assertEquals(1, artists.size());
        assertEquals("The Beatles", artists.iterator().next().getName());

        albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), 0);
        assertEquals(0, albums.size());


        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new SortOrder().add("name"));
        assertEquals(1, artists.size());
        assertEquals("The Beatles", artists.iterator().next().getName());

        albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new SortOrder().add("name"));
        assertEquals(0, albums.size());


        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new SortOrder().add("name"), 0);
        assertEquals(1, artists.size());
        assertEquals("The Beatles", artists.iterator().next().getName());

        albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new SortOrder().add("name"), 0);
        assertEquals(0, albums.size());


        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new Pagination(0, 5));
        assertEquals(1, artists.size());
        assertEquals("The Beatles", artists.iterator().next().getName());

        albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new Pagination(0, 5));
        assertEquals(0, albums.size());


        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new Pagination(0, 5), 0);
        assertEquals(1, artists.size());
        assertEquals("The Beatles", artists.iterator().next().getName());

        albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new Pagination(0, 5), 0);
        assertEquals(0, albums.size());


        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new SortOrder().add("name"), new Pagination(0, 5));
        assertEquals(1, artists.size());
        assertEquals("The Beatles", artists.iterator().next().getName());

        albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new SortOrder().add("name"), new Pagination(0, 5));
        assertEquals(0, albums.size());


        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new SortOrder().add("name"), new Pagination(0, 5), 0);
        assertEquals(1, artists.size());
        assertEquals("The Beatles", artists.iterator().next().getName());

        Artist bonJovi = new Artist("Bon Jovi");
        session.save(bonJovi);

        artists = session.loadAll(Artist.class, Arrays.asList(beatlesId, pleaseId, bonJovi.getId()), new SortOrder().add("name"), new Pagination(0, 5), 0);
        assertEquals(2, artists.size());

        artists = session.loadAll(Artist.class, Collections.singletonList(beatlesId), new SortOrder().add("name"), new Pagination(0, 5), 0);
        assertEquals(1, artists.size());
        assertEquals("The Beatles", artists.iterator().next().getName()); //make sure Bon Jovi isn't returned as well

        albums = session.loadAll(Album.class, Collections.singletonList(beatlesId), new SortOrder().add("name"), new Pagination(0, 5), 0);
        assertEquals(0, albums.size());
    }


    /**
     * @see DATAGRAPH-707
     */
    @Test
    public void loadOneShouldRespectEntityType() {
        Artist artist = session.load(Artist.class, beatlesId);
        assertEquals("The Beatles", artist.getName());

        Album album = session.load(Album.class, beatlesId);
        assertNull(album);

        artist = session.load(Artist.class, beatlesId, 0);
        assertEquals("The Beatles", artist.getName());

        album = session.load(Album.class, beatlesId, 0);
        assertNull(album);

        artist = session.load(Artist.class, 10l); //ID does not exist
        assertNull(artist);
    }

	/**
     * @see Issue 170
     */
    @Test
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
        assertNotNull(pinkfloyd1);
        assertEquals(1, pinkfloyd1.getAlbums().size());
        assertNull(pinkfloyd1.getAlbums().iterator().next().getRecording());

        //Load Pink Floyd to depth -1 in a new session
        Session session2 = sessionFactory.openSession();
        Artist pinkfloyd2 = session2.load(Artist.class, pinkFloyd.getId(), -1);
        assertNotNull(pinkfloyd2);
        assertEquals(1, pinkfloyd2.getAlbums().size());
        assertNotNull(pinkfloyd2.getAlbums().iterator().next().getRecording());

        //Load Pink Floyd to depth -1 in an existing session which has loaded it to depth 1 previously
        Artist pinkfloyd_1_1 = session1.load(Artist.class, pinkFloyd.getId(), -1);
        assertNotNull(pinkfloyd_1_1);
        assertEquals(1, pinkfloyd_1_1.getAlbums().size());
        assertNotNull(pinkfloyd2.getAlbums().iterator().next().getRecording());
    }

    @Test
    public void shouldNotRefreshPropertiesOnEntityReload() {
        Artist pinkFloyd = new Artist("Pink Floyd");
        session.save(pinkFloyd);
        session.clear();

        //Load Pink Floyd in a new session, session1
        Session session1 = sessionFactory.openSession();
        Artist pinkfloyd1 = session1.load(Artist.class, pinkFloyd.getId(), 1);
        assertNotNull(pinkfloyd1);
        assertEquals("Pink Floyd", pinkfloyd1.getName());

        //Load Pink Floyd to in another new session, session2
        Session session2 = sessionFactory.openSession();
        Artist pinkfloyd2 = session2.load(Artist.class, pinkFloyd.getId(), -1);
        assertNotNull(pinkfloyd2);
        assertEquals("Pink Floyd", pinkfloyd2.getName());
        //update the name property
        pinkfloyd2.setName("Purple Floyd");
        //and save it in session2. Now the name in the graph is Purple Floyd
        session2.save(pinkfloyd2);

        //Reload Pink Floyd in session1
        Artist pinkfloyd_1_1 = session1.load(Artist.class, pinkFloyd.getId(), -1);
        assertNotNull(pinkfloyd_1_1);
        assertEquals("Pink Floyd", pinkfloyd_1_1.getName()); //the name should be refreshed from the graph
    }

    /**
     * @see Issue 177
     */
    @Test
    public void shouldNotBeDirtyOnLoadEntityThenSaveThenReload() {

        MappingContext context = ((Neo4jSession) session).context();

        Artist pinkFloyd = new Artist("Pink Floyd");
        assertTrue(context.isDirty(pinkFloyd));     // new object not saved is always dirty

        //System.out.println("saving new object: pinkFloyd");
        session.save(pinkFloyd);
        assertFalse(context.isDirty(pinkFloyd));    // object hash updated by save.

        session.clear();                            // forget everything we've done

        //System.out.println("reloading object: pinkFloyd");
        pinkFloyd = session.load(Artist.class, pinkFloyd.getId());
        assertFalse(context.isDirty(pinkFloyd));    // a freshly loaded object is never dirty

        pinkFloyd.setName("Purple Floyd");
        assertTrue(context.isDirty(pinkFloyd));     // we changed the name so it is now dirty

        //System.out.println("saving changed object: pinkFloyd->purpleFloyd");
        session.save(pinkFloyd);
        assertFalse(context.isDirty(pinkFloyd));    // object saved, no longer dirty

        //System.out.println("reloading object: purpleFloyd");
        Artist purpleFloyd = session.load(Artist.class, pinkFloyd.getId()); // load the same identity, but to a copy ref
        assertFalse(context.isDirty(purpleFloyd));  // nothing has changed, so it should not be dirty

        assertTrue(pinkFloyd == purpleFloyd);       // two refs pointing to the same object

    }

    /**
     * @see Issue 177
     */
    @Test
    public void shouldNotBeDirtyOnLoadRelationshipEntityThenSaveThenReload() {

        MappingContext context = ((Neo4jSession) session).context();

        Artist pinkFloyd = new Artist("Pink Floyd");
        Album divisionBell = new Album("The Division Bell");
        divisionBell.setArtist(pinkFloyd);
        Studio studio = new Studio("Britannia Row Studios");
        Recording recording = new Recording(divisionBell, studio, 1994);
        divisionBell.setRecording(recording);
        pinkFloyd.addAlbum(divisionBell);

        assertTrue(context.isDirty(recording));     // new object not saved is always dirty

        //System.out.println("saving new object: recording");
        session.save(recording);
        assertFalse(context.isDirty(recording));    // object hash updated by save.

        session.clear();                            // forget everything we've done

        //System.out.println("reloading object: recording");
        recording = session.load(Recording.class, recording.getId(), 2);
        assertFalse(context.isDirty(recording));    // a freshly loaded object is never dirty

        recording.setYear(1995);
        assertTrue(context.isDirty(recording));     // we changed the year so it is now dirty

        //System.out.println("saving changed recording year: 1994->1995");
        session.save(recording);
        assertFalse(context.isDirty(recording));    // object saved, no longer dirty

        //System.out.println("reloading recording as recording1995");
        Recording recording1995 = session.load(Recording.class, recording.getId(), 2); // load the same identity, but to a copy ref
        assertFalse(context.isDirty(recording1995));  // nothing has changed, so it should not be dirty

        assertTrue(recording == recording1995);       // two refs pointing to the same object

    }

	/**
     * @see DATAGRAPH-642, Issue 174
     */
    @Test
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
        assertNotNull(ledZeppelin);
        assertEquals(led.getName(), ledZeppelin.getName());
        assertEquals(1, ledZeppelin.getAlbums().size());
        Album ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertEquals(album.getName(), ledZeppelinIV.getName());
        assertEquals(ledZeppelin, ledZeppelinIV.getArtist());
        assertNotNull(ledZeppelinIV.getRecording());
        assertEquals(studio.getName(), ledZeppelinIV.getRecording().getStudio().getName());

        //Now load to depth 1
        ledZeppelin = session.load(Artist.class, led.getId(), 0);
        assertNotNull(ledZeppelin);
        assertEquals(led.getName(), ledZeppelin.getName());
        assertEquals(1, ledZeppelin.getAlbums().size());
        ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertEquals(album.getName(), ledZeppelinIV.getName());
        assertEquals(ledZeppelin, ledZeppelinIV.getArtist());
        assertNotNull(ledZeppelinIV.getRecording());
        assertEquals(studio.getName(), ledZeppelinIV.getRecording().getStudio().getName());

        //Now load to depth 0
        ledZeppelin = session.load(Artist.class, led.getId(), 0);
        assertNotNull(ledZeppelin);
        assertEquals(led.getName(), ledZeppelin.getName());
        assertEquals(1, ledZeppelin.getAlbums().size());
        ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertEquals(album.getName(), ledZeppelinIV.getName());
        assertEquals(ledZeppelin, ledZeppelinIV.getArtist());
        assertNotNull(ledZeppelinIV.getRecording());
        assertEquals(studio.getName(), ledZeppelinIV.getRecording().getStudio().getName());
    }

    /**
     * @see DATAGRAPH-642, Issue 174
     */
    @Test
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
        assertNotNull(ledZeppelin);
        assertEquals(led.getName(), ledZeppelin.getName());
        assertEquals(0, ledZeppelin.getAlbums().size());

        //Load to depth 1
        ledZeppelin = session.load(Artist.class, led.getId(), 1);
        assertNotNull(ledZeppelin);
        assertEquals(led.getName(), ledZeppelin.getName());
        assertEquals(1, ledZeppelin.getAlbums().size());
        Album ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertEquals(album.getName(), ledZeppelinIV.getName());
        assertEquals(ledZeppelin, ledZeppelinIV.getArtist());
        assertNull(ledZeppelinIV.getRecording());

        //Load to depth 2
        ledZeppelin = session.load(Artist.class, led.getId(), 2);
        assertNotNull(ledZeppelin);
        assertEquals(led.getName(), ledZeppelin.getName());
        assertEquals(1, ledZeppelin.getAlbums().size());
        ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertEquals(album.getName(), ledZeppelinIV.getName());
        assertEquals(ledZeppelin, ledZeppelinIV.getArtist());
        assertNotNull(ledZeppelinIV.getRecording());
        assertEquals(studio.getName(), ledZeppelinIV.getRecording().getStudio().getName());
    }

    /**
     * @see Issue 173
     */
    @Test
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
        assertNotNull(ledZeppelin);
        assertEquals(led.getName(), ledZeppelin.getName());
        assertEquals(0, ledZeppelin.getAlbums().size());

        //In session 2, update the name of the artist
        Session session2 = sessionFactory.openSession();
        Artist ledZepp = session2.load(Artist.class, led.getId(), 0);
        ledZepp.setName("Led Zepp");
        session2.save(ledZepp);

        //Back in the first session, load the artist to depth 1
        ledZeppelin = session.load(Artist.class, led.getId(), 1);
        assertNotNull(ledZeppelin);
        assertEquals(led.getName(), ledZeppelin.getName());
        assertEquals(1, ledZeppelin.getAlbums().size());
        Album ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertEquals(album.getName(), ledZeppelinIV.getName());
        assertEquals(ledZeppelin, ledZeppelinIV.getArtist());
        assertNull(ledZeppelinIV.getRecording());
    }

    /**
     * @see Issue 173
      */
    @Test
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
        assertNotNull(ledZeppelin);
        assertEquals(led.getName(), ledZeppelin.getName());
        assertEquals(1, ledZeppelin.getAlbums().size());
        Album ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertEquals(album.getName(), ledZeppelinIV.getName());
        assertEquals(ledZeppelin, ledZeppelinIV.getArtist());
        assertNull(ledZeppelinIV.getRecording());

        //In session 2, update the name of the artist
        Session session2 = sessionFactory.openSession();
        Artist ledZepp = session2.load(Artist.class, led.getId(), 0);
        ledZepp.setName("Led Zepp");
        session2.save(ledZepp);

        //Back in the first session, load the artist to depth 0
        ledZeppelin = session.load(Artist.class, led.getId(), 0);
        assertNotNull(ledZeppelin);
        assertEquals(led.getName(), ledZeppelin.getName());
        assertEquals(1, ledZeppelin.getAlbums().size());
    }

    /**
     * @see DATAGRAPH-642, Issue 174
     */
    @Test
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
        assertNotNull(ledZeppelin);
        assertEquals(led.getName(), ledZeppelin.getName());
        assertEquals(1, ledZeppelin.getAlbums().size());
        Album ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertEquals(album.getName(), ledZeppelinIV.getName());
        assertEquals(ledZeppelin, ledZeppelinIV.getArtist());
        assertNotNull(ledZeppelinIV.getRecording());
        assertEquals(studio.getName(), ledZeppelinIV.getRecording().getStudio().getName());

        //In the second session, load the artist to depth 0
        Session session2 = sessionFactory.openSession();
        Artist ledZeppelin0 = session2.load(Artist.class, led.getId(), 0);
        assertNotNull(ledZeppelin0);
        assertEquals(led.getName(), ledZeppelin0.getName());
        assertEquals(0, ledZeppelin0.getAlbums().size());

    }

	/**
     * @see Issue 173
     */
    @Test
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
        assertNotNull(ledZeppelin);
        assertEquals(led.getName(), ledZeppelin.getName());
        assertEquals(1, ledZeppelin.getAlbums().size());
        Album ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertEquals(album.getName(), ledZeppelinIV.getName());
        assertEquals(ledZeppelin, ledZeppelinIV.getArtist());
        assertNull(ledZeppelinIV.getRecording());

        //In session 2, update the name of the artist
        Session session2 = sessionFactory.openSession();
        Artist ledZepp = session2.load(Artist.class, led.getId(), 0);
        ledZepp.setName("Led Zepp");
        session2.save(ledZepp);

        //Reload the artist in the first session
        ledZeppelin = session.load(Artist.class, led.getId(), 1);
        assertNotNull(ledZeppelin);
        assertEquals(led.getName(), ledZeppelin.getName());
        assertEquals(1, ledZeppelin.getAlbums().size());
        ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertEquals(album.getName(), ledZeppelinIV.getName());
        assertEquals(ledZeppelin, ledZeppelinIV.getArtist());
        assertNull(ledZeppelinIV.getRecording());
    }

	/**
     * @see Issue 173
     */
    @Test
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
        assertNotNull(ledZeppelin);
        assertEquals(led.getName(), ledZeppelin.getName());
        assertEquals(1, ledZeppelin.getAlbums().size());
        Album ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertEquals(album.getName(), ledZeppelinIV.getName());
        assertEquals(ledZeppelin, ledZeppelinIV.getArtist());
        assertNotNull(ledZeppelinIV.getRecording());
        assertEquals(studio.getName(), ledZeppelinIV.getRecording().getStudio().getName());

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
        assertNotNull(ledZeppelin);
        assertEquals(led.getName(), ledZeppelin.getName());
        assertEquals(2, ledZeppelin.getAlbums().size());
        for (Album loadedAlbum : ledZeppelin.getAlbums()) {
            assertEquals(ledZeppelin, loadedAlbum.getArtist());
            assertNotNull(loadedAlbum.getRecording());
            assertNotNull(loadedAlbum.getRecording().getStudio());
        }
    }

	/**
     * @see Issue 173
     */
    @Test
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
        assertNotNull(ledZeppelin);
        assertEquals(led.getName(), ledZeppelin.getName());
        assertEquals(1, ledZeppelin.getAlbums().size());
        Album ledZeppelinIV = ledZeppelin.getAlbums().iterator().next();
        assertEquals(album.getName(), ledZeppelinIV.getName());
        assertEquals(ledZeppelin, ledZeppelinIV.getArtist());
        assertNull(ledZeppelinIV.getRecording());

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
        assertNotNull(ledZeppelin);
        assertEquals(ledZepp.getName(), ledZeppelin.getName());
        assertEquals(0, ledZeppelin.getAlbums().size());
    }

    /**
     * @see Issue 302
     */
    @Test
    public void shouldMaintainSortOrderWhenLoadingByIds() {
        Artist led = new Artist("Led Zeppelin");
        session.save(led);
        Artist bonJovi = new Artist("Bon Jovi");
        session.save(bonJovi);

        List<Long> ids = Arrays.asList(beatlesId, led.getId(), bonJovi.getId());
        SortOrder sortOrder = new SortOrder();
        sortOrder.add(SortOrder.Direction.ASC, "name");
        Iterable<Artist> artists = session.loadAll(Artist.class, ids, sortOrder);
        assertNotNull(artists);
        List<String> artistNames = new ArrayList<>();
        for (Artist artist : artists) {
            artistNames.add(artist.getName());
        }
        assertEquals("Bon Jovi", artistNames.get(0));
        assertEquals("Led Zeppelin", artistNames.get(1));
        assertEquals("The Beatles", artistNames.get(2));


        sortOrder = new SortOrder();
        sortOrder.add(SortOrder.Direction.DESC, "name");
        artists = session.loadAll(Artist.class, ids, sortOrder);
        assertNotNull(artists);
        artistNames = new ArrayList<>();
        for (Artist artist : artists) {
            artistNames.add(artist.getName());
        }
        assertEquals("The Beatles", artistNames.get(0));
        assertEquals("Led Zeppelin", artistNames.get(1));
        assertEquals("Bon Jovi", artistNames.get(2));

    }


}
