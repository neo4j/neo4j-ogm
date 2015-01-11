package org.springframework.data.neo4j.integration.movies;

import com.graphaware.test.integration.WrappingServerIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.springframework.data.neo4j.integration.movies.context.PersistenceContext;
import org.springframework.data.neo4j.integration.movies.domain.User;
import org.springframework.data.neo4j.integration.movies.repo.UserRepository;
import org.springframework.data.neo4j.integration.movies.service.UserService;
import org.neo4j.tooling.GlobalGraphOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.graphaware.test.unit.GraphUnit.assertSameGraph;
import static org.junit.Assert.*;

@ContextConfiguration(classes = {PersistenceContext.class})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TransactionIntegrationTest extends WrappingServerIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Override
    protected int neoServerPort() {
        return 7879;
    }

    @Override
    protected void populateDatabase(GraphDatabaseService database) {
        super.populateDatabase(database);
        database.registerTransactionEventHandler(new TransactionEventHandler.Adapter<Object>() {
            @Override
            public Object beforeCommit(TransactionData data) throws Exception {
                throw new RuntimeException("Deliberate testing exception");
            }
        }) ;
    }

    @Test
    @Ignore //todo
    public void whenImplicitTransactionFailsNothingShouldBeCreated() {
        try {
            userRepository.save(new User("Michal"));
        } catch (Exception e) {
            //expected
        }

        assertEquals(0, userRepository.count());

        try (Transaction tx = getDatabase().beginTx()) {
            assertFalse(GlobalGraphOperations.at(getDatabase()).getAllNodes().iterator().hasNext());
            tx.success();
        }
    }

    @Test
    @Ignore //todo
    public void whenExplicitTransactionFailsNothingShouldBeCreated() {
        try {
            userService.saveWithTxAnnotationOnInterface(new User("Michal"));
        } catch (Exception e) {
            //expected
        }

        assertEquals(0, userRepository.count());

        try (Transaction tx = getDatabase().beginTx()) {
            assertFalse(GlobalGraphOperations.at(getDatabase()).getAllNodes().iterator().hasNext());
            tx.success();
        }
    }

    @Test
    @Ignore //todo
    public void whenExplicitTransactionFailsNothingShouldBeCreated2() {
        try {
            userService.saveWithTxAnnotationOnImpl(new User("Michal"));
        } catch (Exception e) {
            //expected
        }

        assertEquals(0, userRepository.count());

        try (Transaction tx = getDatabase().beginTx()) {
            assertFalse(GlobalGraphOperations.at(getDatabase()).getAllNodes().iterator().hasNext());
            tx.success();
        }
    }


}
