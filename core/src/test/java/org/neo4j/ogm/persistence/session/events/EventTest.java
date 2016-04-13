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

    protected Session session;
    protected Document a;
    protected Document b;
    protected Document c;
    protected Document d;
    protected Document e;
    protected Folder folder;

    protected Actor jim, bruce, lee, stan;
    protected Knows knowsJB;
    protected Knows knowsLS;
    protected Knows knowsJL;

    protected TestEventListener eventListener = new TestEventListener();

    @Before
    public void init() throws IOException {

        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.filesystem", "org.neo4j.ogm.domain.cineasts.annotated");

        session = sessionFactory.openSession();
        session.purgeDatabase();

        session.register(eventListener);

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

        session.save(d);
        System.out.println("****************************************");

        session.save(e);
        System.out.println("****************************************");

        session.save(folder);
        System.out.println("****************************************");

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
        //System.out.println("****************************************");

        knowsJL = new Knows();
        knowsJL.setFirstActor(jim);
        knowsJL.setSecondActor(lee);
        knowsJL.setSince(new Date());

        jim.getKnows().add(knowsJL);
        //session.save(knowsJL);
        //System.out.println("****************************************");

        knowsLS = new Knows();
        knowsLS.setFirstActor(lee);
        knowsLS.setSecondActor(stan);
        knowsLS.setSince(new Date());

        lee.getKnows().add(knowsLS);
        session.save(jim);
        System.out.println("****************************************");

        eventListener.clear();

    }

    class TestEventListener implements EventListener {

        public List<Event> eventsCaptured;

        public TestEventListener() {
            eventsCaptured = new ArrayList<>();
        }

        @Override
        public void onPreSave(Event event) {
            eventsCaptured.add(event);
            System.out.println(event.toString());
        }

        @Override
        public void onPostSave(Event event) {
            eventsCaptured.add(event);
            System.out.println(event.toString());
        }

        @Override
        public void onPreDelete(Event event) {
            eventsCaptured.add(event);
            System.out.println(event.toString());
        }

        @Override
        public void onPostDelete(Event event) {
            eventsCaptured.add(event);
            System.out.println(event.toString());
        }

        public boolean captured(Object o, Event.TYPE lifecycle) {
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

        public void clear() {
            eventsCaptured.clear();
        }
    }

    @After
    public void clean() throws IOException {
        session.purgeDatabase();
    }

}
