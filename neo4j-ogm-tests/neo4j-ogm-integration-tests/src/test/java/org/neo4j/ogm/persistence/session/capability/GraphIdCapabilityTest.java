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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.entityMapping.Movie;
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.domain.music.Artist;
import org.neo4j.ogm.domain.music.Recording;
import org.neo4j.ogm.domain.music.ReleaseFormat;
import org.neo4j.ogm.domain.music.Studio;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Luanne Misquitta
 */
public class GraphIdCapabilityTest extends TestContainersTestBase {

    private Session session;
    private Long pleaseId;
    private Long beatlesId;
    private Long recordingId;
    private Artist theBeatles;
    private Album please;
    private Recording recording;

    @Before
    public void init() throws IOException {
        SessionFactory sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.music");
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
        assertThat(session.resolveGraphIdFor(please)).isEqualTo(pleaseId);
        assertThat(session.resolveGraphIdFor(theBeatles)).isEqualTo(beatlesId);
    }

    /**
     * @see DATAGRAPH-694
     */
    @Test
    public void idShouldBeResolvedForValidRelationshipEntity() {
        assertThat(session.resolveGraphIdFor(recording)).isEqualTo(recordingId);
    }

    /**
     * @see DATAGRAPH-694
     */
    @Test
    public void idShouldReturnNullForEntitiesNotPersisted() {
        Album revolver = new Album("Revolver");
        assertThat(session.resolveGraphIdFor(revolver)).isNull();

        Recording revolverRecording = new Recording(revolver, new Studio(), 1966);
        assertThat(session.resolveGraphIdFor(revolverRecording)).isNull();
    }

    /**
     * @see DATAGRAPH-694
     */
    @Test
    public void idShouldReturnNullForNonEntities() {
        Movie movie = new Movie(); //not in the mapping context
        assertThat(session.resolveGraphIdFor(movie)).isNull();

        Long aLong = (long) 5;
        assertThat(session.resolveGraphIdFor(aLong)).isNull();
    }

    /**
     * @see DATAGRAPH-694
     */
    @Test
    public void idShouldReturnNullForEntitiesWithNoIdentity() {
        assertThat(session.resolveGraphIdFor(ReleaseFormat.VINYL)).isNull();
    }

    /**
     * @see Issue 69
     */
    @Test
    public void idShouldReturnNullForNullsOrPrimitives() {
        assertThat(session.resolveGraphIdFor(null)).isNull();
        assertThat(session.resolveGraphIdFor(true)).isNull();
        assertThat(session.resolveGraphIdFor(1)).isNull();
    }
}
