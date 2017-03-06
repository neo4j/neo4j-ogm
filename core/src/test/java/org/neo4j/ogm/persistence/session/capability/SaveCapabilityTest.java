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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.cypher.compiler.CompileContext;
import org.neo4j.ogm.context.EntityGraphMapper;
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.domain.music.Artist;
import org.neo4j.ogm.domain.music.Recording;
import org.neo4j.ogm.domain.music.Studio;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Luanne Misquitta
 */
public class SaveCapabilityTest extends MultiDriverTestClass{

    private Session session;
    private Artist aerosmith;
    private Artist bonJovi;
    private Artist defLeppard;

    @Before
    public void init() throws IOException {
        SessionFactory sessionFactory = new SessionFactory(baseConfiguration, "org.neo4j.ogm.domain.music");
        session = sessionFactory.openSession();
        session.purgeDatabase();
        aerosmith = new Artist("Aerosmith");
        bonJovi = new Artist("Bon Jovi");
        defLeppard = new Artist("Def Leppard");
    }

    @After
    public void clearDatabase() {
        session.purgeDatabase();
    }

    /**
     * @see Issue #84
     */
    @Test
    public void saveCollectionShouldSaveLists() {
        Album nineLives = new Album("Nine Lives");
        aerosmith.addAlbum(nineLives);
        Album crossRoad = new Album("Cross Road");
        bonJovi.addAlbum(crossRoad);
        List<Artist> artists = Arrays.asList(aerosmith, bonJovi, defLeppard);
        session.save(artists);
        session.clear();
        assertEquals(3, session.countEntitiesOfType(Artist.class));
        assertEquals(2, session.countEntitiesOfType(Album.class));
    }

    /**
     * @see Issue #84
     */
    @Test
    public void saveCollectionShouldSaveSets() {
        Set<Artist> artists = new HashSet<>();
        artists.add(aerosmith);
        artists.add(bonJovi);
        artists.add(defLeppard);
        session.save(artists);
        session.clear();
        assertEquals(3, session.countEntitiesOfType(Artist.class));
    }


    /**
     * @see Issue #84
     */
    @Test
    public void saveCollectionShouldSaveArrays() {
        Artist[] artists = new Artist[]{aerosmith, bonJovi, defLeppard};
        session.save(artists);
        session.clear();
        assertEquals(3, session.countEntitiesOfType(Artist.class));
    }

    @Test
    public void shouldSaveNewNodesAndNewRelationships() {
        Artist leann = new Artist("Leann Rimes");
        Album lost = new Album("Lost Highway");
        lost.setArtist(bonJovi);
        lost.setGuestArtist(leann);
        session.save(lost);
        session.clear();

        Artist loadedLeann = session.load(Artist.class, leann.getId());
        assertNotNull(loadedLeann);
        assertEquals("Leann Rimes", loadedLeann.getName());
        assertEquals(lost.getName(), loadedLeann.getGuestAlbums().iterator().next().getName());

        Artist loadedBonJovi = session.load(Artist.class, bonJovi.getId());
        assertNotNull(loadedBonJovi);
        assertEquals("Bon Jovi", loadedBonJovi.getName());
        assertEquals(lost.getName(), loadedBonJovi.getAlbums().iterator().next().getName());

        Album loadedLost = session.load(Album.class, lost.getId());
        assertNotNull(loadedLost);
        assertEquals("Lost Highway", loadedLost.getName());
        assertEquals(loadedLeann, loadedLost.getGuestArtist());
        assertEquals(loadedBonJovi.getName(), loadedLost.getArtist().getName());
    }


    @Test
    public void shouldSaveOnlyModifiedNodes() {

        int depth = 1;
        Neo4jSession neo4jSession = (Neo4jSession)session;
        CompileContext context = null;

        Artist leann = new Artist("Leann Rimes");
        Album lost = new Album("Lost Highway");
        lost.setArtist(bonJovi);
        lost.setGuestArtist(leann);

        context = new EntityGraphMapper(neo4jSession.metaData(), neo4jSession.context()).map(lost, depth);
        assertEquals("Should save 3 nodes and 2 relations (5 items)" , 5, context.registry().size());

        session.save(lost);

        context = new EntityGraphMapper(neo4jSession.metaData(), neo4jSession.context()).map(lost, depth);
        assertEquals("Should have nothing to save" , 0, context.registry().size());

        session.clear();

        Artist loadedLeann = session.load(Artist.class, leann.getId(), depth);

        context = new EntityGraphMapper(neo4jSession.metaData(), neo4jSession.context()).map(loadedLeann, depth);
        assertEquals("Should have nothing to save" , 0, context.registry().size());

        loadedLeann.setName("New name");
        context = new EntityGraphMapper(neo4jSession.metaData(), neo4jSession.context()).map(loadedLeann, depth);
        assertEquals("Should have one node to save" , 1, context.registry().size());

        loadedLeann.getGuestAlbums().iterator().next().setName("New Album Name");

        context = new EntityGraphMapper(neo4jSession.metaData(), neo4jSession.context()).map(loadedLeann, depth);
        assertEquals("Should have two node to save" , 2, context.registry().size());
    }

    @Test
    public void shouldCountRelationshipEntities() {
        Album greatestHits = new Album("Greatest Hits");

        Studio chessRecordsStudios = new Studio("Chess Records");
        Studio mercuryStudios = new Studio("Mercury");

        Recording recording1 = new Recording(greatestHits, chessRecordsStudios, 1962);
        Recording recording2 = new Recording(greatestHits, mercuryStudios, 1967);

        greatestHits.setRecording(recording1);
        greatestHits.setRecording(recording2);

        session.save(recording1);
        session.save(recording2);

        assertEquals(2, session.countEntitiesOfType(Recording.class));
    }
}
