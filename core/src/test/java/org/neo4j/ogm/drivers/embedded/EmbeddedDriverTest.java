package org.neo4j.ogm.drivers.embedded;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.drivers.AbstractDriverTest;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.test.TestGraphDatabaseFactory;

/**
 * @author vince
 */
public class EmbeddedDriverTest extends AbstractDriverTest {

    private static final GraphDatabaseService graphDatabaseService = new TestGraphDatabaseFactory().newImpermanentDatabase();
    private static final Driver driver = new EmbeddedDriver(graphDatabaseService);

    @Override
    public Driver getDriver() {
        return driver;
    }


//    private SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.social");
//    private Session session = sessionFactory.openSession(driver);
//
//    @Before
//    public void init() {
//        session.purgeDatabase();
//    }
//
//    @Test
//    public void shouldSaveAndReloadEntity() {
//        session.save(new User());
//        session.clear();
//        assertEquals(1, session.loadAll(User.class).size());
//    }
//
//    @Test
//    public void shouldSupportMultipleConcurrentThreads() throws InterruptedException {
//
//        ExecutorService executor = Executors.newFixedThreadPool(10);
//        final CountDownLatch latch = new CountDownLatch(100);
//
//        for (int i = 0; i < 100; i++) {
//            executor.submit(new Runnable() {
//                @Override
//                public void run() {
//                    session.save(new User());
//                    latch.countDown();
//                }
//            });
//        }
//
//        latch.await(); // pause until 100 users
//
//        System.out.println("all threads joined");
//        executor.shutdown();
//
//        assertEquals(100, session.countEntitiesOfType(User.class));
//
//    }
//
//    @Test
//    public void shouldFindExplicitlyCommittedEntity() {
//        Transaction tx = session.beginTransaction();
//        session.save(new User());
//        tx.commit();
//        session.clear();
//        assertEquals(1, session.loadAll(User.class).size());
//    }
//
//
//    @Test
//    public void shouldNotFindExplicitlyRolledBackEntity() {
//
//        Transaction tx = session.beginTransaction();
//        session.save(new User());
//        tx.rollback();
//        session.clear();
//        assertEquals(0, session.loadAll(User.class).size());
//    }
//
//    @Test
//    public void shouldHandleNestedTransactions() {
//        m1();
//        assertEquals(1, session.loadAll(User.class).size());
//    }
//
//    private void m1() {
//        try (Transaction tx = session.beginTransaction()) {
//            m2(); // committed
//            m3(); // rolled back
//            tx.commit();
//        }
//    }
//
//    private void m2() {
//        try (Transaction tx = session.beginTransaction()) {
//            session.save(new User());
//            tx.commit();
//        }
//    }
//
//    private void m3() {
//        try (Transaction tx = session.beginTransaction()) {
//            session.save(new User());
//            tx.rollback();
//        }
//    }

}
