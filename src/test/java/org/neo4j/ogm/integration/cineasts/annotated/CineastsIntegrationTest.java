/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.integration.cineasts.annotated;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.domain.cineasts.annotated.Actor;
import org.neo4j.ogm.domain.cineasts.annotated.Movie;
import org.neo4j.ogm.domain.cineasts.annotated.Rating;
import org.neo4j.ogm.domain.cineasts.annotated.SecurityRole;
import org.neo4j.ogm.domain.cineasts.annotated.Title;
import org.neo4j.ogm.domain.cineasts.annotated.User;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.Neo4jIntegrationTestRule;

/**
 * Simple integration test based on cineasts that exercises relationship entities.
 *
 * @author Michal Bachman
 * @author Adam George
 */
public class CineastsIntegrationTest {

    @ClassRule
    public static Neo4jIntegrationTestRule databaseServerRule = new Neo4jIntegrationTestRule();

    private static Session session;

    @Before
    public  void init() throws IOException {
        session = new SessionFactory("org.neo4j.ogm.domain.cineasts.annotated").openSession(databaseServerRule.url());
        importCineasts();
    }

    @After
    public void tearDown() {
        session.purgeDatabase();
    }
    private static void importCineasts() {
        databaseServerRule.loadClasspathCypherScriptFile("org/neo4j/ogm/cql/cineasts.cql");
    }

    @Test
    public void loadRatingsAndCommentsAboutMovies() {
        Collection<Movie> movies = session.loadAll(Movie.class);

        assertEquals(3, movies.size());

        for (Movie movie : movies) {

            if (movie.getRatings() != null) {
                for (Rating rating : movie.getRatings()) {
                    assertNotNull("The film on the rating shouldn't be null", rating.getMovie());
                    assertSame("The film on the rating was not mapped correctly", movie, rating.getMovie());
                    assertNotNull("The film critic wasn't set", rating.getUser());
                }
            }
        }
    }

    @Test
    public void loadParticularUserRatingsAndComments() {
        Collection<User> filmCritics = session.loadAll(User.class, new Filter("name", "Michal"));
        assertEquals(1, filmCritics.size());

        User critic = filmCritics.iterator().next();
        assertEquals(2, critic.getRatings().size());

        for (Rating rating : critic.getRatings()) {
            assertNotNull("The comment should've been mapped", rating.getComment());
            assertTrue("The star rating should've been mapped", rating.getStars() > 0);
            assertNotNull("The user start node should've been mapped", rating.getUser());
            assertNotNull("The movie end node should've been mapped", rating.getMovie());
        }
    }

    @Test
    public void loadRatingsForSpecificFilm() {
        Collection<Movie> films = session.loadAll(Movie.class, new Filter("title", "Top Gear"));
        assertEquals(1, films.size());

        Movie film = films.iterator().next();
        assertEquals(2, film.getRatings().size());

        for (Rating rating : film.getRatings()) {
            assertTrue("The star rating should've been mapped", rating.getStars() > 0);
            assertNotNull("The user start node should've been mapped", rating.getUser());
            assertSame("The wrong film was mapped to the rating", film, rating.getMovie());
        }
    }

    @Test
    public void saveAndRetrieveUserWithSecurityRoles() {
        User user = new User();
        user.setLogin("daniela");
        user.setName("Daniela");
        user.setPassword("daniela");
        user.setSecurityRoles(new SecurityRole[]{SecurityRole.USER});
        session.save(user);

        Collection<User> users = session.loadAll(User.class, new Filter("login", "daniela"));
        assertEquals(1,users.size());
        User daniela = users.iterator().next();
        assertEquals("Daniela", daniela.getName());
        assertEquals(1,daniela.getSecurityRoles().length);
        assertEquals(SecurityRole.USER,daniela.getSecurityRoles()[0]);
    }

    @Test
    public void saveAndRetrieveUserWithTitles() {
        User user = new User();
        user.setLogin("vince");
        user.setName("Vince");
        user.setPassword("vince");
        user.setTitles(Arrays.asList(Title.MR));
        session.save(user);

        Collection<User> users = session.loadAll(User.class, new Filter("login", "vince"));
        assertEquals(1,users.size());
        User vince = users.iterator().next();
        assertEquals("Vince", vince.getName());
        assertEquals(1, vince.getTitles().size());
        assertEquals(Title.MR,vince.getTitles().get(0));

    }

    /**
     * @see DATAGRAPH-614
     */
    @Test
    public void saveAndRetrieveUserWithDifferentCharset() {
        User user = new User();
        user.setLogin("aki");
        user.setName("Aki Kaurism\u00E4ki");
        user.setPassword("aki");
        session.save(user);
        Collection<User> users = session.loadAll(User.class, new Filter("login", "aki"));
        assertEquals(1,users.size());
        User aki = users.iterator().next();
        try {
            assertArrayEquals("Aki Kaurism\u00E4ki".getBytes("UTF-8"), aki.getName().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            fail("UTF-8 encoding not supported on this platform");
        }

    }

    @Test
    public void shouldQueryForSpecificActorUsingBespokeParameterisedCypherQuery() {
        session.save(new Actor("Alec Baldwin"));
        session.save(new Actor("Helen Mirren"));
        session.save(new Actor("Matt Damon"));

        Actor loadedActor = session.queryForObject(Actor.class, "MATCH (a:Actor) WHERE a.name={param} RETURN a",
                Collections.singletonMap("param", "Alec Baldwin"));
        assertNotNull("The entity wasn't loaded", loadedActor);
        assertEquals("Alec Baldwin", loadedActor.getName());
    }

    @Test
    public void shouldQueryForCollectionOfActorsUsingBespokeCypherQuery() {
        session.save(new Actor("Jeff"));
        session.save(new Actor("John"));
        session.save(new Actor("Colin"));

        Iterable<Actor> actors = session.query(Actor.class, "MATCH (a:Actor) WHERE a.name=~'J.*' RETURN a",
                Collections.<String, Object>emptyMap());
        assertNotNull("The entities weren't loaded", actors);
        assertTrue("The entity wasn't loaded", actors.iterator().hasNext());
        for (Actor actor : actors) {
            assertTrue("Shouldn't've loaded " + actor.getName(), actor.getName().equals("John") || actor.getName().equals("Jeff"));
        }
    }

    @Test
    public void shouldQueryForActorByIdUsingBespokeParameterisedCypherQuery() {
        session.save(new Actor("Keanu Reeves"));
        Actor carrie = new Actor("Carrie-Ann Moss");
        session.save(carrie);
        session.save(new Actor("Laurence Fishburne"));

        Actor loadedActor = session.queryForObject(Actor.class, "MATCH (a:Actor) WHERE ID(a)={param} RETURN a",
                Collections.<String, Object>singletonMap("param", carrie.getId()));
        assertNotNull("The entity wasn't loaded", loadedActor);
        assertEquals("Carrie-Ann Moss", loadedActor.getName());
    }

	/**
     * @see Issue #125
     * @throws MalformedURLException
     */
    @Test
    public void shouldModifyStringArraysCorrectly() throws MalformedURLException {
        User user = new User();
        URL[] urls = new URL[3];
        urls[0] = new URL("http://www.apple.com");
        urls[1] = new URL("http://www.google.com");
        urls[2] = new URL("http://www.neo4j.com");
        user.setUrls(urls);

        String[] nicknames = new String[2];
        nicknames[0] = "batman";
        nicknames[1] = "robin";
        user.setNicknames(nicknames);

        session.save(user);

        user.getUrls()[0] = new URL("http://www.graphaware.com");
        user.getNicknames()[0] = "batgirl";

        session.save(user);

        user = session.load(User.class, user.getId());
        assertEquals(3, user.getUrls().length);
        assertEquals("http://www.graphaware.com", user.getUrls()[0].toString());
        assertEquals("http://www.google.com", user.getUrls()[1].toString());
        assertEquals("http://www.neo4j.com", user.getUrls()[2].toString());
        assertEquals(2, user.getNicknames().length);
        assertEquals("batgirl", user.getNicknames()[0]);
        assertEquals("robin", user.getNicknames()[1]);
    }

    @Test
    public void shouldSetPropertiesToNull() throws MalformedURLException {
        Movie movie = new Movie();
        movie.setTitle("Zootopia");
        movie.setImdbUrl(new URL("http://www.imdb.com/title/tt2948356/"));
        session.save(movie);

        movie.setTitle(null);
        movie.setImdbUrl(null);
        session.save(movie);

        session.clear();

        movie = session.load(Movie.class, movie.getId());
        assertNull(movie.getTitle());
        assertNull(movie.getImdbUrl());
    }
}
