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

package org.neo4j.ogm.persistence.session.events;

import org.junit.After;
import org.junit.Before;
import org.neo4j.ogm.domain.cineasts.annotated.Actor;
import org.neo4j.ogm.domain.cineasts.annotated.Knows;
import org.neo4j.ogm.domain.filesystem.Document;
import org.neo4j.ogm.domain.filesystem.Folder;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.event.Event;
import org.neo4j.ogm.session.event.EventListener;
import org.neo4j.ogm.session.event.PersistenceEvent;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Mihai Raulea
 * @author Vince Bickers
 */
public class EventTest extends MultiDriverTestClass {

    Session session;
    Document a;
    Document b;
    Document c;
    Document d;
    Document e;
    Folder folder;

    Actor jim, bruce, lee, stan;
    Knows knowsJB;
    Knows knowsLS;
    Knows knowsJL;

    EventListenerTest eventListenerTest;

    @Before
    public void init() throws IOException {
        // each test should instantiate a new one
        eventListenerTest = null;
        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.filesystem", "org.neo4j.ogm.domain.cineasts.annotated");
        session = sessionFactory.openSession();
        //session.purgeDatabase();
        a = new Document();
        a.setName("a");

        b = new Document();
        b.setName("b");

        c = new Document();
        c.setName("c");

        d = new Document();
        d.setName("d");

        e = new Document();
        e.setName("e");

        folder = new Folder();
        folder.setName("folder");

        folder.getDocuments().add(a);
        folder.getDocuments().add(b);
        folder.getDocuments().add(c);

        a.setFolder(folder);
        b.setFolder(folder);
        c.setFolder(folder);

        session.save(folder);
        session.save(d);
        session.save(e);

        jim = new Actor("Jim");
        bruce = new Actor("Bruce");
        lee = new Actor("Lee");
        stan = new Actor("Stan");

        knowsJB = new Knows();
        knowsJB.setFirstActor(jim);
        knowsJB.setSecondActor(bruce);
        knowsJB.setSince(new Date());

        jim.getKnows().add(knowsJB);
        //session.save(knowsJB);

        knowsJL = new Knows();
        knowsJL.setFirstActor(jim);
        knowsJL.setSecondActor(lee);
        knowsJL.setSince(new Date());

        jim.getKnows().add(knowsJL);
        //session.save(knowsJL);

        knowsLS = new Knows();
        knowsLS.setFirstActor(lee);
        knowsLS.setSecondActor(stan);
        knowsLS.setSince(new Date());

        lee.getKnows().add(knowsLS);
        session.save(jim);

    }

//    @Test
//    public void shouldFireEventsForNewAndExistingDocuments() {
//
//        eventListenerTest = new EventListenerTest();
//        session.register(eventListenerTest);
//
//        // these nodes are new
//        Document e = new Document();
//        e.setName("newE");
//        session.save(e);
//
//        Document f = new Document();
//        f.setName("newF");
//        session.save(f);
//
//        Document g = new Document();
//        g.setName("newG");
//        session.save(g);
//
//        // this node already exists
//        d.setName("newD");
//        session.save(d);
//
//        assertEquals(8, eventListenerTest.count());
//
//        assertTrue(eventListenerTest.captured(d, Event.LIFECYCLE.PRE_SAVE));
//        assertTrue(eventListenerTest.captured(d, Event.LIFECYCLE.POST_SAVE));
//        assertTrue(eventListenerTest.captured(e, Event.LIFECYCLE.PRE_SAVE));
//        assertTrue(eventListenerTest.captured(e, Event.LIFECYCLE.POST_SAVE));
//        assertTrue(eventListenerTest.captured(f, Event.LIFECYCLE.PRE_SAVE));
//        assertTrue(eventListenerTest.captured(f, Event.LIFECYCLE.POST_SAVE));
//        assertTrue(eventListenerTest.captured(g, Event.LIFECYCLE.PRE_SAVE));
//        assertTrue(eventListenerTest.captured(g, Event.LIFECYCLE.POST_SAVE));
//    }


//    @Test
//    public void testAddNewRelationships() {
//        int noOfExpectedEvents = 4;
//        eventListenerTest = new EventListenerTest();
//        session.register(eventListenerTest);
//
//        d.setFolder(folder);
//        session.save(d);
//
//        e.setFolder(folder);
//        session.save(e);
//
//        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[1];
//        TargetObjectCount targetObjectCount = new TargetObjectCount();
//        targetObjectCount.count = 4;
//        targetObjectCount.targetObjectType = TransientRelationship.class;
//        targetObjectCounts[0] = targetObjectCount;
//
//        testExpectedNumberOfEventsInQueue(eventListenerTest, noOfExpectedEvents);
//        testCountOfExpectedTargetObjects(eventListenerTest, targetObjectCounts);
//    }

//    @Test
//    public void testAlterOneRelationshipEntity() {
//        // when altering a relationship, the relationship is first deleted, and then added.
//        // every relationship altered triggers 2 events(TransientRelationship and RelationshipEntity)
//        int noOfExpectedEvents = 4;
//        eventListenerTest = new EventListenerTest();
//        session.register(eventListenerTest);
//
//        // i need a random date here, otherwise it will not be counted as dirty
//        Random r = new Random();
//        knowsJB.setSince(new Date((long) (1293861599 + r.nextDouble() * 60 * 60 * 24 * 365)));
//        session.save(knowsJB);
//
//        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[2];
//        TargetObjectCount targetObjectCount = new TargetObjectCount();
//        targetObjectCount.count = 2;
//        targetObjectCount.targetObjectType = TransientRelationship.class;
//        targetObjectCounts[0] = targetObjectCount;
//
//        TargetObjectCount targetObjectCount2 = new TargetObjectCount();
//        targetObjectCount2.count = 2;
//        targetObjectCount2.targetObjectType = Knows.class;
//        targetObjectCounts[1] = targetObjectCount2;
//
//        testExpectedNumberOfEventsInQueue(eventListenerTest, noOfExpectedEvents);
//        testCountOfExpectedTargetObjects(eventListenerTest, targetObjectCounts);
//    }

//    @Test
//    public void testAlterMultipleRelationshipEntitiesWhoseObjectsAreNotConnected() {
//        int noOfExpectedEvents = 14;
//        eventListenerTest = new EventListenerTest();
//        session.register(eventListenerTest);
//        Random r = new Random();
//        knowsJB.setSince(new Date((long) (1293861599 + r.nextDouble() * 60 * 60 * 24 * 365)));
//        session.save(knowsJB);
//
//        knowsLS.setSince(new Date((long) (1293861599 + r.nextDouble() * 60 * 60 * 24 * 365)));
//        session.save(knowsLS);
//
//        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[2];
//        TargetObjectCount targetObjectCount = new TargetObjectCount();
//        targetObjectCount.count = 8;
//        targetObjectCount.targetObjectType = TransientRelationship.class;
//        targetObjectCounts[0] = targetObjectCount;
//
//        TargetObjectCount targetObjectCount2 = new TargetObjectCount();
//        targetObjectCount2.count = 6;
//        targetObjectCount2.targetObjectType = Knows.class;
//        targetObjectCounts[1] = targetObjectCount2;
//
//        testExpectedNumberOfEventsInQueue(eventListenerTest, noOfExpectedEvents);
//        testCountOfExpectedTargetObjects(eventListenerTest, targetObjectCounts);
//    }



    // session.save(<? implements Collection>);
    class EventListenerTest implements EventListener {

        public List<Event> eventsCaptured;

        public EventListenerTest() {
            eventsCaptured = new ArrayList<>();
        }

        @Override
        public void update(Event event) {
            eventsCaptured.add(event);
            System.out.println(event.toString());
        }

        public boolean captured(Object o, Event.LIFECYCLE lifecycle) {
            Event event = new PersistenceEvent(o, lifecycle);
            for (Event captured : eventsCaptured)
            {
                if (captured.toString().equals(event.toString())) {
                    return true;
                }
            }
            return false;
        }

        public int count() {
            return eventsCaptured.size();
        }
    }

//    @Test
//    public void eventIntegrationTest() {
//        this.eventListenerTest = new EventListenerTest();
//        session.register(eventListenerTest);
//
//        a.setName("newA");
//        b.setName("newB");
//        c.setName("newC");
//        folder.setName("newFolder");
//
//        session.save(folder);
//        Document aa = session.load(Document.class, a.getId());
//        Document bb = session.load(Document.class, b.getId());
//        System.out.println(aa.getName());
//        System.out.println(bb.getName());
//        System.out.println(folder.getName());
//
//        testExpectedNumberOfEventsInQueue(eventListenerTest, 8);
//    }

//    private void testExpectedNumberOfEventsInQueue(EventListenerTest eventListener, int noOfExpectedEvents) {
//        assertTrue(eventListener.count() == noOfExpectedEvents);
//    }
//
//    private void testCountOfExpectedTargetObjects(EventListenerTest eventListener, TargetObjectCount[] targetObjectCountArray) {
//        int noOfConsideredObjects = 0;
//        int noOfCaughtObjects = 0;
//        for (TargetObjectCount targetObjectCount : targetObjectCountArray) {
//            int[] result = getConsideredObjectsAndCaughtObjects(eventListener, targetObjectCount);
//            noOfCaughtObjects += result[0];
//            noOfConsideredObjects = result[1];
//        }
//        assertTrue(noOfCaughtObjects == noOfConsideredObjects);
//    }


//    private int[] getConsideredObjectsAndCaughtObjects(EventListenerTest eventListener, TargetObjectCount targetObjectCount) {
//        int noOfCaughtObjectsOfType = 0;
//        int noOfConsideredObjects = 0;
//        int[] result = new int[2];
//
//        for (Event event : eventListener.eventsCaptured) {
//            Object caughtObject = event.getTargetObject();
//            noOfConsideredObjects++;
//            if (targetObjectCount.targetObjectType.isInstance(caughtObject)) {
//                noOfCaughtObjectsOfType++;
//            }
//
//        }
//
//        for (int i = 0; i < eventListener.eventsCaptured.size(); i++) {
//            Object caughtObject = eventListener.eventsCaptured[i].getTargetObject();
//            noOfConsideredObjects++;
//            if (targetObjectCount.targetObjectType.isInstance(caughtObject)) {
//                noOfCaughtObjectsOfType++;
//            }
//        }
//        result[0] = noOfCaughtObjectsOfType;
//        result[1] = noOfConsideredObjects;
//        return result;
//    }

//    class TargetObjectCount {
//        public Class targetObjectType;
//        public int count;
//    }
//
    @After
    public void clean() throws IOException {
        session.purgeDatabase();
    }

}
