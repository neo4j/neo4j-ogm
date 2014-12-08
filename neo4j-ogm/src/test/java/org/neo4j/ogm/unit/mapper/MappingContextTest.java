package org.neo4j.ogm.unit.mapper;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.mapper.MappingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class MappingContextTest {

    private MappingContext collector;
    private static final int NUM_OBJECTS=100000;
    private static final int NUM_THREADS=15;

    @Before
    public void setUp() {
        collector = new MappingContext();
    }

    @Test
    public void ensureThreadSafe() throws InterruptedException {

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < NUM_THREADS; i++) {
            Thread thread = new Thread(new Inserter());
            threads.add(thread);
            thread.start();
        }

        for (int i = 0; i < NUM_THREADS; i++) {
            threads.get(i).join();
        }

        Set<Object> objects = collector.getAll(TestObject.class);

        assertEquals(NUM_OBJECTS, objects.size());

        int sum = (NUM_OBJECTS * (NUM_OBJECTS + 1)) / 2;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (Object object : objects) {
            TestObject testObject = (TestObject) object;
            sum -= testObject.id;                           // remove this id from sum of all ids
            assertEquals(1, testObject.notes.size());       // only one thread created this object
            int id = Integer.parseInt(testObject.notes.get(0));
            if (id < min ) min = id;                        // update min thread-id found
            if (id > max ) max = id;                        // update max thread-id found
        }

        assertEquals(0, sum);                               // all objects were created
        assertEquals(NUM_THREADS, max-min+1);               // all threads took part
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

                TestObject testObject = (TestObject) collector.get(id);
                if (testObject == null) {
                    testObject = (TestObject) collector.register(new TestObject(), id);
                    synchronized (testObject) {
                        if (testObject.id == null) {
                            testObject.notes.add(String.valueOf(Thread.currentThread().getId()));
                            testObject.id = id;
                        }
                    }
                }

            }
        }
    }

}
