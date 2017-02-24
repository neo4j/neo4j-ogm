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
import org.neo4j.ogm.domain.entityMapping.Movie;
import org.neo4j.ogm.domain.music.*;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Luanne Misquitta
 */
public class GraphIdCapabilityTest extends MultiDriverTestClass {

    private Session session;
    private Long pleaseId;
    private Long beatlesId;
    private Long recordingId;
    private Artist theBeatles;
    private Album please;
    private Recording recording;

    @Before
    public void init() throws IOException {
        SessionFactory sessionFactory = new SessionFactory(baseConfiguration, "org.neo4j.ogm.domain.music");
        session = sessionFactory.openSession();
        session.purgeDatabase();
        //Create some data
        theBeatles = new Artist("The Beatles");
        please = new Album("Please Please Me");
        theBeatles.getAlbums().add(please);
        please.setArtist(theBeatles);
        session.save(theBeatles);

        Studio studio = new Studio("EMI Studios, London");
        recording = new Recording(please, studio, 1963);
        please.setRecording(recording);
        session.save(recording);

        pleaseId = please.getId();
        beatlesId = theBeatles.getId();
        recordingId = recording.getId();
    }

    @After
    public void clearDatabase() {
        session.purgeDatabase();
    }

    /**
     * @see DATAGRAPH-694
     */
    @Test
    public void idShouldBeResolvedForValidNodeEntity() {
        assertEquals(pleaseId, session.resolveGraphIdFor(please));
        assertEquals(beatlesId, session.resolveGraphIdFor(theBeatles));
    }

    /**
     * @see DATAGRAPH-694
     */
    @Test
    public void idShouldBeResolvedForValidRelationshipEntity() {
        assertEquals(recordingId, session.resolveGraphIdFor(recording));
    }

    /**
     * @see DATAGRAPH-694
     */
    @Test
    public void idShouldReturnNullForEntitiesNotPersisted() {
        Album revolver = new Album("Revolver");
        assertNull(session.resolveGraphIdFor(revolver));

        Recording revolverRecording = new Recording(revolver, new Studio(), 1966);
        assertNull(session.resolveGraphIdFor(revolverRecording));

    }

    /**
     * @see DATAGRAPH-694
     */
    @Test
    public void idShouldReturnNullForNonEntities() {
        Movie movie = new Movie(); //not in the mapping context
        assertNull(session.resolveGraphIdFor(movie));

        Long aLong = (long) 5;
        assertNull(session.resolveGraphIdFor(aLong));
    }

    /**
     * @see DATAGRAPH-694
     */
    @Test
    public void idShouldReturnNullForEntitiesWithNoIdentity() {
        assertNull(session.resolveGraphIdFor(ReleaseFormat.VINYL));
    }

    /**
     * @see Issue 69
     */
    @Test
    public void idShouldReturnNullForNullsOrPrimitives() {
        assertNull(session.resolveGraphIdFor(null));
        assertNull(session.resolveGraphIdFor(true));
        assertNull(session.resolveGraphIdFor(1));
    }

}
