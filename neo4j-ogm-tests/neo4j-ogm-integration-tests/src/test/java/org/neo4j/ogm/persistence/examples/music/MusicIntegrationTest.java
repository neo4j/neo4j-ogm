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
package org.neo4j.ogm.persistence.examples.music;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.domain.music.Album;
import org.neo4j.ogm.domain.music.Artist;
import org.neo4j.ogm.domain.music.Recording;
import org.neo4j.ogm.domain.music.Studio;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Luanne Misquitta
 */
public class MusicIntegrationTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.music");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
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
        assertThat(theBeatles.getName()).isEqualTo("The Beatles");
        assertThat(theBeatles.getAlbums()).hasSize(1);
        assertThat(theBeatles.getAlbums().iterator().next().getName()).isEqualTo("Please Please Me");
        assertThat(theBeatles.getAlbums().iterator().next().getRecording().getStudio().getName())
            .isEqualTo("EMI Studios, London");

        please = session.loadAll(Album.class, new Filter("name", ComparisonOperator.EQUALS, "Please Please Me"))
            .iterator().next();
        assertThat(please.getArtist().getName()).isEqualTo("The Beatles");

        Album hard = new Album("A Hard Day's Night");
        hard.setArtist(theBeatles);
        Recording hardRecording = new Recording(hard, emi, 1964);
        hard.setRecording(hardRecording);
        theBeatles.getAlbums().add(hard);
        session.save(hard);

        Collection<Album> albums = session.loadAll(Album.class);
        assertThat(albums).hasSize(2);
        for (Album album : albums) {
            if (album.getName().equals("Please Please Me")) {
                assertThat(album.getRecording().getYear()).isEqualTo(1963);
            } else {
                assertThat(album.getRecording().getYear()).isEqualTo(1964);
            }
        }
    }

    /**
     * @see DATAGRAPH-631
     */
    @Test
    public void shouldLoadStudioWithLocationMissingInDomainModel() {
        session.query("CREATE (s:Studio {`studio-name`:'Abbey Road Studios'})", Collections.emptyMap());
        Studio studio = session
            .loadAll(Studio.class, new Filter("name", ComparisonOperator.EQUALS, "Abbey Road Studios")).iterator()
            .next();
        assertThat(studio).isNotNull();
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
        assertThat(theBeatles.getName()).isEqualTo("The Beatles");
        assertThat(theBeatles.getAlbums()).hasSize(1);
        assertThat(theBeatles.getAlbums().iterator().next().getName()).isEqualTo("Please Please Me");
        assertThat(theBeatles.getAlbums().iterator().next().getRecording().getStudio().getName())
            .isEqualTo("EMI Studios, London");

        session.clear();

        please = session.loadAll(Album.class, new Filter("name", ComparisonOperator.EQUALS, "Please Please Me"), 0)
            .iterator().next();
        assertThat(please.getName()).isEqualTo("Please Please Me");
        assertThat(please.getArtist()).isNull();
        assertThat(please.getRecording()).isNull();
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

        theBeatles = session
            .loadAll(Artist.class, new Filters().add(new Filter("name", ComparisonOperator.EQUALS, "The Beatles")))
            .iterator().next();
        assertThat(theBeatles.getName()).isEqualTo("The Beatles");
        assertThat(theBeatles.getAlbums()).hasSize(1);
        assertThat(theBeatles.getAlbums().iterator().next().getName()).isEqualTo("The Beatles");
        assertThat(theBeatles.getAlbums().iterator().next().getRecording().getStudio().getName())
            .isEqualTo("EMI Studios, London");
        assertThat(theBeatles.getAlbums().iterator().next().getGuestArtist()).isEqualTo(eric);

        //Eric has 2 albums now
        session.clear();
        Artist loadedEric = session
            .loadAll(Artist.class, new Filters().add(new Filter("name", ComparisonOperator.EQUALS, "Eric Clapton")))
            .iterator().next();
        assertThat(loadedEric).isNotNull();
        assertThat(loadedEric.getGuestAlbums().iterator().next().getName()).isEqualTo("The Beatles");
        assertThat(loadedEric.getAlbums().iterator().next().getName()).isEqualTo("Slowhand");
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

        Iterator<Map<String, Object>> resultIterator = session.query(
            "MATCH (n:`l'artiste`)-[:`HAS-ALBUM`]-(a) return {artist: collect(distinct n.name), albums: collect(a.name)} as result",
            Collections.EMPTY_MAP).queryResults().iterator();
        assertThat(resultIterator.hasNext()).isTrue();
        Map<String, Object> row = resultIterator.next();
        Map data = (Map) row.get("result");
        List<String> albums = (List<String>) data.get("albums");
        List<String> artist = (List<String>) data.get("artist");
        assertThat(artist).hasSize(1);
        assertThat(artist.get(0)).isEqualTo("The Beatles");
        assertThat(albums).hasSize(2);
        assertThat(albums.contains("The Beatles")).isTrue();
        assertThat(albums.contains("Please Please Me")).isTrue();
        assertThat(resultIterator.hasNext()).isFalse();
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

        Iterator<Map<String, Object>> resultIterator = session.query(
            "MATCH (n:`l'artiste`)-[:`HAS-ALBUM`]-(a) return {artist: collect(distinct n.name), albums: collect(a.name)}",
            Collections.EMPTY_MAP).queryResults().iterator();
        assertThat(resultIterator.hasNext()).isTrue();
        Map<String, Object> row = resultIterator.next();
        Map data = (Map) row.get("{artist: collect(distinct n.name), albums: collect(a.name)}");
        List<String> albums = (List<String>) data.get("albums");
        List<String> artist = (List<String>) data.get("artist");
        assertThat(artist).hasSize(1);
        assertThat(artist.get(0)).isEqualTo("The Beatles");
        assertThat(albums).hasSize(2);
        assertThat(albums.contains("The Beatles")).isTrue();
        assertThat(albums.contains("Please Please Me")).isTrue();
        assertThat(resultIterator.hasNext()).isFalse();
    }

    /**
     * @see Issue 191
     */
    @Test
    public void shouldSortByDomainPropertyName() {
        Studio emi = new Studio("EMI Studios, London");
        Studio olympic = new Studio("Olympic Studios, London");

        session.save(emi);
        session.save(olympic);

        session.clear();

        SortOrder name = new SortOrder().add("name");
        Collection<Studio> studios1 = session.loadAll(Studio.class, name);
        Collection<Studio> studios2 = session.loadAll(Studio.class, name);
        assertThat(studios1.iterator().next().getName()).isEqualTo("EMI Studios, London");
        assertThat(studios2.iterator().next().getName()).isEqualTo("EMI Studios, London");

        studios1 = session.loadAll(Studio.class, new SortOrder().add(SortOrder.Direction.DESC, "name"));
        assertThat(studios1.iterator().next().getName()).isEqualTo("Olympic Studios, London");
    }
}
