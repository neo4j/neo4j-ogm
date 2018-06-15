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

package org.neo4j.ogm.persistence.examples.cineasts.annotated;

import static org.assertj.core.api.Assertions.*;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.domain.cineasts.annotated.Actor;
import org.neo4j.ogm.domain.cineasts.annotated.Hobby;
import org.neo4j.ogm.domain.cineasts.annotated.Movie;
import org.neo4j.ogm.domain.cineasts.annotated.Pet;
import org.neo4j.ogm.domain.cineasts.annotated.Plays;
import org.neo4j.ogm.domain.cineasts.annotated.Rating;
import org.neo4j.ogm.domain.cineasts.annotated.SecurityRole;
import org.neo4j.ogm.domain.cineasts.annotated.Title;
import org.neo4j.ogm.domain.cineasts.annotated.User;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.testutil.TestUtils;

/**
 * Simple integration test based on cineasts that exercises relationship entities.
 *
 * @author Michal Bachman
 * @author Adam George
 * @author Mark Angrish
 */
public class CineastsIntegrationTest extends MultiDriverTestClass {

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.cineasts.annotated");
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();
        importCineasts();
    }

    private void importCineasts() {
        session.query(TestUtils.readCQLFile("org/neo4j/ogm/cql/cineasts.cql").toString(), Utils.map());
    }

    @After
    public void teardown() {
        session.purgeDatabase();
    }

    @Test
    public void loadRatingsAndCommentsAboutMovies() {
        Collection<Movie> movies = session.loadAll(Movie.class);

        assertThat(movies).hasSize(3);

        for (Movie movie : movies) {

            if (movie.getRatings() != null) {
                for (Rating rating : movie.getRatings()) {
                    assertThat(rating.getMovie()).as("The film on the rating shouldn't be null").isNotNull();
                    assertThat(rating.getMovie()).as("The film on the rating was not mapped correctly").isSameAs(movie);
                    assertThat(rating.getUser()).as("The film critic wasn't set").isNotNull();
                }
            }
        }
    }

    @Test
    public void loadParticularUserRatingsAndComments() {
        Collection<User> filmCritics = session
            .loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "Michal"));
        assertThat(filmCritics).hasSize(1);

        User critic = filmCritics.iterator().next();
        assertThat(critic.getRatings()).hasSize(2);

        for (Rating rating : critic.getRatings()) {
            assertThat(rating.getComment()).as("The comment should've been mapped").isNotNull();
            assertThat(rating.getStars() > 0).as("The star rating should've been mapped").isTrue();
            assertThat(rating.getUser()).as("The user start node should've been mapped").isNotNull();
            assertThat(rating.getMovie()).as("The movie end node should've been mapped").isNotNull();
        }
    }

    @Test
    public void loadRatingsForSpecificFilm() {
        Collection<Movie> films = session
            .loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Top Gear"));
        assertThat(films).hasSize(1);

        Movie film = films.iterator().next();
        assertThat(film.getRatings()).hasSize(2);

        for (Rating rating : film.getRatings()) {
            assertThat(rating.getStars() > 0).as("The star rating should've been mapped").isTrue();
            assertThat(rating.getUser()).as("The user start node should've been mapped").isNotNull();
            assertThat(rating.getMovie()).as("The wrong film was mapped to the rating").isSameAs(film);
        }
    }

    @Test
    public void loadFilmByRatingUsersPet() {
        // Pulp Fiction and Top Gear rated by Michal who owns Catty

        Filter filter = new Filter("name", ComparisonOperator.EQUALS, "Catty");

        filter.setOwnerEntityType(Movie.class);

        filter.setNestedPath(
            new Filter.NestedPathSegment("ratings", Rating.class),
            new Filter.NestedPathSegment("user", User.class),
            new Filter.NestedPathSegment("pets", Pet.class)
        );
        Collection<Movie> films = session.loadAll(Movie.class, filter);
        assertThat(films).hasSize(2);

    }

    @Test
    public void loadFilmByRating() {
        Filter filter = new Filter("stars", ComparisonOperator.EQUALS, 5);

        filter.setOwnerEntityType(Movie.class);

        filter.setNestedPath(
            new Filter.NestedPathSegment("ratings", Rating.class)
        );
        Collection<Movie> films = session.loadAll(Movie.class, filter);
        assertThat(films).hasSize(1);
    }

    @Test
    public void loadFilmByRatingUserPlays() {
        Filter filter = new Filter("level", ComparisonOperator.EQUALS, "ok");
        filter.setOwnerEntityType(Movie.class);

        filter.setNestedPath(
            new Filter.NestedPathSegment("ratings", Rating.class),
            new Filter.NestedPathSegment("user", User.class),
            new Filter.NestedPathSegment("plays", Plays.class)
        );
        Collection<Movie> films = session.loadAll(Movie.class, filter);
        assertThat(films).hasSize(2);

    }

    @Test
    public void loadFilmByUserAndRatingUserPlays() {
        Filter userFilter = new Filter("name", ComparisonOperator.EQUALS, "Michal");

        userFilter.setNestedPath(
            new Filter.NestedPathSegment("ratings", Rating.class),
            new Filter.NestedPathSegment("user", User.class)
        );

        Filter playsFilter = new Filter("level", ComparisonOperator.EQUALS, "ok");

        playsFilter.setOwnerEntityType(Movie.class);

        playsFilter.setNestedPath(
            new Filter.NestedPathSegment("ratings", Rating.class),
            new Filter.NestedPathSegment("user", User.class),
            new Filter.NestedPathSegment("plays", Plays.class)
        );

        Filters filters = userFilter.and(playsFilter);
        Collection<Movie> films = session.loadAll(Movie.class, filters);
        assertThat(films).hasSize(2);

    }

    @Test
    public void loadRatingByUserName() {
        Filter userNameFilter = new Filter("name", ComparisonOperator.EQUALS, "Michal");

        Filter ratingFilter = new Filter("stars", ComparisonOperator.EQUALS, 5);

        userNameFilter.setNestedPath(
            new Filter.NestedPathSegment("user", User.class)
        );

        Collection<Rating> ratings = session.loadAll(Rating.class, userNameFilter.and(ratingFilter));
        assertThat(ratings).hasSize(1);

    }
    @Test
    public void loadRatingByUserNameAndStars() {
        Filter userNameFilter = new Filter("name", ComparisonOperator.EQUALS, "Michal");

        userNameFilter.setNestedPath(
            new Filter.NestedPathSegment("user", User.class)
        );

        Collection<Rating> ratings = session.loadAll(Rating.class, userNameFilter);
        assertThat(ratings).hasSize(2);

    }

    @Test
    public void saveAndRetrieveUserWithSecurityRoles() {
        User user = new User();
        user.setLogin("daniela");
        user.setName("Daniela");
        user.setPassword("daniela");
        user.setSecurityRoles(new SecurityRole[] { SecurityRole.USER });
        session.save(user);

        User daniela = session.load(User.class, "daniela");
        assertThat(daniela).isNotNull();
        assertThat(daniela.getName()).isEqualTo("Daniela");
        assertThat(daniela.getSecurityRoles().length).isEqualTo(1);
        assertThat(daniela.getSecurityRoles()[0]).isEqualTo(SecurityRole.USER);
    }

    @Test
    public void saveAndRetrieveUserWithTitles() {
        User user = new User();
        user.setLogin("vince");
        user.setName("Vince");
        user.setPassword("vince");
        user.setTitles(Arrays.asList(Title.MR));
        session.save(user);

        User vince = session.load(User.class, "vince");
        assertThat(vince).isNotNull();
        assertThat(vince.getName()).isEqualTo("Vince");
        assertThat(vince.getTitles()).hasSize(1);
        assertThat(vince.getTitles().get(0)).isEqualTo(Title.MR);
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
        User aki = session.load(User.class, "aki");
        assertThat(aki).isNotNull();
        try {
            assertThat(aki.getName().getBytes("UTF-8")).isEqualTo("Aki Kaurism\u00E4ki".getBytes("UTF-8"));
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
        assertThat(loadedActor).as("The entity wasn't loaded").isNotNull();
        assertThat(loadedActor.getName()).isEqualTo("Alec Baldwin");
    }

    @Test
    public void shouldQueryForCollectionOfActorsUsingBespokeCypherQuery() {
        session.save(new Actor("Jeff"));
        session.save(new Actor("John"));
        session.save(new Actor("Colin"));

        Iterable<Actor> actors = session.query(Actor.class, "MATCH (a:Actor) WHERE a.name=~'J.*' RETURN a",
            Collections.<String, Object>emptyMap());
        assertThat(actors).as("The entities weren't loaded").isNotNull();
        assertThat(actors.iterator().hasNext()).as("The entity wasn't loaded").isTrue();
        assertThat(actors).extracting(Actor::getName).containsOnly("John", "Jeff");
    }

    @Test
    public void shouldQueryForActorByIdUsingBespokeParameterisedCypherQuery() {
        session.save(new Actor("Keanu Reeves"));
        Actor carrie = new Actor("Carrie-Ann Moss");
        session.save(carrie);
        session.save(new Actor("Laurence Fishbourne"));

        Actor loadedActor = session.queryForObject(Actor.class, "MATCH (a:Actor) WHERE a.uuid={param} RETURN a",
            Collections.<String, Object>singletonMap("param", carrie.getUuid()));
        assertThat(loadedActor).as("The entity wasn't loaded").isNotNull();
        assertThat(loadedActor.getName()).isEqualTo("Carrie-Ann Moss");
    }

    /**
     * @throws MalformedURLException
     * @see issue #125
     */
    @Test
    public void shouldModifyStringArraysCorrectly() throws MalformedURLException {
        User user = new User("joker", "Joker", "password");
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

        // Test that arrays with and without custom converters are saved and loaded correctly when their content is updated
        user = session.load(User.class, user.getLogin());
        assertThat(user.getUrls().length).isEqualTo(3);
        assertThat(user.getUrls()[0].toString()).isEqualTo("http://www.graphaware.com");
        assertThat(user.getUrls()[1].toString()).isEqualTo("http://www.google.com");
        assertThat(user.getUrls()[2].toString()).isEqualTo("http://www.neo4j.com");
        assertThat(user.getNicknames().length).isEqualTo(2);
        assertThat(user.getNicknames()[0]).isEqualTo("batgirl");
        assertThat(user.getNicknames()[1]).isEqualTo("robin");
    }

    /**
     * @throws MalformedURLException
     * @see Issue #128
     */
    @Test
    public void shouldBeAbleToSetNodePropertiesToNull() throws MalformedURLException {
        Movie movie = new Movie("Zootopia", 2016);
        movie.setImdbUrl(new URL("http://www.imdb.com/title/tt2948356/"));
        session.save(movie);

        movie.setTitle(null);
        movie.setImdbUrl(null);
        session.save(movie);

        movie = session.load(Movie.class, movie.getUuid());
        assertThat(movie.getTitle()).isNull();
        assertThat(movie.getImdbUrl()).isNull();

        session.clear();
        movie = session.load(Movie.class, movie.getUuid());
        assertThat(movie.getTitle()).isNull();
        assertThat(movie.getImdbUrl()).isNull();
    }
}
