package org.neo4j.ogm.unit.mapper;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.mapper.MappingContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MappingContextTest {

    private MappingContext context;
    private static final int NUM_OBJECTS=100000;
    private static final int NUM_THREADS=15;

    @Before
    public void setUp() {
        context = new MappingContext();
    }

    @Test
    public void testMultiThreadedAccess() throws InterruptedException {

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < NUM_THREADS; i++) {
            Thread thread = new Thread(new Inserter());
            threads.add(thread);
            thread.start();
        }

        // occasionally only NUM-OBJECTS-1 get created. I have no
        // idea why. Sleeping the main thread seems to solve the
        // problem, but I can't really explain why.

        Thread.sleep(1000);

        for (int i = 0; i < NUM_THREADS; i++) {
            threads.get(i).join();
        }

        Set<Object> objects = context.getObjects(TestObject.class);
        assertEquals(NUM_OBJECTS, objects.size());

        int sum = (NUM_OBJECTS * (NUM_OBJECTS + 1)) / 2;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        Iterator iterator = objects.iterator();
        for (int i = 0; i < NUM_OBJECTS; i++) {
            TestObject testObject = (TestObject) iterator.next();

            sum -= testObject.id; // remove this id from sum of all ids
            assertTrue(testObject.notes.size() == 1); // only one thread created this object
            int id = Integer.parseInt(testObject.notes.get(0));
            if (id < min ) min = id;
            if (id > max ) max = id;
        }

        assertEquals(0, sum); // all objects were created

        assertEquals(NUM_THREADS, max-min+1);  // all threads took part
    }



    class TestObject {
        Long id = null;
        List<String> notes = new ArrayList<>();
    }

    class Inserter implements Runnable {

        @Override
        public void run() {
            for (int i = 1; i <= NUM_OBJECTS; i++) {
                Long id = new Long(i);

                // this is the code pattern the ogm must use when loading objects in a multi-threaded context
                TestObject testObject = (TestObject) context.get(id);

                if (testObject == null) {
                    testObject = (TestObject) context.register(new TestObject(), id);
                    synchronized (testObject) {
                        if (testObject.id == null) {
                            // if these mutators don't change the object on a per-thread basis
                            // they could be moved outside the synchronized block. in this test
                            // that isn't the case.

                            testObject.notes.add(String.valueOf(Thread.currentThread().getId()));
                            testObject.id = id;
                        }
                    }
                }
            }
        }
    }
}
