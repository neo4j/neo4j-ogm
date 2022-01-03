/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.persistence.session.events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.neo4j.ogm.domain.cineasts.annotated.Actor;
import org.neo4j.ogm.domain.cineasts.annotated.Knows;
import org.neo4j.ogm.domain.filesystem.Document;
import org.neo4j.ogm.domain.filesystem.Folder;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.event.Event;
import org.neo4j.ogm.session.event.EventListener;
import org.neo4j.ogm.session.event.PersistenceEvent;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Mihai Raulea
 * @author Vince Bickers
 */
public abstract class EventTestBaseClass extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    static TestEventListener eventListener;

    protected Session session;
    protected Document a;
    protected Document b;
    protected Document c;
    protected Document d;
    protected Document e;
    protected Folder folder;

    Actor jim;
    Actor bruce;
    Actor lee;
    Actor stan;
    Knows knowsJB;
    private Knows knowsLS;
    Knows knowsJL;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.filesystem",
            "org.neo4j.ogm.domain.cineasts.annotated");
        eventListener = new TestEventListener();
        sessionFactory.register(eventListener);
    }

    @Before
    public void init() throws IOException {

        session = sessionFactory.openSession();
        session.purgeDatabase();

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

        session.save(e);

        session.save(folder);

        jim = new Actor("Jim");
        bruce = new Actor("Bruce");
        lee = new Actor("Lee");
        stan = new Actor("Stan");

        knowsJB = new Knows();
        knowsJB.setFirstActor(jim);
        knowsJB.setSecondActor(bruce);
        knowsJB.setSince(new Date());

        jim.getKnows().add(knowsJB);

        knowsJL = new Knows();
        knowsJL.setFirstActor(jim);
        knowsJL.setSecondActor(lee);
        knowsJL.setSince(new Date());

        jim.getKnows().add(knowsJL);

        knowsLS = new Knows();
        knowsLS.setFirstActor(lee);
        knowsLS.setSecondActor(stan);
        knowsLS.setSince(new Date());

        lee.getKnows().add(knowsLS);
        session.save(jim);

        eventListener.clear();
    }

    static class TestEventListener implements EventListener {

        List<Event> eventsCaptured;

        TestEventListener() {
            eventsCaptured = new ArrayList<>();
        }

        @Override
        public void onPreSave(Event event) {
            eventsCaptured.add(event);
        }

        @Override
        public void onPostSave(Event event) {
            eventsCaptured.add(event);
        }

        @Override
        public void onPreDelete(Event event) {
            eventsCaptured.add(event);
        }

        @Override
        public void onPostDelete(Event event) {
            eventsCaptured.add(event);
        }

        public boolean captured(Object o, Event.TYPE lifecycle) {
            return get(o, lifecycle) != null;
        }

        public Event get(Object o, Event.TYPE lifecycle) {
            Event event = new PersistenceEvent(o, lifecycle);
            for (Event captured : eventsCaptured) {
                if (captured.toString().equals(event.toString())) {
                    return captured;
                }
            }
            return null;
        }

        public int count() {
            return eventsCaptured.size();
        }

        public void clear() {
            eventsCaptured.clear();
        }
    }

    @After
    public void clean() {
        session.purgeDatabase();
    }
}
