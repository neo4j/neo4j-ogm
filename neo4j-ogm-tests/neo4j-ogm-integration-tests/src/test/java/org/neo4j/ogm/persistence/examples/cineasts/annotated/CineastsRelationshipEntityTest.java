/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.ogm.persistence.examples.cineasts.annotated;

import static org.assertj.core.api.Assertions.*;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.domain.cineasts.annotated.Actor;
import org.neo4j.ogm.domain.cineasts.annotated.Knows;
import org.neo4j.ogm.domain.cineasts.annotated.Movie;
import org.neo4j.ogm.domain.cineasts.annotated.Rating;
import org.neo4j.ogm.domain.cineasts.annotated.Role;
import org.neo4j.ogm.domain.cineasts.annotated.User;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.testutil.TestUtils;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 * @author Michael J. Simons
 */
public class CineastsRelationshipEntityTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.cineasts.annotated");
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
        Collection<Movie> films = session
            .loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Pulp Fiction"));
        assertThat(films).hasSize(1);

        Movie film = films.iterator().next();
        assertThat(film).isNotNull();
        assertThat(film.getRatings()).hasSize(1);
        assertThat(film.getRatings().iterator().next().getUser().getName()).isEqualTo("Michal");

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
        assertThat(film.getRatings()).hasSize(2);   //Fail, it has just one rating, luannes

        //Verify that luanne's rating is saved
        User foundLuanne = session.load(User.class, "luanne");
        assertThat(foundLuanne.getRatings()).hasSize(1);

        //Verify that Michals rating still exists
        Collection<User> users = session.loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "Michal"));
        User foundMichal = users.iterator().next();
        assertThat(foundMichal.getRatings()).hasSize(1); //Fail, Michals rating is gone
    }

    @Test
    public void shouldCreateREWithExistingStartAndEndNodes() {

        bootstrap("org/neo4j/ogm/cql/cineasts.cql");

        Collection<Movie> films = session
            .loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Top Gear"));
        Movie movie = films.iterator().next();
        assertThat(movie.getRatings()).hasSize(2);

        User michal = null;
        for (Rating rating : movie.getRatings()) {
            if (rating.getUser().getName().equals("Michal")) {
                michal = rating.getUser();
                break;
            }
        }
        assertThat(michal).isNotNull();
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

        Collection<Movie> movies = session
            .loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Top Gear"));
        movie = movies.iterator().next();
        assertThat(movie.getRatings())
            .isNotNull(); //Fails. But when entities are created first, test passes, see CineastsRatingsTest.shouldSaveRatingWithMovie
        assertThat(movie.getRatings()).hasSize(1);
        assertThat(movie.getRatings().iterator().next().getUser().getName()).isEqualTo("Michal");
    }

    @Test
    public void shouldNotLoseRelationshipEntitiesWhenALoadedEntityIsPersisted() {

        bootstrap("org/neo4j/ogm/cql/cineasts.cql");

        Movie topGear = session.loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Top Gear"))
            .iterator().next();
        assertThat(topGear.getRatings()).hasSize(2);  //2 ratings
        session.save(topGear);

        topGear = session.loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Top Gear")).iterator()
            .next();
        assertThat(topGear.getRatings()).hasSize(2);  //Then there was one

        User michal = session.loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "Michal")).iterator()
            .next();
        assertThat(michal.getRatings()).hasSize(2);  //The Top Gear Rating is gone
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

        assertThat(dieHard).isNotNull();
        assertThat(dieHard.getRoles()).isNotNull();
        assertThat(dieHard.getRoles()).hasSize(1);
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

        Collection<Movie> movies = session.loadAll(Movie.class,
            new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Philosophers Stone"));
        movie = movies.iterator().next();
        assertThat(movie.getRatings()).hasSize(1);
        Rating rating = movie.getRatings().iterator().next();
        assertThat(rating.getUser().getName()).isEqualTo("Vince");
        assertThat(rating.getStars()).isEqualTo(5);

        //Modify the rating stars and save the rating directly
        rating.setStars(3);
        session.save(rating);
        session.clear();

        movies = session.loadAll(Movie.class,
            new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Philosophers Stone"));
        movie = movies.iterator().next();
        assertThat(movie.getRatings()).hasSize(1);
        rating = movie.getRatings().iterator().next();
        assertThat(rating.getUser().getName()).isEqualTo("Vince");
        assertThat(rating.getStars()).isEqualTo(3);
        vince = rating.getUser();

        //Modify the rating stars and save the user (start node)
        movie.getRatings().iterator().next().setStars(2);
        session.save(vince);
        session.clear();
        movies = session.loadAll(Movie.class,
            new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Philosophers Stone"));
        movie = movies.iterator().next();
        assertThat(movie.getRatings()).hasSize(1);
        rating = movie.getRatings().iterator().next();
        assertThat(rating.getUser().getName()).isEqualTo("Vince");
        assertThat(rating.getStars()).isEqualTo(2);
        //Modify the rating stars and save the movie (end node)
        movie.getRatings().iterator().next().setStars(1);
        session.save(movie);
        session.clear();

        movies = session.loadAll(Movie.class,
            new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Philosophers Stone"));
        movie = movies.iterator().next();
        assertThat(movie.getRatings()).hasSize(1);
        rating = movie.getRatings().iterator().next();
        assertThat(rating.getUser().getName()).isEqualTo("Vince");
        assertThat(rating.getStars()).isEqualTo(1);
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

        Actor actor = TestUtils
            .firstOrNull(session.loadAll(Actor.class, new Filter("name", ComparisonOperator.EQUALS, "Bruce")));
        assertThat(actor).isNotNull();
        assertThat(actor.getKnows()).hasSize(1);
        assertThat(actor.getKnows().iterator().next().getSecondActor().getName()).isEqualTo("Jim");
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
        assertThat(loadedRating).as("The loaded rating shouldn't be null").isNotNull();
        assertThat(loadedRating.getStars()).as("The relationship properties weren't saved correctly")
            .isEqualTo(filmRating.getStars());
        assertThat(loadedRating.getMovie().getTitle()).as("The rated film wasn't saved correctly")
            .isEqualTo(film.getTitle());
        assertThat(loadedRating.getUser().getId()).as("The critic wasn't saved correctly").isEqualTo(critic.getId());
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

        assertThat(loadedRating).as("The loaded rating shouldn't be null").isNotNull();
        assertThat(loadedRating.getMovie().getTitle()).as("The rated film wasn't saved correctly")
            .isEqualTo("Fast and Furious XVII");
        assertThat(loadedRating.getUser().getName()).as("The critic wasn't saved correctly").isEqualTo("Gary");
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

        Collection<Movie> movies = session.loadAll(Movie.class,
            new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Goblet of Fire"));
        goblet = movies.iterator().next();
        assertThat(goblet.getRatings()).isNotNull();
        assertThat(goblet.getRatings()).hasSize(1);

        movies = session.loadAll(Movie.class,
            new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Order of the Phoenix"));
        phoenix = movies.iterator().next();
        assertThat(phoenix.getRatings()).isNotNull();
        assertThat(phoenix.getRatings()).hasSize(1);

        adam = session.loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "Adam")).iterator().next();
        assertThat(adam.getRatings()).hasSize(2);

        adam.setRatings(gobletRatings); //Get rid of the Phoenix rating
        session.save(adam);

        adam = session.loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "Adam")).iterator().next();
        assertThat(adam.getRatings()).hasSize(1);

        movies = session.loadAll(Movie.class,
            new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Order of the Phoenix"));
        phoenix = movies.iterator().next();
        assertThat(phoenix.getRatings()).isNullOrEmpty();

        movies = session.loadAll(Movie.class,
            new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Goblet of Fire"));
        goblet = movies.iterator().next();
        assertThat(goblet.getRatings()).isNotNull();
        assertThat(goblet.getRatings()).hasSize(1);
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
        assertThat(adam.getRatings()).hasSize(2);

        //delete all ratings
        session.deleteAll(Rating.class);
        assertThat(session.loadAll(Rating.class)).isEmpty();

        phoenix = session.loadAll(Movie.class,
            new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Order of the Phoenix")).iterator()
            .next();
        assertThat(phoenix.getRatings()).isNull();

        goblet = session
            .loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Goblet of Fire"))
            .iterator().next();
        assertThat(goblet.getRatings()).isNull();

        adam = session.loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "Adam")).iterator().next();
        assertThat(adam.getRatings()).isNull();
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
        assertThat(adam.getRatings()).hasSize(2);

        //delete one rating
        session.delete(okay);
        Collection<Rating> ratings = session.loadAll(Rating.class);
        assertThat(ratings).hasSize(1);

        phoenix = session.loadAll(Movie.class,
            new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Order of the Phoenix")).iterator()
            .next();
        assertThat(phoenix.getRatings()).isNull();

        goblet = session
            .loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Harry Potter and the Goblet of Fire"))
            .iterator().next();
        assertThat(goblet.getRatings()).hasSize(1);

        adam = session.loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "Adam")).iterator().next();
        assertThat(adam.getRatings()).hasSize(1);
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

        Actor actor = TestUtils
            .firstOrNull(session.loadAll(Actor.class, new Filter("name", ComparisonOperator.EQUALS, "Bruce")));
        assertThat(actor).isNotNull();
        assertThat(actor.getKnows()).hasSize(1);
        assertThat(actor.getKnows().iterator().next().getSecondActor().getName()).isEqualTo("Jim");

        bruce.getKnows().iterator().next().setSince(new Date());
        session.save(bruce);

        actor = TestUtils
            .firstOrNull(session.loadAll(Actor.class, new Filter("name", ComparisonOperator.EQUALS, "Bruce")));
        assertThat(actor.getKnows()).hasSize(1);
        assertThat(actor.getKnows().iterator().next().getSince()).isNotNull();

        bruce.getKnows().iterator().next().setSince(null);
        session.save(bruce);

        actor = TestUtils
            .firstOrNull(session.loadAll(Actor.class, new Filter("name", ComparisonOperator.EQUALS, "Bruce")));
        assertThat(actor.getKnows()).hasSize(1);
        assertThat(actor.getKnows().iterator().next().getSince()).isNull();
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

        Actor actor = TestUtils
            .firstOrNull(session.loadAll(Actor.class, new Filter("name", ComparisonOperator.EQUALS, "Bruce")));
        assertThat(actor).isNotNull();
        assertThat(actor.getKnows()).hasSize(1);
        assertThat(actor.getKnows().iterator().next().getFirstActor().getName()).isEqualTo("Bruce");
        assertThat(actor.getKnows().iterator().next().getSecondActor().getName()).isEqualTo("Jim");
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
        Collection<Movie> films = session
            .loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Pulp Fiction"));
        assertThat(films).hasSize(1);

        Movie film = films.iterator().next();
        assertThat(film).isNotNull();
        assertThat(film.getRatings()).hasSize(1);
        assertThat(film.getRatings().iterator().next().getUser().getName()).isEqualTo("Michal");
        assertThat(film.getRoles().iterator().next().getRole()).isEqualTo("Vincent");

        session.clear();
        Rating rating = session.load(Rating.class, awesome.getId(), 2);
        assertThat(rating).isNotNull();
        Movie loadedMovie = rating.getMovie();
        assertThat(loadedMovie).isNotNull();
        assertThat(loadedMovie.getTitle()).isEqualTo("Pulp Fiction");
        assertThat(loadedMovie.getRoles().iterator().next().getRole()).isEqualTo("Vincent");
    }

    @Test
    public void shouldSaveMultipleRoleRelationshipsBetweenTheSameTwoObjects() {

        Movie movie = new Movie("The big John Travolta Party", 2016);
        Actor actor = new Actor("John Travolta");

        for (int i = 65; i <= 90; i++) {
            String role = new String(new char[] { (char) i });
            actor.playedIn(movie, role);
        }

        assertThat(actor.getRoles()).hasSize(26);
        session.save(actor);

        session.clear();
        Actor loadedActor = session.load(Actor.class, actor.getUuid());

        assertThat(loadedActor.getRoles()).hasSize(26);
    }

    @Test
    public void shouldSaveSameRoleTwiceRelationshipBetweenTheSameTwoObjects() {

        Movie movie = new Movie("The big John Travolta Party", 2016);

        Actor actor = new Actor("John Travolta");
        actor.playedIn(movie, "He danced mostly");

        assertThat(actor.getRoles()).hasSize(1);
        session.save(actor);

        session.clear();
        Actor loadedActor = session.load(Actor.class, actor.getUuid());

        assertThat(loadedActor.getRoles()).hasSize(1);

        //Add an identical role
        actor.playedIn(movie, "He danced mostly");
        assertThat(actor.getRoles()).hasSize(2);
        session.save(actor);

        session.clear();
        loadedActor = session.load(Actor.class, actor.getUuid());

        assertThat(loadedActor.getRoles()).hasSize(2);
    }

    @Test
    public void updateRoleToSameValueResultsInTwoRelationshipBetweenSameObjects() throws Exception {
        Movie movie = new Movie("The big John Travolta Party", 2016);

        Actor actor = new Actor("John Travolta");
        Role role1 = actor.playedIn(movie, "He danced mostly");
        Role role2 = actor.playedIn(movie, "He was dancing mostly");

        assertThat(actor.getRoles()).hasSize(2);

        session.save(actor);

        session.clear();
        Actor loaded = session.load(Actor.class, actor.getUuid());
        assertThat(loaded.getRoles()).hasSize(2);

        // set to identical role - this should behave consistently to previous test case
        role2.setRole("He danced mostly");
        session.save(actor);

        session.clear();
        loaded = session.load(Actor.class, actor.getUuid());
        assertThat(loaded.getRoles()).hasSize(2);
    }

    /**
     * @see DATAGRAPH-704
     */
    @Test
    public void shouldRetainREsWhenAStartOrEndNodeIsLoaded() {
        bootstrap("org/neo4j/ogm/cql/cineasts.cql");

        Collection<Movie> films = session
            .loadAll(Movie.class, new Filter("title", ComparisonOperator.EQUALS, "Top Gear"));
        Movie movie = films.iterator().next();
        assertThat(movie.getRatings()).hasSize(2);

        session.loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "Michal")).iterator().next();

        assertThat(movie.getRatings()).hasSize(
            2); //should not lose one rating because Michal is loaded; ratings should me merged and not overwritten
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
        assertThat(allFilms).as("The wrong-sized collection of films was returned").hasSize(1);
        assertThat(allFilms.iterator().next().getTitle()).isEqualTo("Harry Potter and the Order of the Phoenix");
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
        assertThat(loadedRating).isNotNull();
        assertThat(loadedRating.getId()).isEqualTo(rating.getId());
        assertThat(loadedRating.getStars()).isEqualTo(rating.getStars());
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
        Collection<Rating> ratings = session
            .loadAll(Rating.class, new SortOrder().add(SortOrder.Direction.ASC, "stars"));
        assertThat(ratings).isNotNull();
        int i = 0;
        for (Rating r : ratings) {
            if (i++ == 0) {
                assertThat(r.getStars()).isEqualTo(3);
            } else {
                assertThat(r.getStars()).isEqualTo(5);
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
        assertThat(awesome.getComment()).isNull();
        assertThat(awesome.getStars()).isEqualTo(5);
        session.clear();

        awesome = session.load(Rating.class, awesome.getId());
        assertThat(awesome.getComment()).isNull();
        assertThat(awesome.getStars()).isEqualTo(5);

        //make sure there's still just one rating
        session.clear();
        movie = session.load(Movie.class, movie.getUuid());
        assertThat(movie.getRatings()).hasSize(1);
        assertThat(movie.getRatings().iterator().next().getComment()).isNull();
    }

    @Test
    public void testFilterOnRelationshipEntity() throws Exception {
        Movie pulpFiction = new Movie("Pulp Fiction", 1994);

        Movie ootf = new Movie("Harry Potter and the Order of the Phoenix", 2009);

        User frantisek = new User();
        frantisek.setName("Frantisek");
        frantisek.setLogin("frantisek");

        Rating pulpRating = new Rating();
        pulpRating.setStars(3);
        pulpRating.setMovie(pulpFiction);
        pulpRating.setUser(frantisek);
        pulpFiction.setRatings(new HashSet<>(Arrays.asList(pulpRating)));

        Rating ootfRating = new Rating();
        ootfRating.setStars(3);
        ootfRating.setMovie(ootf);
        ootfRating.setUser(frantisek);

        frantisek.setRatings(new HashSet<>(Arrays.asList(ootfRating, pulpRating)));

        User otto = new User();
        otto.setName("Otto");
        otto.setLogin("otto");

        Rating pulpRating2 = new Rating();
        pulpRating2.setStars(3);
        pulpRating2.setMovie(pulpFiction);
        pulpRating2.setUser(otto);

        Rating ootfRating2 = new Rating();
        ootfRating2.setStars(3);
        ootfRating2.setMovie(ootf);
        ootfRating2.setUser(otto);

        pulpFiction.setRatings(new HashSet<>(Arrays.asList(pulpRating, pulpRating2)));
        ootf.setRatings(new HashSet<>(Arrays.asList(ootfRating, ootfRating2)));
        otto.setRatings(new HashSet<>(Arrays.asList(pulpRating2, ootfRating2)));
        session.save(otto);

        session.clear();

        Filter filter = new Filter("stars", ComparisonOperator.EQUALS, 3);
        filter.setNestedPropertyName("ratings");
        filter.setNestedPropertyType(Rating.class);
        filter.setNestedRelationshipEntity(true);
        Collection<User> users = session.loadAll(User.class, filter, new Pagination(0, 2));

        assertThat(users).hasSize(2);
    }

    @Test
    public void testFilterOnRelatedNode() throws Exception {
        User frantisek = new User();
        frantisek.setName("Frantisek");
        frantisek.setLogin("frantisek");

        User michal = new User();
        michal.setName("Michal");
        michal.setLogin("michal");

        User ottoH = new User();
        ottoH.setName("Otto");
        ottoH.setLogin("otto");

        User ottoB = new User();
        ottoB.setName("Otto von Bismarc");
        ottoB.setLogin("ottob");

        frantisek.addFriends(ottoH, ottoB);
        session.save(frantisek);

        michal.addFriends(ottoH, ottoB);
        session.save(michal);

        Filter filter = new Filter("name", ComparisonOperator.CONTAINING, "Otto"); // name contains ' '
        filter.setNestedPropertyName("friends");
        filter.setNestedPropertyType(User.class);
        SortOrder sortOrder = new SortOrder();

        sortOrder.add("name");
        Collection<User> users = session.loadAll(User.class, filter, sortOrder, new Pagination(0, 2));

        assertThat(users).hasSize(2);
    }

    private void bootstrap(String cqlFileName) {
        session.query(TestUtils.readCQLFile(cqlFileName).toString(), Utils.map());
    }
}
