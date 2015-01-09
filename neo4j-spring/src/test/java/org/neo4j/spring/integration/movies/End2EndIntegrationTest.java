package org.neo4j.spring.integration.movies;

import com.graphaware.test.integration.WrappingServerIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.spring.integration.movies.context.PersistenceContext;
import org.neo4j.spring.integration.movies.domain.Cinema;
import org.neo4j.spring.integration.movies.domain.Genre;
import org.neo4j.spring.integration.movies.domain.User;
import org.neo4j.spring.integration.movies.repo.CinemaRepository;
import org.neo4j.spring.integration.movies.repo.UserRepository;
import org.neo4j.spring.integration.movies.service.UserService;
import org.neo4j.tooling.GlobalGraphOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;
import java.util.Iterator;
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
    @Ignore //todo fixme
    public void shouldFindUsersByName() {
        new ExecutionEngine(getDatabase()).execute("CREATE (m:User {name:'Michal'})<-[:FRIEND_OF]-(a:User {name:'Adam'})");

        Collection<User> users = userRepository.findUsersByName("Michal");
        Iterator<User> iterator = users.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("Michal", iterator.next().getName());
        assertFalse(iterator.hasNext());
    }
}
