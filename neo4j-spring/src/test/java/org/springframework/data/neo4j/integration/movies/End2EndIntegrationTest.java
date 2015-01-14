package org.springframework.data.neo4j.integration.movies;

import com.graphaware.test.integration.WrappingServerIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterables;
import org.springframework.data.neo4j.integration.movies.context.PersistenceContext;
import org.springframework.data.neo4j.integration.movies.domain.*;
import org.springframework.data.neo4j.integration.movies.repo.AbstractEntityRepository;
import org.springframework.data.neo4j.integration.movies.repo.CinemaRepository;
import org.springframework.data.neo4j.integration.movies.repo.AbstractAnnotatedEntityRepository;
import org.springframework.data.neo4j.integration.movies.repo.UserRepository;
import org.springframework.data.neo4j.integration.movies.service.UserService;
import org.neo4j.tooling.GlobalGraphOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.graphaware.test.unit.GraphUnit.assertSameGraph;
import static org.junit.Assert.*;

@ContextConfiguration(classes = {PersistenceContext.class})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class End2EndIntegrationTest extends WrappingServerIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private AbstractAnnotatedEntityRepository abstractAnnotatedEntityRepository;

    @Autowired
    private AbstractEntityRepository abstractEntityRepository;

    @Override
    protected int neoServerPort() {
        return 7879;
    }

    @Test
    public void shouldSaveUser() {
        User user = new User("Michal");
        userRepository.save(user);

        assertSameGraph(getDatabase(), "CREATE (u:User {name:'Michal'})");
        assertEquals(0L, (long) user.getId());
    }

    @Test
    public void shouldSaveUserWithoutName() {
        User user = new User();
        userRepository.save(user);

        assertSameGraph(getDatabase(), "CREATE (u:User)");
        assertEquals(0L, (long) user.getId());
    }

    @Test
    @Ignore //todo fix
    public void shouldSaveReleasedMovie() {
        Calendar cinemaReleaseDate = Calendar.getInstance();
        cinemaReleaseDate.set(1994, Calendar.SEPTEMBER, 10);

        Calendar cannesReleaseDate = Calendar.getInstance();
        cannesReleaseDate.set(1994, Calendar.MAY, 12);

        ReleasedMovie releasedMovie = new ReleasedMovie("Pulp Fiction", cinemaReleaseDate.getTime(), cannesReleaseDate.getTime());

        abstractAnnotatedEntityRepository.save(releasedMovie);

        //todo assert graph contents when test passes
    }

    @Test
    @Ignore //todo fix
    public void shouldSaveReleasedMovie2() {
        Calendar cannesReleaseDate = Calendar.getInstance();
        cannesReleaseDate.set(1994, Calendar.MAY, 12);

        ReleasedMovie releasedMovie = new ReleasedMovie("Pulp Fiction", null, cannesReleaseDate.getTime());

        abstractAnnotatedEntityRepository.save(releasedMovie);

        //todo assert graph contents when test passes
    }

    @Test
    public void shouldSaveMovie() {
        Movie movie = new Movie("Pulp Fiction");
        movie.setTags(new String[]{"cool", "classic"});
        movie.setImage(new byte[]{1, 2, 3});

        abstractEntityRepository.save(movie);

        // byte arrays have to be transferred with a JSON-supported format. Base64 is the default.
        assertSameGraph(getDatabase(), "CREATE (m:Movie {title:'Pulp Fiction', tags:['cool','classic'], image:'AQID'})");
    }

    @Test
    public void shouldSaveUsers() {
        Set<User> set = new HashSet<>();
        set.add(new User("Michal"));
        set.add(new User("Adam"));
        set.add(new User("Vince"));

        userRepository.save(set);

        assertSameGraph(getDatabase(), "CREATE (:User {name:'Michal'})," +
                "(:User {name:'Vince'})," +
                "(:User {name:'Adam'})");

        assertEquals(3, userRepository.count());
    }

    @Test
    public void shouldSaveUsers2() {
        List<User> list = new LinkedList<>();
        list.add(new User("Michal"));
        list.add(new User("Adam"));
        list.add(new User("Vince"));

        userRepository.save(list);

        assertSameGraph(getDatabase(), "CREATE (:User {name:'Michal'})," +
                "(:User {name:'Vince'})," +
                "(:User {name:'Adam'})");

        assertEquals(3, userRepository.count());
    }

    @Test
    public void shouldUpdateUserUsingRepository() {
        User user = userRepository.save(new User("Michal"));
        user.setName("Adam");
        userRepository.save(user);

        assertSameGraph(getDatabase(), "CREATE (u:User {name:'Adam'})");
        assertEquals(0L, (long) user.getId());
    }

    @Test
    @Ignore
    // this test expects the session/tx to check for dirty objects, which it currently does not do
    // you must save objects explicitly.
    public void shouldUpdateUserUsingTransactionalService() {
        User user = new User("Michal");
        userRepository.save(user);

        userService.updateUser(user, "Adam"); //notice userRepository.save(..) isn't called, not even in the service impl!

        assertSameGraph(getDatabase(), "CREATE (u:User {name:'Adam'})");
        assertEquals(0L, (long) user.getId());
    }

    @Test
    public void shouldFindUser() {
        User user = new User("Michal");
        userRepository.save(user);

        User loaded = userRepository.findOne(0L);

        assertEquals(0L, (long) loaded.getId());
        assertEquals("Michal", loaded.getName());

        assertTrue(loaded.equals(user));
        assertTrue(loaded == user);
    }

    @Test
    public void shouldFindUserWithoutName() {
        User user = new User();
        userRepository.save(user);

        User loaded = userRepository.findOne(0L);

        assertEquals(0L, (long) loaded.getId());
        assertNull(loaded.getName());

        assertTrue(loaded.equals(user));
        assertTrue(loaded == user);
    }

    @Test
    public void shouldDeleteUser() {
        User user = new User("Michal");
        userRepository.save(user);
        userRepository.delete(user);

        assertFalse(userRepository.findAll().iterator().hasNext());
        assertFalse(userRepository.findAll(1).iterator().hasNext());
        assertFalse(userRepository.exists(0L));
        assertEquals(0, userRepository.count());
        assertNull(userRepository.findOne(0L));
        assertNull(userRepository.findOne(0L, 10));

        try (Transaction tx = getDatabase().beginTx()) {
            assertFalse(GlobalGraphOperations.at(getDatabase()).getAllNodes().iterator().hasNext());
            tx.success();
        }
    }

    @Test
    public void shouldCreateUsersInMultipleThreads() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 100; i++) {
            final int j = i;
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    userRepository.save(new User("User" + j));
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        assertEquals(100, userRepository.count());

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals(100, Iterables.count(GlobalGraphOperations.at(getDatabase()).getAllNodes()));
            tx.success();
        }
    }

    @Test
    public void shouldSaveUserAndNewGenre() {
        User user = new User("Michal");
        user.interestedIn(new Genre("Drama"));

        userRepository.save(user);

        assertSameGraph(getDatabase(), "CREATE (u:User {name:'Michal'})-[:INTERESTED]->(g:Genre {name:'Drama'})");
    }

    @Test
    public void shouldSaveUserAndNewGenres() {
        User user = new User("Michal");
        user.interestedIn(new Genre("Drama"));
        user.interestedIn(new Genre("Historical"));
        user.interestedIn(new Genre("Thriller"));

        userRepository.save(user);

        assertSameGraph(getDatabase(), "CREATE " +
                "(u:User {name:'Michal'})," +
                "(g1:Genre {name:'Drama'})," +
                "(g2:Genre {name:'Historical'})," +
                "(g3:Genre {name:'Thriller'})," +
                "(u)-[:INTERESTED]->(g1)," +
                "(u)-[:INTERESTED]->(g2)," +
                "(u)-[:INTERESTED]->(g3)");
    }

    @Test
    public void shouldSaveUserAndNewGenre2() {
        User user = new User("Michal");
        user.interestedIn(new Genre("Drama"));

        userRepository.save(user, 1);

        assertSameGraph(getDatabase(), "CREATE (u:User {name:'Michal'})-[:INTERESTED]->(g:Genre {name:'Drama'})");
    }

    @Test
    public void shouldSaveUserAndExistingGenre() {
        User michal = new User("Michal");
        Genre drama = new Genre("Drama");
        michal.interestedIn(drama);

        userRepository.save(michal);

        User vince = new User("Vince");
        vince.interestedIn(drama);

        userRepository.save(vince);

        assertSameGraph(getDatabase(), "CREATE " +
                "(m:User {name:'Michal'})," +
                "(v:User {name:'Vince'})," +
                "(g:Genre {name:'Drama'})," +
                "(m)-[:INTERESTED]->(g)," +
                "(v)-[:INTERESTED]->(g)");
    }

    @Test
    public void shouldSaveUserButNotGenre() {
        User user = new User("Michal");
        user.interestedIn(new Genre("Drama"));

        userRepository.save(user, 0);

        assertSameGraph(getDatabase(), "CREATE (u:User {name:'Michal'})");
    }

    @Test
    public void shouldUpdateGenreWhenSavedThroughUser() {
        User michal = new User("Michal");
        Genre drama = new Genre("Drama");
        michal.interestedIn(drama);

        userRepository.save(michal);

        drama.setName("New Drama");

        userRepository.save(michal);

        assertSameGraph(getDatabase(), "CREATE " +
                "(m:User {name:'Michal'})," +
                "(g:Genre {name:'New Drama'})," +
                "(m)-[:INTERESTED]->(g)");
    }

    @Test
    public void shouldRemoveGenreFromUser() {
        User michal = new User("Michal");
        Genre drama = new Genre("Drama");
        michal.interestedIn(drama);

        userRepository.save(michal);

        michal.notInterestedIn(drama);

        userRepository.save(michal);

        assertSameGraph(getDatabase(), "CREATE " +
                "(m:User {name:'Michal'})," +
                "(g:Genre {name:'Drama'})");
    }

    @Test
    public void shouldRemoveGenreFromUserUsingService() {
        User michal = new User("Michal");
        Genre drama = new Genre("Drama");
        michal.interestedIn(drama);

        userRepository.save(michal);

        userService.notInterestedIn(michal.getId(), drama.getId());

        assertSameGraph(getDatabase(), "CREATE " +
                "(m:User {name:'Michal'})," +
                "(g:Genre {name:'Drama'})");
    }

    @Test
    public void shouldAddNewVisitorToCinema() {
        Cinema cinema = new Cinema("Odeon");
        cinema.addVisitor(new User("Michal"));

        cinemaRepository.save(cinema);

        assertSameGraph(getDatabase(), "CREATE " +
                "(m:User {name:'Michal'})," +
                "(c:Cinema {name:'Odeon'})," +
                "(m)-[:VISITED]->(c)");
    }

    @Test
    public void shouldAddExistingVisitorToCinema() {
        User michal = new User("Michal");
        userRepository.save(michal);

        Cinema cinema = new Cinema("Odeon");
        cinema.addVisitor(michal);

        cinemaRepository.save(cinema);

        assertSameGraph(getDatabase(), "CREATE " +
                "(m:User {name:'Michal'})," +
                "(c:Cinema {name:'Odeon'})," +
                "(m)-[:VISITED]->(c)");
    }

    @Test
    public void shouldBefriendPeople() {
        User michal = new User("Michal");
        michal.befriend(new User("Adam"));
        userRepository.save(michal);

        try {
            assertSameGraph(getDatabase(), "CREATE (m:User {name:'Michal'})-[:FRIEND_OF]->(a:User {name:'Adam'})");
        } catch (AssertionError error) {
            assertSameGraph(getDatabase(), "CREATE (m:User {name:'Michal'})<-[:FRIEND_OF]-(a:User {name:'Adam'})");
        }
    }

    @Test
    public void shouldLoadFriends() {
        new ExecutionEngine(getDatabase()).execute("CREATE (m:User {name:'Michal'})-[:FRIEND_OF]->(a:User {name:'Adam'})");

        User michal = userRepository.findByProperty("name", "Michal").iterator().next();
        assertEquals(1, michal.getFriends().size());

        User adam = michal.getFriends().iterator().next();
        assertEquals("Adam", adam.getName());
        assertEquals(1, adam.getFriends().size());

        assertTrue(michal == adam.getFriends().iterator().next());
        assertTrue(michal.equals(adam.getFriends().iterator().next()));
    }

    @Test
    public void shouldLoadFriends2() {
        new ExecutionEngine(getDatabase()).execute("CREATE (m:User {name:'Michal'})<-[:FRIEND_OF]-(a:User {name:'Adam'})");

        User michal = userRepository.findByProperty("name", "Michal").iterator().next();
        assertEquals(1, michal.getFriends().size());

        User adam = michal.getFriends().iterator().next();
        assertEquals("Adam", adam.getName());
        assertEquals(1, adam.getFriends().size());

        assertTrue(michal == adam.getFriends().iterator().next());
        assertTrue(michal.equals(adam.getFriends().iterator().next()));
    }

    @Test
    @Ignore //todo fixme when query methods working in spring aop
    public void shouldFindUsersByName() {
        new ExecutionEngine(getDatabase()).execute("CREATE (m:User {name:'Michal'})<-[:FRIEND_OF]-(a:User {name:'Adam'})");

        Collection<User> users = userRepository.findByName("Michal");
        Iterator<User> iterator = users.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("Michal", iterator.next().getName());
        assertFalse(iterator.hasNext());
    }

    @Test
    //@Ignore //todo
    public void shouldSaveNewUserAndNewMovieWithRatings() {
        User user = new User("Michal");
        TempMovie movie = new TempMovie("Pulp Fiction");
        user.rate(movie, 5, "Best movie ever");
        userRepository.save(user);

        User michal = userRepository.findByProperty("name", "Michal").iterator().next();

        assertSameGraph(getDatabase(), "CREATE (u:User {name:'Michal'})-[:Rating {stars:5, comment:'Best movie ever'}]->(m:Movie {title:'Pulp Fiction'})");
    }

    @Test
    public void testDatabaseCheck() {
//        assertSameGraph(getDatabase(), "CREATE (u:User {name:'Michal'})-[:Rating {stars:5, comment:'Best movie ever'}]->(m:Movie {title:'Pulp Fiction'})");
    }
}
