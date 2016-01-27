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

package org.neo4j.ogm.persistence.examples.music;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.domain.music.Artist;
import org.neo4j.ogm.domain.music.Recording;
import org.neo4j.ogm.domain.music.Studio;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Luanne Misquitta
 */
public class MusicIntegrationTest extends MultiDriverTestClass {

    private static Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory("org.neo4j.ogm.domain.music").openSession();
    }

    @After
    public void clear() {
        session.purgeDatabase();
    }

    /**
     * @see DATAGRAPH-589
     */
    @Test
    public void shouldSaveAndRetrieveEntitiesWithInvalidCharsInLabelsAndRels() {
        Studio emi = new Studio("EMI Studios, London");

        Artist theBeatles = new Artist("The Beatles");
        Album please = new Album("Please Please Me");
        Recording pleaseRecording = new Recording(please, emi, 1963);
        please.setRecording(pleaseRecording);
        theBeatles.getAlbums().add(please);
        please.setArtist(theBeatles);
        session.save(theBeatles);

        theBeatles = session.loadAll(Artist.class).iterator().next();
        assertEquals("The Beatles", theBeatles.getName());
        assertEquals(1, theBeatles.getAlbums().size());
        assertEquals("Please Please Me", theBeatles.getAlbums().iterator().next().getName());
        assertEquals("EMI Studios, London", theBeatles.getAlbums().iterator().next().getRecording().getStudio().getName());

        please = session.loadAll(Album.class, new Filter("name", "Please Please Me")).iterator().next();
        assertEquals("The Beatles", please.getArtist().getName());

        Album hard = new Album("A Hard Day's Night");
        hard.setArtist(theBeatles);
        Recording hardRecording = new Recording(hard, emi, 1964);
        hard.setRecording(hardRecording);
        theBeatles.getAlbums().add(hard);
        session.save(hard);

        Collection<Album> albums = session.loadAll(Album.class);
        assertEquals(2, albums.size());
        for (Album album : albums) {
            if (album.getName().equals("Please Please Me")) {
                assertEquals(1963, album.getRecording().getYear());
            } else {
                assertEquals(1964, album.getRecording().getYear());
            }
        }
    }

    /**
     * @see DATAGRAPH-631
     */
    @Test
    public void shouldLoadStudioWithLocationMissingInDomainModel() {
        session.query("CREATE (s:Studio {`studio-name`:'Abbey Road Studios'})", Utils.map());
        Studio studio = session.loadAll(Studio.class, new Filter("name", "Abbey Road Studios")).iterator().next();
        assertNotNull(studio);

    }

    /**
     * @see DATAGRAPH-629
     */
    @Test
    public void shouldRetrieveEntityByPropertyWithZeroDepth() {
        Studio emi = new Studio("EMI Studios, London");

        Artist theBeatles = new Artist("The Beatles");
        Album please = new Album("Please Please Me");
        Recording pleaseRecording = new Recording(please, emi, 1963);
        please.setRecording(pleaseRecording);
        theBeatles.getAlbums().add(please);
        please.setArtist(theBeatles);
        session.save(theBeatles);

        theBeatles = session.loadAll(Artist.class).iterator().next();
        assertEquals("The Beatles", theBeatles.getName());
        assertEquals(1, theBeatles.getAlbums().size());
        assertEquals("Please Please Me", theBeatles.getAlbums().iterator().next().getName());
        assertEquals("EMI Studios, London", theBeatles.getAlbums().iterator().next().getRecording().getStudio().getName());

        session.clear();

        please = session.loadAll(Album.class, new Filter("name", "Please Please Me"), 0).iterator().next();
        assertEquals("Please Please Me", please.getName());
        assertNull(please.getArtist());
        assertNull(please.getRecording());
    }

    /**
     * @see DATAGRAPH-637
     */
    @Test
    public void shouldSaveAndRetrieveArtistWithTwoRelationshipTypesToAlbums() {
        Studio emi = new Studio("EMI Studios, London");
        Studio olympic = new Studio("Olympic Studios, London");

        Artist theBeatles = new Artist("The Beatles");
        Artist eric = new Artist("Eric Clapton");

        Album slowhand = new Album("Slowhand");
        Recording slowRecording = new Recording(slowhand, olympic, 1977);
        slowhand.setRecording(slowRecording);
        slowhand.setArtist(eric);
        session.save(slowhand);


        session.clear();
        Album theBeatlesAlbum = new Album("The Beatles");
        Recording pleaseRecording = new Recording(theBeatlesAlbum, emi, 1968);
        theBeatlesAlbum.setRecording(pleaseRecording);
        theBeatles.getAlbums().add(theBeatlesAlbum);
        theBeatlesAlbum.setArtist(theBeatles);
        theBeatlesAlbum.setGuestArtist(eric);
        session.save(theBeatlesAlbum);

        theBeatles = session.loadAll(Artist.class, new Filters().add("name", "The Beatles")).iterator().next();
        assertEquals("The Beatles", theBeatles.getName());
        assertEquals(1, theBeatles.getAlbums().size());
        assertEquals("The Beatles", theBeatles.getAlbums().iterator().next().getName());
        assertEquals("EMI Studios, London", theBeatles.getAlbums().iterator().next().getRecording().getStudio().getName());
        assertEquals(eric, theBeatles.getAlbums().iterator().next().getGuestArtist());

        //Eric has 2 albums now
        session.clear();
        Artist loadedEric = session.loadAll(Artist.class, new Filters().add("name", "Eric Clapton")).iterator().next();
        assertNotNull(loadedEric);
        assertEquals("The Beatles", loadedEric.getGuestAlbums().iterator().next().getName());
        assertEquals("Slowhand", loadedEric.getAlbums().iterator().next().getName());
    }

    /**
     * Issue #83
     */
    @Test
    public void shouldBeAbleToQueryForLiteralMapWithAlias() {
        Studio emi = new Studio("EMI Studios, London");
        Artist theBeatles = new Artist("The Beatles");

        Album theBeatlesAlbum = new Album("The Beatles");
        Recording theBeatlesRec = new Recording(theBeatlesAlbum, emi, 1968);
        theBeatlesAlbum.setRecording(theBeatlesRec);
        theBeatles.getAlbums().add(theBeatlesAlbum);
        theBeatlesAlbum.setArtist(theBeatles);
        session.save(theBeatlesAlbum);

        Album please = new Album("Please Please Me");
        Recording pleaseRecording = new Recording(please, emi, 1963);
        please.setRecording(pleaseRecording);
        theBeatles.getAlbums().add(please);
        please.setArtist(theBeatles);
        session.save(theBeatles);

        Iterator<Map<String, Object>> resultIterator = session.query("MATCH (n:`l'artiste`)-[:`HAS-ALBUM`]-(a) return {artist: collect(distinct n.name), albums: collect(a.name)} as result", Collections.EMPTY_MAP).queryResults().iterator();
        assertTrue(resultIterator.hasNext());
        Map<String, Object> row = resultIterator.next();
        Map data = (Map) row.get("result");
        List<String> albums = (List<String>) data.get("albums");
        List<String> artist = (List<String>) data.get("artist");
        assertEquals(1, artist.size());
        assertEquals("The Beatles", artist.get(0));
        assertEquals(2, albums.size());
        assertTrue(albums.contains("The Beatles"));
        assertTrue(albums.contains("Please Please Me"));
        assertFalse(resultIterator.hasNext());
    }

    /**
     * Issue #83
     */
    @Test
    public void shouldBeAbleToQueryForLiteralMapWithoutAlias() {
        Studio emi = new Studio("EMI Studios, London");
        Artist theBeatles = new Artist("The Beatles");

        Album theBeatlesAlbum = new Album("The Beatles");
        Recording theBeatlesRec = new Recording(theBeatlesAlbum, emi, 1968);
        theBeatlesAlbum.setRecording(theBeatlesRec);
        theBeatles.getAlbums().add(theBeatlesAlbum);
        theBeatlesAlbum.setArtist(theBeatles);
        session.save(theBeatlesAlbum);

        Album please = new Album("Please Please Me");
        Recording pleaseRecording = new Recording(please, emi, 1963);
        please.setRecording(pleaseRecording);
        theBeatles.getAlbums().add(please);
        please.setArtist(theBeatles);
        session.save(theBeatles);

        Iterator<Map<String, Object>> resultIterator = session.query("MATCH (n:`l'artiste`)-[:`HAS-ALBUM`]-(a) return {artist: collect(distinct n.name), albums: collect(a.name)}", Collections.EMPTY_MAP).queryResults().iterator();
        assertTrue(resultIterator.hasNext());
        Map<String, Object> row = resultIterator.next();
        Map data = (Map) row.get("{artist: collect(distinct n.name), albums: collect(a.name)}");
        List<String> albums = (List<String>) data.get("albums");
        List<String> artist = (List<String>) data.get("artist");
        assertEquals(1, artist.size());
        assertEquals("The Beatles", artist.get(0));
        assertEquals(2, albums.size());
        assertTrue(albums.contains("The Beatles"));
        assertTrue(albums.contains("Please Please Me"));
        assertFalse(resultIterator.hasNext());
    }
}
