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

import org.junit.*;
import org.neo4j.ogm.context.TransientRelationship;
import org.neo4j.ogm.domain.cineasts.annotated.Actor;
import org.neo4j.ogm.domain.cineasts.annotated.Knows;
import org.neo4j.ogm.domain.filesystem.Document;
import org.neo4j.ogm.domain.filesystem.Folder;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.event.Event;
import org.neo4j.ogm.session.event.EventListener;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * @author Mihai Raulea
 */
public class EventTest extends MultiDriverTestClass {

    private Session session;
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
        session.save(jim);

        knowsLS = new Knows();
        knowsLS.setFirstActor(lee);
        knowsLS.setSecondActor(stan);
        knowsLS.setSince(new Date());

        lee.getKnows().add(knowsLS);
        session.save(lee);

        knowsJL = new Knows();
        knowsJL.setFirstActor(jim);
        knowsJL.setSecondActor(lee);

        // made a logical error on this, somehow -- it made knowsJB non-existant
        lee.getKnows().add(knowsJL);
        session.save(lee);
    }

    @Test
    public void noEventsShouldFire() {
        this.eventListenerTest = new EventListenerTest(0);
        session.register(eventListenerTest);

        session.save(folder);
        Document aa = session.load(Document.class, a.getId());
        Document bb = session.load(Document.class, b.getId());

        testExpectedNumberOfEventsInQueue(eventListenerTest, 0);
    }

    @Test
    public void testAddOneNode() {
        eventListenerTest = new EventListenerTest(2);
        session.register(eventListenerTest);

        Document e = new Document();
        e.setName("e");

        session.save(e);

        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[1];
        TargetObjectCount targetObjectCount = new TargetObjectCount();
        targetObjectCount.count = 2;
        targetObjectCount.targetObjectType = Document.class;
        targetObjectCounts[0] = targetObjectCount;

        testExpectedNumberOfEventsInQueue(eventListenerTest,2);
        testCountOfExpectedTargetObjects(eventListenerTest, targetObjectCounts);
    }

    @Test
    public void testAddMultipleNodes() {
        eventListenerTest = new EventListenerTest(6);
        session.register(eventListenerTest);

        Document e = new Document();
        e.setName("e");
        session.save(e);

        Document f = new Document();
        f.setName("f");
        session.save(f);

        Document g = new Document();
        g.setName("g");
        session.save(g);

        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[1];
        TargetObjectCount targetObjectCount = new TargetObjectCount();
        targetObjectCount.count = 6;
        targetObjectCount.targetObjectType = Document.class;
        targetObjectCounts[0] = targetObjectCount;

        testExpectedNumberOfEventsInQueue(eventListenerTest,6);
        testCountOfExpectedTargetObjects(eventListenerTest, targetObjectCounts);
    }

    @Test
    public void testAlterMultipleNodes() {
        eventListenerTest = new EventListenerTest(6);
        session.register(eventListenerTest);

        Document e = new Document();
        e.setName("newE");
        session.save(e);

        Document f = new Document();
        f.setName("newF");
        session.save(f);

        Document g = new Document();
        g.setName("newG");
        session.save(g);

        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[1];
        TargetObjectCount targetObjectCount = new TargetObjectCount();
        targetObjectCount.count = 6;
        targetObjectCount.targetObjectType = Document.class;
        targetObjectCounts[0] = targetObjectCount;

        testExpectedNumberOfEventsInQueue(eventListenerTest,6);
        testCountOfExpectedTargetObjects(eventListenerTest, targetObjectCounts);
    }

    @Test
    public void testAddAndAlterMultipleUnconnectedNodes() {
        eventListenerTest = new EventListenerTest(8);
        session.register(eventListenerTest);

        // these nodes are new
        Document e = new Document();
        e.setName("newE");
        session.save(e);

        Document f = new Document();
        f.setName("newF");
        session.save(f);

        Document g = new Document();
        g.setName("newG");
        session.save(g);

        // this node is not connected to any other entities
        d.setName("newD");
        session.save(d);

        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[1];
        TargetObjectCount targetObjectCount = new TargetObjectCount();
        targetObjectCount.count = 6;
        targetObjectCount.targetObjectType = Document.class;
        targetObjectCounts[0] = targetObjectCount;

        testExpectedNumberOfEventsInQueue(eventListenerTest,8);
        testCountOfExpectedTargetObjects(eventListenerTest, targetObjectCounts);
    }

    @Test
    public void testAddAndAlterConnectedNode() {
        eventListenerTest = new EventListenerTest(8);
        session.register(eventListenerTest);

        Document e = new Document();
        e.setName("newE");
        session.save(e);

        Document f = new Document();
        f.setName("newF");
        session.save(f);

        Document g = new Document();
        g.setName("newG");
        session.save(g);

        // even though the node is connected, the connected nodes are not dirty, so no additional events should fire
        a.setName("newA");
        session.save(a);

        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[1];
        TargetObjectCount targetObjectCount = new TargetObjectCount();
        targetObjectCount.count = 8;
        targetObjectCount.targetObjectType = Document.class;
        targetObjectCounts[0] = targetObjectCount;

        testExpectedNumberOfEventsInQueue(eventListenerTest,8);
        testCountOfExpectedTargetObjects(eventListenerTest, targetObjectCounts);
    }

    @Test
    public void testAddAndAlterMultipleConnectedNodes() {
        int expectedNumberOfEvents = 6;
        eventListenerTest = new EventListenerTest(expectedNumberOfEvents);
        session.register(eventListenerTest);

        // the node is connected, the connected nodes are dirty, so additional events should fire
        a.setName("newA");
        b.setName("newB");
        c.setName("newC");
        session.save(a);

        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[1];
        TargetObjectCount targetObjectCount = new TargetObjectCount();
        targetObjectCount.count = 6;
        targetObjectCount.targetObjectType = Document.class;
        targetObjectCounts[0] = targetObjectCount;

        testExpectedNumberOfEventsInQueue(eventListenerTest,expectedNumberOfEvents);
        testCountOfExpectedTargetObjects(eventListenerTest, targetObjectCounts);
    }


    @Test
    public void testAddAndAlterSomeMultipleConnectedNodes() {
        int expectedNumberOfEvents = 4;
        eventListenerTest = new EventListenerTest(expectedNumberOfEvents);
        session.register(eventListenerTest);

        // the node is connected, the connected nodes are dirty, so additional events should fire
        a.setName("newA");
        c.setName("newC");
        session.save(a);

        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[1];
        TargetObjectCount targetObjectCount = new TargetObjectCount();
        targetObjectCount.count = 4;
        targetObjectCount.targetObjectType = Document.class;
        targetObjectCounts[0] = targetObjectCount;

        testExpectedNumberOfEventsInQueue(eventListenerTest,expectedNumberOfEvents);
        testCountOfExpectedTargetObjects(eventListenerTest, targetObjectCounts);
    }

    @Test
    public void testAddAndAlterMultipleConnectedNode() {
        int noOfExpectedEvents = 12;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);

        Document e = new Document();
        e.setName("newE");
        session.save(e);

        Document f = new Document();
        f.setName("newF");
        session.save(f);

        Document g = new Document();
        g.setName("newG");
        session.save(g);

        // even though the node is connected, the connected nodes are not dirty, so the context registry should only contain 1 node at a time
        a.setName("newA");
        session.save(a);

        b.setName("newB");
        session.save(b);

        c.setName("newC");
        session.save(c);

        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[1];
        TargetObjectCount targetObjectCount = new TargetObjectCount();
        targetObjectCount.count = 12;
        targetObjectCount.targetObjectType = Document.class;
        targetObjectCounts[0] = targetObjectCount;

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
        testCountOfExpectedTargetObjects(eventListenerTest, targetObjectCounts);
    }

    @Test
    public void testAddOneNewRelationship() {
        int noOfExpectedEvents = 2;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);

        // shouldn't this fire events for both the Document and the Folder ?!?
        d.setFolder(folder);
        session.save(d);

        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[1];
        TargetObjectCount targetObjectCount = new TargetObjectCount();
        targetObjectCount.count = 1;
        targetObjectCount.targetObjectType = TransientRelationship.class;
        targetObjectCounts[0] = targetObjectCount;


        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
        testCountOfExpectedTargetObjects(eventListenerTest, targetObjectCounts);
    }

    @Test
    public void testAddNewRelationships() {
        int noOfExpectedEvents = 4;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);

        d.setFolder(folder);
        session.save(d);

        e.setFolder(folder);
        session.save(e);

        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[1];
        TargetObjectCount targetObjectCount = new TargetObjectCount();
        targetObjectCount.count = 4;
        targetObjectCount.targetObjectType = TransientRelationship.class;
        targetObjectCounts[0] = targetObjectCount;

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
        testCountOfExpectedTargetObjects(eventListenerTest, targetObjectCounts);
    }

    @Test
    public void testAlterOneRelationshipEntity() {
        // when altering a relationship, the relationship is first deleted, and then added. every relationship altered triggers 2 events(TransientRelationship and RelationshipEntity)
        int noOfExpectedEvents = 4;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);

        // i need a random date here, otherwise it will not be counted as dirty
        Random r = new Random();
        knowsJB.setSince(new Date((long) (1293861599+ r.nextDouble()*60*60*24*365)));
        session.save(knowsJB);

        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[2];
        TargetObjectCount targetObjectCount = new TargetObjectCount();
        targetObjectCount.count = 2;
        targetObjectCount.targetObjectType = TransientRelationship.class;
        targetObjectCounts[0] = targetObjectCount;

        TargetObjectCount targetObjectCount2 = new TargetObjectCount();
        targetObjectCount2.count = 2;
        targetObjectCount2.targetObjectType = Knows.class;
        targetObjectCounts[1] = targetObjectCount2;

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
        testCountOfExpectedTargetObjects(eventListenerTest, targetObjectCounts);
    }

    @Test
    public void testAlterMultipleRelationshipEntitiesWhoseObjectsAreNotConnected() {
        int noOfExpectedEvents = 14;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);
        Random r = new Random();
        knowsJB.setSince(new Date((long) (1293861599+ r.nextDouble()*60*60*24*365)));
        session.save(knowsJB);

        knowsLS.setSince(new Date((long) (1293861599+ r.nextDouble()*60*60*24*365)));
        session.save(knowsLS);

        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[2];
        TargetObjectCount targetObjectCount = new TargetObjectCount();
        targetObjectCount.count = 8;
        targetObjectCount.targetObjectType = TransientRelationship.class;
        targetObjectCounts[0] = targetObjectCount;

        TargetObjectCount targetObjectCount2 = new TargetObjectCount();
        targetObjectCount2.count = 6;
        targetObjectCount2.targetObjectType = Knows.class;
        targetObjectCounts[1] = targetObjectCount2;

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
        testCountOfExpectedTargetObjects(eventListenerTest, targetObjectCounts);
    }

    @Test
    public void testAlterMultipleRelationshipEntitiesWhoseObjectsAreConnected() {
        int noOfExpectedEvents = 4;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);
        Random r = new Random();
        knowsJL.setSince((new Date((long) (1293861599+ r.nextDouble()*60*60*24*365))));
        session.save(knowsJL);

        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[2];
        TargetObjectCount targetObjectCount = new TargetObjectCount();
        targetObjectCount.count = 2;
        targetObjectCount.targetObjectType = TransientRelationship.class;
        targetObjectCounts[0] = targetObjectCount;

        TargetObjectCount targetObjectCount2 = new TargetObjectCount();
        targetObjectCount2.count = 2;
        targetObjectCount2.targetObjectType = Knows.class;
        targetObjectCounts[1] = targetObjectCount2;

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
        testCountOfExpectedTargetObjects(eventListenerTest,targetObjectCounts);
    }

    @Test
    public void deleteOneUnconnectedNode() {
        int noOfExpectedEvents = 2;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);

        session.delete(d);

        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[1];
        TargetObjectCount targetObjectCount = new TargetObjectCount();
        targetObjectCount.count = 2;
        targetObjectCount.targetObjectType = Document.class;
        targetObjectCounts[0] = targetObjectCount;

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
        testCountOfExpectedTargetObjects(eventListenerTest,targetObjectCounts);
    }

    @Test
    public void deleteOneConnectedNode() {
        int noOfExpectedEvents = 2;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);

        session.delete(a);

        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[1];
        TargetObjectCount targetObjectCount = new TargetObjectCount();
        targetObjectCount.count = 2;
        targetObjectCount.targetObjectType = Document.class;
        targetObjectCounts[0] = targetObjectCount;

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
        testCountOfExpectedTargetObjects(eventListenerTest,targetObjectCounts);
    }

    // no events should fire
    @Test
    public void testDeleteUnpersistedEntity() {
        Document unpersistedDocument = new Document();
        int noOfExpectedEvents = 0;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);
        session.delete(unpersistedDocument);
        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
    }

    @Test
    public void deleteOneRelationship() {
        int noOfExpectedEvents = 2;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);

        session.delete(knowsJL);

        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[1];
        TargetObjectCount targetObjectCount = new TargetObjectCount();
        targetObjectCount.count = 2;
        targetObjectCount.targetObjectType = Knows.class;
        targetObjectCounts[0] = targetObjectCount;

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
        testCountOfExpectedTargetObjects(eventListenerTest, targetObjectCounts);
    }

    @Test
    public void deleteMultipleRelationships() {
        int noOfExpectedEvents = 4;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);

        session.delete(knowsJL);
        session.delete(knowsJB);

        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[1];
        TargetObjectCount targetObjectCount = new TargetObjectCount();
        targetObjectCount.count = 4;
        targetObjectCount.targetObjectType = Knows.class;
        targetObjectCounts[0] = targetObjectCount;

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
        testCountOfExpectedTargetObjects(eventListenerTest, targetObjectCounts);
    }

    // session.save(<? implements Collection>);
    @Test
    public void saveMultipleNewNodes() {
        int noOfExpectedEvents = 6;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);

        a.setName("newA");
        b.setName("newB");
        c.setName("newC");
        List<Object> saveList = new LinkedList<>();
        saveList.add(a);saveList.add(b);saveList.add(c);

        session.save(saveList);

        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[1];
        TargetObjectCount targetObjectCount = new TargetObjectCount();
        // the method looks inside the list of objects in the event; actually, only 2 events are thrown
        targetObjectCount.count = 6;
        targetObjectCount.targetObjectType = Document.class;
        targetObjectCounts[0] = targetObjectCount;

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
        testCountOfExpectedTargetObjects(eventListenerTest, targetObjectCounts);
    }

    @Test
    public void deleteMultipleNodes() {
        int noOfExpectedEvents = 6;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);

        List<Object> saveList = new LinkedList<>();
        saveList.add(a);saveList.add(b);saveList.add(c);

        session.delete(saveList);

        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[1];
        TargetObjectCount targetObjectCount = new TargetObjectCount();
        targetObjectCount.count = 6;
        targetObjectCount.targetObjectType = Document.class;
        targetObjectCounts[0] = targetObjectCount;

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
        testCountOfExpectedTargetObjects(eventListenerTest, targetObjectCounts);
    }

    @Test
    public void deleteAllOfType() {
        int noOfExpectedEvents = 2;
        eventListenerTest = new EventListenerTest(noOfExpectedEvents);
        session.register(eventListenerTest);

        session.deleteAll(Document.class);

        TargetObjectCount[] targetObjectCounts = new TargetObjectCount[1];
        TargetObjectCount targetObjectCount = new TargetObjectCount();
        targetObjectCount.count = 2;
        targetObjectCount.targetObjectType = Class.class;
        targetObjectCounts[0] = targetObjectCount;

        testExpectedNumberOfEventsInQueue(eventListenerTest,noOfExpectedEvents);
        testCountOfExpectedTargetObjects(eventListenerTest, targetObjectCounts);
    }

    class EventListenerTest implements EventListener {

        public Event[] eventsCaptured;
        public int currentIndex = 0;

        public EventListenerTest(int noOfExpectedEvents) {
            eventsCaptured = new Event[noOfExpectedEvents];
            currentIndex = 0;
        }

        @Override
        public void update(Event event) {
            eventsCaptured[currentIndex++] = event;
            System.out.println("caught an event of type "+event.toString());
        }

    }

    @Test
    public void eventIntegrationTest() {
        this.eventListenerTest = new EventListenerTest(8);
        session.register(eventListenerTest);

        a.setName("newA");
        b.setName("newB");
        c.setName("newC");
        folder.setName("newFolder");

        session.save(folder);
        Document aa = session.load(Document.class, a.getId());
        Document bb = session.load(Document.class, b.getId());
        System.out.println(aa.getName());
        System.out.println(bb.getName());
        System.out.println(folder.getName());

        testExpectedNumberOfEventsInQueue(eventListenerTest, 8);
    }

    private void testExpectedNumberOfEventsInQueue(EventListenerTest eventListener, int noOfExpectedEvents) {
        assertTrue(eventListener.currentIndex == noOfExpectedEvents);
    }

    private void testCountOfExpectedTargetObjects(EventListenerTest eventListener, TargetObjectCount[] targetObjectCountArray) {
        int noOfConsideredObjects = 0; int noOfCaughtObjects = 0;
        for(TargetObjectCount targetObjectCount : targetObjectCountArray) {
            int[] result = getConsideredObjectsAndCaughtObjects(eventListener, targetObjectCount);
            noOfCaughtObjects += result[0];
            noOfConsideredObjects = result[1];
        }
        assertTrue(noOfCaughtObjects == noOfConsideredObjects);
    }


    private int[] getConsideredObjectsAndCaughtObjects(EventListenerTest eventListener, TargetObjectCount targetObjectCount) {
        int noOfCaughtObjectsOfType = 0;
        int noOfConsideredObjects = 0;
        int[] result = new int[2];
        for(int i=0;i<eventListener.eventsCaptured.length;i++) {
            Object caughtObject = eventListener.eventsCaptured[i].getTargetObject();
                noOfConsideredObjects++;
                if( targetObjectCount.targetObjectType.isInstance(caughtObject) ) {
                    noOfCaughtObjectsOfType++;
                }
        }
        result[0] = noOfCaughtObjectsOfType;
        result[1] = noOfConsideredObjects;
        return result;
    }

    class TargetObjectCount {
        public Class targetObjectType;
        public int count;
    }

    @After
    public void clean() throws IOException {
        session.purgeDatabase();
    }

}
