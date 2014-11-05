package org.neo4j.ogm.mapper;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class MappingContextTest {

    private MappingContext context;
    private static final int NUM_OBJECTS=1000;
    private static final int NUM_THREADS=15;

    @Before
    public void setUp() {
        context = new MappingContext();
    }

    @Test
    public void testMultiThreadedAccess() {

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < NUM_THREADS; i++) {
            Thread thread = new Thread(new Inserter());
            threads.add(thread);
            thread.start();
        }

        // wait for all threads to finish
        for (int i = 0; i < NUM_THREADS; i++) {
            while (threads.get(i).isAlive()) {
                continue;
            }
        }

        List<Object> objects = context.getObjects(TestObject.class);
        assertEquals(NUM_OBJECTS, objects.size());

        int sum = (NUM_OBJECTS * (NUM_OBJECTS + 1)) / 2;

        for (int i = 0; i < NUM_OBJECTS; i++) {
            TestObject testObject = (TestObject) objects.get(i);
            sum -= testObject.id; // remove this id from sum of all ids
            assertTrue(testObject.notes.size() == 1); // only one thread created this object
        }

        assertEquals(0, sum); // all objects were created
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
                context.registerTypeMember(testObject);
            }
        }
    }
}
