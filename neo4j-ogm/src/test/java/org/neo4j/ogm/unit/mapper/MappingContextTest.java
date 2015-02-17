/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.unit.mapper;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.policy.Person;
import org.neo4j.ogm.domain.policy.Policy;
import org.neo4j.ogm.mapper.MappedRelationship;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.metadata.MetaData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MappingContextTest {

    private MappingContext collector;
    private static final int NUM_OBJECTS=100000;
    private static final int NUM_THREADS=15;

    @Before
    public void setUp() {
        collector = new MappingContext(new MetaData("org.neo4j.ogm.domain.policy"));
    }

    @Test
    public void testPath() {

        Person jim = new Person("jim");
        jim.setId(1L);

        Policy policy = new Policy("healthcare");
        policy.setId(2L);

        collector.registerNodeEntity(jim, jim.getId());
        collector.registerNodeEntity(policy, policy.getId());
        collector.registerRelationship(new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId()));

        assertEquals(jim, collector.get(jim.getId()));
        assertEquals(policy, collector.get(policy.getId()));
        assertTrue(collector.isRegisteredRelationship(new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId())));

    }

    @Test
    public void clearOne() {

        Person jim = new Person("jim");
        jim.setId(1L);

        Policy policy = new Policy("healthcare");
        policy.setId(2L);

        collector.registerNodeEntity(jim, jim.getId());
        collector.registerNodeEntity(policy, policy.getId());
        collector.registerRelationship(new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId()));
        collector.clear(jim);

        assertEquals(null, collector.get(jim.getId()));
        assertEquals(policy, collector.get(policy.getId()));
        assertFalse(collector.isRegisteredRelationship(new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId())));

    }

    @Test
    public void clearType() {
        Person jim = new Person("jim");
        jim.setId(1L);

        Policy healthcare = new Policy("healthcare");
        healthcare.setId(2L);

        Policy immigration = new Policy("immigration");
        immigration.setId(3L);

        Person rik = new Person("rik");
        rik.setId(4L);

        collector.registerNodeEntity(jim, jim.getId());
        collector.registerNodeEntity(rik, rik.getId());
        collector.registerNodeEntity(healthcare, healthcare.getId());
        collector.registerNodeEntity(immigration, immigration.getId());

        collector.registerRelationship(new MappedRelationship(jim.getId(), "INFLUENCES", healthcare.getId()));
        collector.registerRelationship(new MappedRelationship(jim.getId(), "INFLUENCES", immigration.getId()));
        collector.registerRelationship(new MappedRelationship(jim.getId(), "WORKS_WITH", rik.getId()));

        collector.clear(Policy.class);

        assertEquals(0, collector.getAll(Policy.class).size());
        assertEquals(null, collector.get(healthcare.getId()));
        assertEquals(null, collector.get(immigration.getId()));

        assertEquals(jim, collector.get(jim.getId()));
        assertEquals(rik, collector.get(rik.getId()));
        assertEquals(1, collector.mappedRelationships().size());

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

        for (Object object : objects) {
            TestObject testObject = (TestObject) object;
            sum -= testObject.id;                           // remove this id from sum of all ids
            assertEquals(1, testObject.notes.size());       // only one thread created this object
            int id = Integer.parseInt(testObject.notes.get(0));
        }

        assertEquals(0, sum);                               // all objects were created

    }

    public class TestObject {
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
                    testObject = (TestObject) collector.registerNodeEntity(new TestObject(), id);
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
