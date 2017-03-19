/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.util.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.domain.cineasts.annotated.*;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.testutil.TestUtils;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
public class CineastsRelationshipEntityTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory("org.neo4j.ogm.domain.cineasts.annotated");
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void shouldSaveMultipleRatingsFromDifferentUsersForSameMovie() {
        Movie movie = new Movie("Pulp Fiction", 1994);
        session.save(movie);

        User michal = new User();
        michal.setName("Michal");
        michal.setLogin("bachmania");

        Rating awesome = new Rating();
        awesome.setMovie(movie);
        awesome.setUser(michal);
        awesome.setStars(5);
        michal.setRatings(Collections.singleton(awesome));
        session.save(michal);

        //Check that Pulp Fiction has one rating from Michal
        Collection<Movie> films = session.loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Pulp Fiction"));
        assertEquals(1, films.size());

        Movie film = films.iterator().next();
        Assert.assertNotNull(film);
        assertEquals(1, film.getRatings().size());
        assertEquals("Michal", film.getRatings().iterator().next().getUser().getName());

        //Add a rating from luanne for the same movie
        User luanne = new User();
        luanne.setName("luanne");
        luanne.setLogin("luanne");
        luanne.setPassword("luanne");

        Rating rating = new Rating();
        rating.setMovie(film);
        rating.setUser(luanne);
        rating.setStars(3);
        luanne.setRatings(Collections.singleton(rating));
        session.save(luanne);

        //Verify that pulp fiction has two ratings
        films = session.loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Pulp Fiction"));
        film = films.iterator().next();
        assertEquals(2, film.getRatings().size());   //Fail, it has just one rating, luannes

        //Verify that luanne's rating is saved
        User foundLuanne = session.load(User.class, "luanne");
        assertEquals(1, foundLuanne.getRatings().size());

        //Verify that Michals rating still exists
        Collection<User> users = session.loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "Michal"));
        User foundMichal = users.iterator().next();
        assertEquals(1, foundMichal.getRatings().size()); //Fail, Michals rating is gone
    }

    @Test
    public void shouldCreateREWithExistingStartAndEndNodes() {

        bootstrap("org/neo4j/ogm/cql/cineasts.cql");

        Collection<Movie> films = session.loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Top Gear"));
        Movie movie = films.iterator().next();
        assertEquals(2, movie.getRatings().size());

        User michal = null;
        for (Rating rating : movie.getRatings()) {
            if (rating.getUser().getName().equals("Michal")) {
                michal = rating.getUser();
                break;
            }
        }
        assertNotNull(michal);
        Set<Rating> ratings = new HashSet<>();
        Rating awesome = new Rating();
        awesome.setComment("Awesome");
        awesome.setMovie(movie);
        awesome.setUser(michal);
        awesome.setStars(5);
        ratings.add(awesome);

        michal.setRatings(ratings); //Overwrite Michal's earlier rating
        movie.setRatings(ratings);
        session.save(movie);

        Collection<Movie> movies = session.loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Top Gear"));
        movie = movies.iterator().next();
        Assert.assertNotNull(movie.getRatings()); //Fails. But when entities are created first, test passes, see CineastsRatingsTest.shouldSaveRatingWithMovie
        assertEquals(1, movie.getRatings().size());
        assertEquals("Michal", movie.getRatings().iterator().next().getUser().getName());
    }

    @Test
    public void shouldNotLoseRelationshipEntitiesWhenALoadedEntityIsPersisted() {

        bootstrap("org/neo4j/ogm/cql/cineasts.cql");

        Movie topGear = session.loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Top Gear")).iterator().next();
        assertEquals(2, topGear.getRatings().size());  //2 ratings
        session.save(topGear);

        topGear = session.loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Top Gear")).iterator().next();
        assertEquals(2, topGear.getRatings().size());  //Then there was one

        User michal = session.loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "Michal")).iterator().next();
        assertEquals(2, michal.getRatings().size());  //The Top Gear Rating is gone
    }

    @Test
    public void shouldLoadActorsForAPersistedMovie() {
        session.query(
                "CREATE " +
                        "(dh:Movie {title:'Die Hard'}), " +
                        "(bw:Actor {name: 'Bruce Willis'}), " +
                        "(bw)-[:ACTS_IN {role : 'John'}]->(dh)", Utils.map());

        //Movie dieHard = TestUtils.firstOrNull(session.loadByProperty(Movie.class, new Parameter("title", "Die Hard")));

        Movie dieHard = session.loadAll(Movie.class).iterator().next();

        Assert.assertNotNull(dieHard);
        Assert.assertNotNull(dieHard.getRoles());
        assertEquals(1, dieHard.getRoles().size());
    }


    /**
     * @see DATAGRAPH-714
     */
    @Test
    public void shouldBeAbleToModifyRating() {
        Movie movie = new Movie("Harry Potter and the Philosophers Stone", 2003);

        User vince = new User();
        vince.setName("Vince");
        vince.setLogin("bickerv");

        Set<Rating> ratings = new HashSet<>();
        Rating awesome = new Rating();
        awesome.setComment("Awesome");
        awesome.setMovie(movie);
        awesome.setUser(vince);
        awesome.setStars(5);
        ratings.add(awesome);

        vince.setRatings(ratings);
        movie.setRatings(ratings);
        session.save(movie);

        Collection<Movie> movies = session.loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Philosophers Stone"));
        movie = movies.iterator().next();
        assertEquals(1, movie.getRatings().size());
        Rating rating = movie.getRatings().iterator().next();
        assertEquals("Vince", rating.getUser().getName());
        assertEquals(5, rating.getStars());

        //Modify the rating stars and save the rating directly
        rating.setStars(3);
        session.save(rating);
        session.clear();

        movies = session.loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Philosophers Stone"));
        movie = movies.iterator().next();
        assertEquals(1, movie.getRatings().size());
        rating = movie.getRatings().iterator().next();
        assertEquals("Vince", rating.getUser().getName());
        assertEquals(3, rating.getStars());
        vince = rating.getUser();

        //Modify the rating stars and save the user (start node)
        movie.getRatings().iterator().next().setStars(2);
        session.save(vince);
        session.clear();
        movies = session.loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Philosophers Stone"));
        movie = movies.iterator().next();
        assertEquals(1, movie.getRatings().size());
        rating = movie.getRatings().iterator().next();
        assertEquals("Vince", rating.getUser().getName());
        assertEquals(2, rating.getStars());
        //Modify the rating stars and save the movie (end node)
        movie.getRatings().iterator().next().setStars(1);
        session.save(movie);
        session.clear();

        movies = session.loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Philosophers Stone"));
        movie = movies.iterator().next();
        assertEquals(1, movie.getRatings().size());
        rating = movie.getRatings().iterator().next();
        assertEquals("Vince", rating.getUser().getName());
        assertEquals(1, rating.getStars());
    }

    /**
     * @see DATAGRAPH-567
     */
    @Test
    public void shouldSaveRelationshipEntityWithCamelCaseStartEndNodes() {
        Actor bruce = new Actor("Bruce");
        Actor jim = new Actor("Jim");

        Knows knows = new Knows();
        knows.setFirstActor(bruce);
        knows.setSecondActor(jim);
        knows.setSince(new Date());

        bruce.getKnows().add(knows);

        session.save(bruce);

        Actor actor = TestUtils.firstOrNull(session.loadAll(Actor.class, new Filter("name", ComparisonOperator.EQUALS, "Bruce")));
        Assert.assertNotNull(actor);
        assertEquals(1, actor.getKnows().size());
        assertEquals("Jim", actor.getKnows().iterator().next().getSecondActor().getName());
    }


    /**
     * @see DATAGRAPH-552
     */
    @Test
    public void shouldSaveAndRetrieveRelationshipEntitiesDirectly() {
        // we need some guff in the database
        session.query(
                "CREATE " +
                        "(nc:NotAClass {name:'Colin'}), " +
                        "(g:NotAClass {age: 39}), " +
                        "(g)-[:TEST {comment : 'test'}]->(nc)", Utils.map());

        User critic = new User();
        critic.setName("Gary");
        critic.setLogin("gman");
        Movie film = new Movie("Fast and Furious XVII", 2015);
        Rating filmRating = new Rating();
        filmRating.setUser(critic);
        critic.setRatings(Collections.singleton(filmRating));
        filmRating.setMovie(film);
        film.setRatings(Collections.singleton(filmRating));
        filmRating.setStars(2);
        filmRating.setComment("They've made far too many of these films now!");

        session.save(filmRating);

        //load the rating by id
        Rating loadedRating = session.load(Rating.class, filmRating.getId());
        Assert.assertNotNull("The loaded rating shouldn't be null", loadedRating);
        assertEquals("The relationship properties weren't saved correctly", filmRating.getStars(), loadedRating.getStars());
        assertEquals("The rated film wasn't saved correctly", film.getTitle(), loadedRating.getMovie().getTitle());
        assertEquals("The critic wasn't saved correctly", critic.getId(), loadedRating.getUser().getId());
    }

    /**
     * @see DATAGRAPH-552
     */
    @Test
    public void shouldSaveAndRetrieveRelationshipEntitiesPreExistingDirectly() {

        session.query(
                "CREATE " +
                        "(ff:Movie {title:'Fast and Furious XVII'}), " +
                        "(g:User {name: 'Gary'}), " +
                        "(g)-[:RATED {comment : 'Too many of these films!'}]->(ff)", Utils.map());

        Rating loadedRating = session.loadAll(Rating.class).iterator().next();

        Assert.assertNotNull("The loaded rating shouldn't be null", loadedRating);
        assertEquals("The rated film wasn't saved correctly", "Fast and Furious XVII", loadedRating.getMovie().getTitle());
        assertEquals("The critic wasn't saved correctly", "Gary", loadedRating.getUser().getName());
    }

    /**
     * @see DATAGRAPH-569
     */
    @Test
    public void shouldBeAbleToSaveAndUpdateMultipleUserRatings() {
        Set<Rating> gobletRatings = new HashSet<>();
        Set<Rating> phoenixRatings = new HashSet<>();

        Movie goblet = new Movie("Harry Potter and the Goblet of Fire", 2006);
        session.save(goblet);

        Movie phoenix = new Movie("Harry Potter and the Order of the Phoenix", 2009);
        session.save(phoenix);

        User adam = new User();
        adam.setName("Adam");
        adam.setLogin("adamg");

        Rating good = new Rating();
        good.setUser(adam);
        good.setMovie(goblet);
        good.setStars(3);
        gobletRatings.add(good);
        goblet.setRatings(gobletRatings);

        Rating okay = new Rating();
        okay.setMovie(phoenix);
        okay.setUser(adam);
        okay.setStars(2);
        phoenixRatings.add(okay);
        phoenix.setRatings(phoenixRatings);

        Set<Rating> adamsRatings = new HashSet<>();
        adamsRatings.add(good);
        adamsRatings.add(okay);
        adam.setRatings(adamsRatings);

        session.save(adam);

        Collection<Movie> movies = session.loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Goblet of Fire"));
        goblet = movies.iterator().next();
        Assert.assertNotNull(goblet.getRatings());
        assertEquals(1, goblet.getRatings().size());

        movies = session.loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Order of the Phoenix"));
        phoenix = movies.iterator().next();
        Assert.assertNotNull(phoenix.getRatings());
        assertEquals(1, phoenix.getRatings().size());

        adam = session.loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "Adam")).iterator().next();
        assertEquals(2, adam.getRatings().size());

        adam.setRatings(gobletRatings); //Get rid of the Phoenix rating
        session.save(adam);

        adam = session.loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "Adam")).iterator().next();
        assertEquals(1, adam.getRatings().size());

        movies = session.loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Order of the Phoenix"));
        phoenix = movies.iterator().next();
        assertNull(phoenix.getRatings());

        movies = session.loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Goblet of Fire"));
        goblet = movies.iterator().next();
        Assert.assertNotNull(goblet.getRatings());
        assertEquals(1, goblet.getRatings().size());
    }

    /**
     * @see DATAGRAPH-586
     */
    @Test
    public void shouldBeAbleToDeleteAllRatings() {
        Set<Rating> gobletRatings = new HashSet<>();
        Set<Rating> phoenixRatings = new HashSet<>();

        Movie goblet = new Movie("Harry Potter and the Goblet of Fire", 2006);
        session.save(goblet);

        Movie phoenix = new Movie("Harry Potter and the Order of the Phoenix", 2009);
        session.save(phoenix);

        User adam = new User();
        adam.setName("Adam");
        adam.setLogin("adamg");
        Rating good = new Rating();
        good.setUser(adam);
        good.setMovie(goblet);
        good.setStars(3);
        gobletRatings.add(good);
        goblet.setRatings(gobletRatings);

        Rating okay = new Rating();
        okay.setMovie(phoenix);
        okay.setUser(adam);
        okay.setStars(2);
        phoenixRatings.add(okay);
        phoenix.setRatings(phoenixRatings);

        Set<Rating> adamsRatings = new HashSet<>();
        adamsRatings.add(good);
        adamsRatings.add(okay);
        adam.setRatings(adamsRatings);

        session.save(adam);

        adam = session.loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "Adam")).iterator().next();
        assertEquals(2, adam.getRatings().size());

        //delete all ratings
        session.deleteAll(Rating.class);
        assertEquals(0, session.loadAll(Rating.class).size());

        phoenix = session.loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Order of the Phoenix")).iterator().next();
        assertNull(phoenix.getRatings());

        goblet = session.loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Goblet of Fire")).iterator().next();
        assertNull(goblet.getRatings());

        adam = session.loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "Adam")).iterator().next();
        assertNull(adam.getRatings());
    }

    /**
     * @see DATAGRAPH-586
     */
    @Test
    public void shouldBeAbleToDeleteOneRating() {
        Set<Rating> gobletRatings = new HashSet<>();
        Set<Rating> phoenixRatings = new HashSet<>();

        Movie goblet = new Movie("Harry Potter and the Goblet of Fire", 2006);
        session.save(goblet);

        Movie phoenix = new Movie("Harry Potter and the Order of the Phoenix", 2009);
        session.save(phoenix);

        User adam = new User();
        adam.setName("Adam");
        adam.setLogin("adamg");

        Rating good = new Rating();
        good.setUser(adam);
        good.setMovie(goblet);
        good.setStars(3);
        gobletRatings.add(good);
        goblet.setRatings(gobletRatings);

        Rating okay = new Rating();
        okay.setMovie(phoenix);
        okay.setUser(adam);
        okay.setStars(2);
        phoenixRatings.add(okay);
        phoenix.setRatings(phoenixRatings);

        Set<Rating> adamsRatings = new HashSet<>();
        adamsRatings.add(good);
        adamsRatings.add(okay);
        adam.setRatings(adamsRatings);

        session.save(adam);

        adam = session.loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "Adam")).iterator().next();
        assertEquals(2, adam.getRatings().size());

        //delete one rating
        session.delete(okay);
        Collection<Rating> ratings = session.loadAll(Rating.class);
        assertEquals(1, ratings.size());

        phoenix = session.loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Order of the Phoenix")).iterator().next();
        assertNull(phoenix.getRatings());

        goblet = session.loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Goblet of Fire")).iterator().next();
        assertEquals(1, goblet.getRatings().size());

        adam = session.loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "Adam")).iterator().next();
        assertEquals(1, adam.getRatings().size());
    }

    /**
     * @see DATAGRAPH-610
     */
    @Test
    public void shouldSaveRelationshipEntityWithNullProperty() {
        Actor bruce = new Actor("Bruce");
        Actor jim = new Actor("Jim");

        Knows knows = new Knows(); //since = null
        knows.setFirstActor(bruce);
        knows.setSecondActor(jim);

        bruce.getKnows().add(knows);
        session.save(bruce);

        Actor actor = TestUtils.firstOrNull(session.loadAll(Actor.class, new Filter("name", ComparisonOperator.EQUALS, "Bruce")));
        Assert.assertNotNull(actor);
        assertEquals(1, actor.getKnows().size());
        assertEquals("Jim", actor.getKnows().iterator().next().getSecondActor().getName());

        bruce.getKnows().iterator().next().setSince(new Date());
        session.save(bruce);

        actor = TestUtils.firstOrNull(session.loadAll(Actor.class, new Filter("name", ComparisonOperator.EQUALS, "Bruce")));
        assertEquals(1, actor.getKnows().size());
        assertNotNull(actor.getKnows().iterator().next().getSince());

        bruce.getKnows().iterator().next().setSince(null);
        session.save(bruce);

        actor = TestUtils.firstOrNull(session.loadAll(Actor.class, new Filter("name", ComparisonOperator.EQUALS, "Bruce")));
        assertEquals(1, actor.getKnows().size());
        assertNull(actor.getKnows().iterator().next().getSince());
    }

    /**
     * @see DATAGRAPH-616
     */
    @Test
    public void shouldLoadRelationshipEntityWithSameStartEndNodeType() {
        Actor bruce = new Actor("Bruce");
        Actor jim = new Actor("Jim");

        Knows knows = new Knows();
        knows.setFirstActor(bruce);
        knows.setSecondActor(jim);
        knows.setSince(new Date());

        bruce.getKnows().add(knows);

        session.save(bruce);

        session.clear();

        Actor actor = TestUtils.firstOrNull(session.loadAll(Actor.class, new Filter("name", ComparisonOperator.EQUALS, "Bruce")));
        Assert.assertNotNull(actor);
        assertEquals(1, actor.getKnows().size());
        assertEquals("Bruce", actor.getKnows().iterator().next().getFirstActor().getName());
        assertEquals("Jim", actor.getKnows().iterator().next().getSecondActor().getName());
    }

    /**
     * @see DATAGRAPH-552
     */
    @Test
    public void shouldHydrateTheEndNodeOfAnRECorrectly() {
        //TODO add some more end node hydration tests
        Movie movie = new Movie("Pulp Fiction", 1994);
        Actor actor = new Actor("John Travolta");
        actor.playedIn(movie, "Vincent");
        session.save(movie);

        User michal = new User();
        michal.setName("Michal");
        michal.setLogin("bachmania");

        Rating awesome = new Rating();
        awesome.setMovie(movie);
        awesome.setUser(michal);
        awesome.setStars(5);
        michal.setRatings(Collections.singleton(awesome));
        session.save(michal);

        //Check that Pulp Fiction has one rating from Michal
        Collection<Movie> films = session.loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Pulp Fiction"));
        assertEquals(1, films.size());

        Movie film = films.iterator().next();
        Assert.assertNotNull(film);
        assertEquals(1, film.getRatings().size());
        assertEquals("Michal", film.getRatings().iterator().next().getUser().getName());
        assertEquals("Vincent", film.getRoles().iterator().next().getRole());

        session.clear();
        Rating rating = session.load(Rating.class, awesome.getId(), 2);
        assertNotNull(rating);
        Movie loadedMovie = rating.getMovie();
        assertNotNull(loadedMovie);
        assertEquals("Pulp Fiction", loadedMovie.getTitle());
        assertEquals("Vincent", loadedMovie.getRoles().iterator().next().getRole());
    }

    @Test
    public void shouldSaveMultipleRoleRelationshipsBetweenTheSameTwoObjects() {

        Movie movie = new Movie("The big John Travolta Party", 2016);
        Actor actor = new Actor("John Travolta");

        for (int i = 65; i <= 90; i++) {
            String role = new String(new char[]{(char) i});
            actor.playedIn(movie, role);
        }

        assertEquals(26, actor.getRoles().size());
        session.save(actor);

        session.clear();
        Actor loadedActor = session.load(Actor.class, actor.getUuid());

        assertEquals(26, loadedActor.getRoles().size());
    }

    @Test
    public void shouldSaveSingleRoleRelationshipBetweenTheSameTwoObjects() {

        Movie movie = new Movie("The big John Travolta Party", 2016);

        Actor actor = new Actor("John Travolta");
        actor.playedIn(movie, "He danced mostly");

        assertEquals(1, actor.getRoles().size());
        session.save(actor);

        session.clear();
        Actor loadedActor = session.load(Actor.class, actor.getUuid());

        assertEquals(1, loadedActor.getRoles().size());

        //Add an identical role
        actor.playedIn(movie, "He danced mostly");
        assertEquals(2, actor.getRoles().size());
        session.save(actor);

        session.clear();
        loadedActor = session.load(Actor.class, actor.getUuid());

        assertEquals(1, loadedActor.getRoles().size());
    }

    /**
     * @see DATAGRAPH-704
     */
    @Test
    public void shouldRetainREsWhenAStartOrEndNodeIsLoaded() {
        bootstrap("org/neo4j/ogm/cql/cineasts.cql");

        Collection<Movie> films = session.loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Top Gear"));
        Movie movie = films.iterator().next();
        assertEquals(2, movie.getRatings().size());

        session.loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "Michal")).iterator().next();

        assertEquals(2, movie.getRatings().size()); //should not lose one rating because Michal is loaded; ratings should me merged and not overwritten
    }

    /**
     * For DATAGRAPH-761
     */
    @Test
    public void shouldLoadFilmsByTitleUsingCaseInsensitiveWildcardBasedLikeExpression() {
        Movie firstFilm = new Movie("Dirty Harry", 1977);
        Movie secondFilm = new Movie("Harry Potter and the Order of the Phoenix", 2009);
        Movie thirdFilm = new Movie("Delhi Belly", 2012);

        session.save(firstFilm);
        session.save(secondFilm);
        session.save(thirdFilm);

        Filter filter = new Filter("title", ComparisonOperator.LIKE, "harry*");

        Collection<Movie> allFilms = session.loadAll(Movie.class, filter);
        assertEquals("The wrong-sized collection of films was returned", 1, allFilms.size());
        assertEquals("Harry Potter and the Order of the Phoenix", allFilms.iterator().next().getTitle());
    }


    @Test
    public void shouldLoadASingleRating() {
        Movie movie = new Movie("Pulp Fiction", 1994);
        session.save(movie);

        User michal = new User();
        michal.setName("Michal");
        michal.setLogin("bachmania");

        Rating awesome = new Rating();
        awesome.setMovie(movie);
        awesome.setUser(michal);
        awesome.setStars(5);
        michal.setRatings(Collections.singleton(awesome));
        session.save(michal);

        //Add a rating from luanne for the same movie
        User luanne = new User();
        luanne.setName("luanne");
        luanne.setLogin("luanne");
        luanne.setPassword("luanne");

        Rating rating = new Rating();
        rating.setMovie(movie);
        rating.setUser(luanne);
        rating.setStars(3);
        luanne.setRatings(Collections.singleton(rating));
        session.save(luanne);

        session.clear();

        Rating loadedRating = session.load(Rating.class, rating.getId(), 3);
        assertNotNull(loadedRating);
        assertEquals(rating.getId(), loadedRating.getId());
        assertEquals(rating.getStars(), loadedRating.getStars());
    }

    @Test
    public void shouldSortRatings() {
        Movie movie = new Movie("Pulp Fiction", 1994);
        session.save(movie);

        User michal = new User();
        michal.setName("Michal");
        michal.setLogin("bachmania");
        Rating awesome = new Rating();
        awesome.setMovie(movie);
        awesome.setUser(michal);
        awesome.setStars(5);
        michal.setRatings(Collections.singleton(awesome));
        session.save(michal);

        //Add a rating from luanne for the same movie
        User luanne = new User();
        luanne.setName("luanne");
        luanne.setLogin("luanne");
        luanne.setPassword("luanne");

        Rating rating = new Rating();
        rating.setMovie(movie);
        rating.setUser(luanne);
        rating.setStars(3);
        luanne.setRatings(Collections.singleton(rating));
        session.save(luanne);

        session.clear();
        Collection<Rating> ratings = session.loadAll(Rating.class, new SortOrder().add(SortOrder.Direction.ASC, "stars"));
        assertNotNull(ratings);
        int i = 0;
        for (Rating r : ratings) {
            if (i++ == 0) {
                assertEquals(3, r.getStars());
            } else {
                assertEquals(5, r.getStars());
            }
        }
    }

    /**
     * @throws MalformedURLException
     * @see Issue #128
     */
    @Test
    public void shouldBeAbleToSetREPropertiesToNull() throws MalformedURLException {
        Movie movie = new Movie("Pulp Fiction", 1994);
        session.save(movie);

        User michal = new User();
        michal.setName("Michal");
        michal.setLogin("bachmania");

        Rating awesome = new Rating();
        awesome.setMovie(movie);
        awesome.setUser(michal);
        awesome.setStars(5);
        awesome.setComment("Just awesome");
        michal.setRatings(Collections.singleton(awesome));
        session.save(michal);

        awesome.setComment(null);

        session.save(awesome);

        awesome = session.load(Rating.class, awesome.getId());
        assertNull(awesome.getComment());
        assertEquals(5, awesome.getStars());
        session.clear();

        awesome = session.load(Rating.class, awesome.getId());
        assertNull(awesome.getComment());
        assertEquals(5, awesome.getStars());

        //make sure there's still just one rating
        session.clear();
        movie = session.load(Movie.class, movie.getUuid());
        assertEquals(1, movie.getRatings().size());
        assertNull(movie.getRatings().iterator().next().getComment());
    }

    private void bootstrap(String cqlFileName) {
        session.query(TestUtils.readCQLFile(cqlFileName).toString(), Utils.map());
    }
}
