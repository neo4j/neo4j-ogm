/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

package org.neo4j.ogm.context;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.domain.policy.Person;
import org.neo4j.ogm.domain.policy.Policy;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class MappingContextTest {

    private static final int NUM_OBJECTS = 100000;
    private static final int NUM_THREADS = 15;
    private MappingContext collector;

    @Before
    public void setUp() {
        collector = new MappingContext(new MetaData("org.neo4j.ogm.domain.policy", "org.neo4j.ogm.context"));
    }

    @Test
    public void testPath() {

        Person jim = new Person("jim");
        jim.setId(1L);

        Policy policy = new Policy("healthcare");
        policy.setId(2L);

        collector.addNodeEntity(jim, jim.getId());
        collector.addNodeEntity(policy, policy.getId());
        collector.addRelationship(new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), Person.class, Policy.class));

        assertEquals(jim, collector.getNodeEntity(jim.getId()));
        assertEquals(policy, collector.getNodeEntity(policy.getId()));
        assertTrue(collector.containsRelationship(new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), Person.class, Policy.class)));
    }

    @Test
    public void clearOne() {

        Person jim = new Person("jim");
        jim.setId(1L);

        Policy policy = new Policy("healthcare");
        policy.setId(2L);

        collector.addNodeEntity(jim, jim.getId());
        collector.addNodeEntity(policy, policy.getId());
        collector.addRelationship(new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), Person.class, Policy.class));
        collector.removeEntity(jim);

        assertEquals(null, collector.getNodeEntity(jim.getId()));
        assertEquals(policy, collector.getNodeEntity(policy.getId()));
        assertFalse(collector.containsRelationship(new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), Person.class, Policy.class)));
    }

    /**
     * @see Issue #96
     */
    @Test
    public void clearOneEqualToAnother() {

        Person jim = new Person("jim");
        jim.setId(1L);

        Person another = new Person("jim"); //jim.equals(another)=true
        another.setId(3L);

        Policy policy = new Policy("healthcare");
        policy.setId(2L);

        collector.addNodeEntity(jim, jim.getId());
        collector.addNodeEntity(another, another.getId());
        collector.addNodeEntity(policy, policy.getId());
        collector.addRelationship(new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), Person.class, Policy.class));
        collector.removeEntity(jim);

        assertEquals(null, collector.getNodeEntity(jim.getId()));
        assertEquals(policy, collector.getNodeEntity(policy.getId()));
        assertEquals(another, collector.getNodeEntity(another.getId()));
        assertFalse(collector.containsRelationship(new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), Person.class, Policy.class)));
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

        collector.addNodeEntity(jim, jim.getId());
        collector.addNodeEntity(rik, rik.getId());
        collector.addNodeEntity(healthcare, healthcare.getId());
        collector.addNodeEntity(immigration, immigration.getId());

        collector.addRelationship(new MappedRelationship(jim.getId(), "INFLUENCES", healthcare.getId(), Person.class, Policy.class));
        collector.addRelationship(new MappedRelationship(jim.getId(), "INFLUENCES", immigration.getId(), Person.class, Policy.class));
        collector.addRelationship(new MappedRelationship(jim.getId(), "WORKS_WITH", rik.getId(), Person.class, Person.class));

        collector.removeType(Policy.class);

        assertEquals(0, collector.getEntities(Policy.class).size());
        assertEquals(null, collector.getNodeEntity(healthcare.getId()));
        assertEquals(null, collector.getNodeEntity(immigration.getId()));

        assertEquals(jim, collector.getNodeEntity(jim.getId()));
        assertEquals(rik, collector.getNodeEntity(rik.getId()));
        assertEquals(1, collector.getRelationships().size());
    }

    @Test
    public void areObjectsReportedAsDirtyCorrectly() {
        Person jim = new Person("jim");
        jim.setId(1L);

        Policy healthcare = new Policy("healthcare");
        healthcare.setId(2L);

        Policy immigration = new Policy("immigration");
        immigration.setId(3L);

        Person rik = new Person("rik");
        rik.setId(4L);

        collector.addNodeEntity(jim, jim.getId());
        collector.addNodeEntity(rik, rik.getId());
        collector.addNodeEntity(healthcare, healthcare.getId());
        collector.addNodeEntity(immigration, immigration.getId());

        rik.setName("newRik");

        assertFalse(collector.isDirty(jim));
        assertTrue(collector.isDirty(rik));
        assertFalse(collector.isDirty(healthcare));
        assertFalse(collector.isDirty(immigration));
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

                TestObject testObject = (TestObject) collector.getNodeEntity(id);
                if (testObject == null) {
                    synchronized (this) {
                        testObject = new TestObject();
                        testObject.notes.add(String.valueOf(Thread.currentThread().getId()));
                        testObject.id = id;
                        collector.addNodeEntity(testObject, id);
                    }
                }
            }
        }
    }
}
