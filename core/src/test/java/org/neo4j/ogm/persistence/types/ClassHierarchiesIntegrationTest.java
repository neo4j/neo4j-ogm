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

package org.neo4j.ogm.persistence.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.neo4j.ogm.testutil.GraphTestUtils.assertSameGraph;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.ogm.domain.hierarchy.domain.annotated.*;
import org.neo4j.ogm.domain.hierarchy.domain.people.Bloke;
import org.neo4j.ogm.domain.hierarchy.domain.people.Entity;
import org.neo4j.ogm.domain.hierarchy.domain.people.Female;
import org.neo4j.ogm.domain.hierarchy.domain.people.Male;
import org.neo4j.ogm.domain.hierarchy.domain.people.Person;
import org.neo4j.ogm.domain.hierarchy.domain.plain.PlainChildWithAbstractParentAndAnnotatedSuperclass;
import org.neo4j.ogm.domain.hierarchy.domain.plain.PlainChildWithAnnotatedAbstractNamedParent;
import org.neo4j.ogm.domain.hierarchy.domain.plain.PlainChildWithAnnotatedAbstractParent;
import org.neo4j.ogm.domain.hierarchy.domain.plain.PlainChildWithAnnotatedConcreteNamedParent;
import org.neo4j.ogm.domain.hierarchy.domain.plain.PlainChildWithAnnotatedConcreteParent;
import org.neo4j.ogm.domain.hierarchy.domain.plain.PlainChildWithAnnotatedConcreteSuperclass;
import org.neo4j.ogm.domain.hierarchy.domain.plain.PlainChildWithAnnotatedInterfaceParent;
import org.neo4j.ogm.domain.hierarchy.domain.plain.PlainChildWithAnnotatedNamedInterfaceParent;
import org.neo4j.ogm.domain.hierarchy.domain.plain.PlainChildWithAnnotatedSuperInterface;
import org.neo4j.ogm.domain.hierarchy.domain.plain.PlainChildWithPlainAbstractParent;
import org.neo4j.ogm.domain.hierarchy.domain.plain.PlainChildWithPlainConcreteParent;
import org.neo4j.ogm.domain.hierarchy.domain.plain.PlainChildWithPlainConcreteParentImplementingInterface;
import org.neo4j.ogm.domain.hierarchy.domain.plain.PlainChildWithPlainInterfaceChild;
import org.neo4j.ogm.domain.hierarchy.domain.plain.PlainChildWithPlainInterfaceParent;
import org.neo4j.ogm.domain.hierarchy.domain.plain.PlainConcreteParent;
import org.neo4j.ogm.domain.hierarchy.domain.plain.PlainSingleClass;
import org.neo4j.ogm.domain.hierarchy.domain.trans.PlainChildOfTransientInterface;
import org.neo4j.ogm.domain.hierarchy.domain.trans.PlainChildOfTransientParent;
import org.neo4j.ogm.domain.hierarchy.domain.trans.PlainClassWithTransientFields;
import org.neo4j.ogm.domain.hierarchy.domain.trans.TransientChildWithPlainConcreteParent;
import org.neo4j.ogm.domain.hierarchy.domain.trans.TransientSingleClass;
import org.neo4j.ogm.domain.hierarchy.domain.trans.TransientSingleClassWithId;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.GraphTestUtils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * Integration test for label-based mapping of class hierarchies.
 * <p/>
 * The rules should be as follows:
 * <ul>
 * <li>any plain concrete class in the hierarchy generates a label by default</li>
 * <li>plain abstract class does not generate a label by default</li>
 * <li>plain interface does not generate a label by default</li>
 * <li>any class annotated with @NodeEntity or @NodeEntity(label="something") generates a label</li>
 * <li>empty or null labels must not be allowed</li>
 * <li>classes / hierarchies that are not to be persisted must be annotated with @Transient</li>
 * </ul>
 *
 * @author Michal Bachman
 * @author Luanne Misquitta
 */
public class ClassHierarchiesIntegrationTest extends MultiDriverTestClass {

    private static final SessionFactory sessionFactory = new SessionFactory(baseConfiguration, "org.neo4j.ogm.domain.hierarchy.domain");

    private Session session;

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    private GraphDatabaseService getDatabase() {
        return getGraphDatabaseService();
    }

    @Test
    public void annotatedChildWithAnnotatedAbstractNamedParent() {
        session.save(new AnnotatedChildWithAnnotatedAbstractNamedParent());

        assertSameGraph(getDatabase(), "CREATE (:AnnotatedChildWithAnnotatedAbstractNamedParent:Parent)");

        assertNotNull(session.loadAll(AnnotatedChildWithAnnotatedAbstractNamedParent.class).iterator().next());
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void annotatedChildWithAnnotatedNamedInterfaceParent() {
        session.save(new AnnotatedChildWithAnnotatedNamedInterfaceParent());

        assertSameGraph(getDatabase(), "CREATE (:AnnotatedChildWithAnnotatedNamedInterfaceParent:Parent)");

        assertNotNull(session.loadAll(AnnotatedChildWithAnnotatedNamedInterfaceParent.class).iterator().next());
    }


    @Test
    public void annotatedChildWithAnnotatedAbstractParent() {
        session.save(new AnnotatedChildWithAnnotatedAbstractParent());

        assertSameGraph(getDatabase(), "CREATE (:AnnotatedChildWithAnnotatedAbstractParent:AnnotatedAbstractParent)");

        assertNotNull(session.loadAll(AnnotatedChildWithAnnotatedAbstractParent.class).iterator().next());
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void annotatedChildWithAnnotatedInterfaceParent() {
        session.save(new AnnotatedChildWithAnnotatedInterfaceParent());

        assertSameGraph(getDatabase(), "CREATE (:AnnotatedChildWithAnnotatedInterfaceParent:AnnotatedInterface)");

        assertNotNull(session.loadAll(AnnotatedChildWithAnnotatedInterfaceParent.class).iterator().next());
    }

    @Test
    public void annotatedChildWithAnnotatedConcreteNamedParent() {
        session.save(new AnnotatedChildWithAnnotatedConcreteNamedParent());

        assertSameGraph(getDatabase(), "CREATE (:AnnotatedChildWithAnnotatedConcreteNamedParent:Parent)");

        assertNotNull(session.loadAll(AnnotatedChildWithAnnotatedConcreteNamedParent.class).iterator().next());
    }

    @Test
    public void annotatedChildWithAnnotatedConcreteParent() {
        session.save(new AnnotatedChildWithAnnotatedConcreteParent());

        assertSameGraph(getDatabase(), "CREATE (:AnnotatedChildWithAnnotatedConcreteParent:AnnotatedConcreteParent)");

        assertNotNull(session.loadAll(AnnotatedChildWithAnnotatedConcreteParent.class).iterator().next());
    }

    @Test
    public void annotatedChildWithPlainAbstractParent() {
        session.save(new AnnotatedChildWithPlainAbstractParent());

        assertSameGraph(getDatabase(), "CREATE (:AnnotatedChildWithPlainAbstractParent)");

        assertNotNull(session.loadAll(AnnotatedChildWithPlainAbstractParent.class).iterator().next());
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void annotatedChildWithPlainInterfaceParent() {
        session.save(new AnnotatedChildWithPlainInterfaceParent());

        assertSameGraph(getDatabase(), "CREATE (:AnnotatedChildWithPlainInterfaceParent)");

        assertNotNull(session.loadAll(AnnotatedChildWithPlainInterfaceParent.class).iterator().next());
    }

    @Test
    public void annotatedChildWithPlainConcreteParent() {
        session.save(new AnnotatedChildWithPlainConcreteParent());

        assertSameGraph(getDatabase(), "CREATE (:AnnotatedChildWithPlainConcreteParent:PlainConcreteParent)");

        assertNotNull(session.loadAll(AnnotatedChildWithPlainConcreteParent.class).iterator().next());
    }

    @Test
    @Ignore("class hierarchies are invalid for this test: multiple classes labelled 'Child' and 'Parent'")
    public void annotatedNamedChildWithAnnotatedAbstractNamedParent() {
        session.save(new AnnotatedNamedChildWithAnnotatedAbstractNamedParent());

        assertSameGraph(getDatabase(), "CREATE (:Child:Parent)");

        assertNotNull(session.loadAll(AnnotatedNamedChildWithAnnotatedAbstractNamedParent.class).iterator().next());
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    @Ignore("class hierarchies are invalid for this test: multiple classes labelled 'Child' and 'Parent'")
    public void annotatedNamedChildWithAnnotatedNamedInterface() {
        session.save(new AnnotatedNamedChildWithAnnotatedNamedInterfaceParent());

        assertSameGraph(getDatabase(), "CREATE (:Child:Parent)");

        assertNotNull(session.loadAll(AnnotatedNamedChildWithAnnotatedNamedInterfaceParent.class).iterator().next());
    }

    @Test
    //@Ignore("class hierarchies are invalid for this test: multiple classes labelled 'Child' and 'Parent'")
    public void annotatedNamedChildWithAnnotatedAbstractParent() {
        session.save(new AnnotatedNamedChildWithAnnotatedAbstractParent());

        assertSameGraph(getDatabase(), "CREATE (:Child:AnnotatedAbstractParent)");

        assertNotNull(session.loadAll(AnnotatedNamedChildWithAnnotatedAbstractParent.class).iterator().next());
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    @Ignore("class hierarchies are invalid for this test: multiple classes labelled 'Child' and 'Parent'")
    public void annotatedNamedChildWithAnnotatedInterfaceParent() {
        session.save(new AnnotatedNamedChildWithAnnotatedInterfaceParent());

        assertSameGraph(getDatabase(), "CREATE (:Child:AnnotatedInterface)");

        assertNotNull(session.loadAll(AnnotatedNamedChildWithAnnotatedInterfaceParent.class).iterator().next());
    }

    @Test
    @Ignore("class hierarchies are invalid for this test: multiple classes labelled 'Child' and 'Parent'")
    public void annotatedNamedChildWithAnnotatedConcreteNamedParent() {
        session.save(new AnnotatedNamedChildWithAnnotatedConcreteNamedParent());

        assertSameGraph(getDatabase(), "CREATE (:Child:Parent)");

        assertNotNull(session.loadAll(AnnotatedNamedChildWithAnnotatedConcreteNamedParent.class).iterator().next());
    }

    @Test
    @Ignore("class hierarchies are invalid for this test: multiple classes labelled 'Child' and 'Parent'")
    public void annotatedNamedChildWithAnnotatedConcreteParent() {
        session.save(new AnnotatedNamedChildWithAnnotatedConcreteParent());

        assertSameGraph(getDatabase(), "CREATE (:Child:AnnotatedConcreteParent)");

        assertNotNull(session.loadAll(AnnotatedNamedChildWithAnnotatedConcreteParent.class).iterator().next());
    }

    @Test
    @Ignore("class hierarchies are invalid for this test: multiple classes labelled 'Child' and 'Parent'")
    public void annotatedNamedChildWithPlainAbstractParent() {
        session.save(new AnnotatedNamedChildWithPlainAbstractParent());

        assertSameGraph(getDatabase(), "CREATE (:Child)");

        assertNotNull(session.loadAll(AnnotatedNamedChildWithPlainAbstractParent.class).iterator().next());
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    @Ignore("class hierarchies are invalid for this test: multiple classes labelled 'Child' and 'Parent'")
    public void annotatedNamedChildWithPlainInterfaceParent() {
        session.save(new AnnotatedNamedChildWithPlainInterfaceParent());

        assertSameGraph(getDatabase(), "CREATE (:Child)");

        assertNotNull(session.loadAll(AnnotatedNamedChildWithPlainInterfaceParent.class).iterator().next());
    }

    @Test
    @Ignore("class hierarchies are invalid for this test: multiple classes labelled 'Child' and 'Parent'")
    public void annotatedNamedChildWithPlainConcreteParent() {
        session.save(new AnnotatedNamedChildWithPlainConcreteParent());

        assertSameGraph(getDatabase(), "CREATE (:Child:PlainConcreteParent)");

        assertNotNull(session.loadAll(AnnotatedNamedChildWithPlainConcreteParent.class).iterator().next());
    }

    @Test
    public void annotatedNamedSingleClass() {
        session.save(new AnnotatedNamedSingleClass());

        assertSameGraph(getDatabase(), "CREATE (:Single)");

        assertNotNull(session.loadAll(AnnotatedNamedSingleClass.class).iterator().next());
    }

    @Test
    public void annotatedSingleClass() {
        session.save(new AnnotatedSingleClass());

        assertSameGraph(getDatabase(), "CREATE (:AnnotatedSingleClass)");

        assertNotNull(session.loadAll(AnnotatedSingleClass.class).iterator().next());
    }

    @Test
    public void plainChildWithAnnotatedAbstractNamedParent() {
        session.save(new PlainChildWithAnnotatedAbstractNamedParent());

        assertSameGraph(getDatabase(), "CREATE (:PlainChildWithAnnotatedAbstractNamedParent:Parent)");

        assertNotNull(session.loadAll(PlainChildWithAnnotatedAbstractNamedParent.class).iterator().next());
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void plainChildWithAnnotatedNamedInterfaceParent() {
        session.save(new PlainChildWithAnnotatedNamedInterfaceParent());

        assertSameGraph(getDatabase(), "CREATE (:PlainChildWithAnnotatedNamedInterfaceParent:Parent)");

        assertNotNull(session.loadAll(PlainChildWithAnnotatedNamedInterfaceParent.class).iterator().next());
    }

    @Test
    public void plainChildWithAnnotatedAbstractParent() {
        session.save(new PlainChildWithAnnotatedAbstractParent());

        assertSameGraph(getDatabase(), "CREATE (:PlainChildWithAnnotatedAbstractParent:AnnotatedAbstractParent)");

        assertNotNull(session.loadAll(PlainChildWithAnnotatedAbstractParent.class).iterator().next());
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void plainChildWithAnnotatedInterfaceParent() {
        session.save(new PlainChildWithAnnotatedInterfaceParent());

        assertSameGraph(getDatabase(), "CREATE (:PlainChildWithAnnotatedInterfaceParent:AnnotatedInterface)");

        assertNotNull(session.loadAll(PlainChildWithAnnotatedInterfaceParent.class).iterator().next());
    }

    @Test
    public void plainChildWithAnnotatedConcreteNamedParent() {
        session.save(new PlainChildWithAnnotatedConcreteNamedParent());

        assertSameGraph(getDatabase(), "CREATE (:PlainChildWithAnnotatedConcreteNamedParent:Parent)");

        assertNotNull(session.loadAll(PlainChildWithAnnotatedConcreteNamedParent.class).iterator().next());
    }

    @Test
    public void plainChildWithAnnotatedConcreteParent() {
        session.save(new PlainChildWithAnnotatedConcreteParent());

        assertSameGraph(getDatabase(), "CREATE (:PlainChildWithAnnotatedConcreteParent:AnnotatedConcreteParent)");

        assertNotNull(session.loadAll(PlainChildWithAnnotatedConcreteParent.class).iterator().next());
    }

    @Test
    public void plainChildWithPlainAbstractParent() {
        session.save(new PlainChildWithPlainAbstractParent());

        assertSameGraph(getDatabase(), "CREATE (:PlainChildWithPlainAbstractParent)");

        assertNotNull(session.loadAll(PlainChildWithPlainAbstractParent.class).iterator().next());
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void plainChildWithPlainInterfaceParent() {
        session.save(new PlainChildWithPlainInterfaceParent());

        assertSameGraph(getDatabase(), "CREATE (:PlainChildWithPlainInterfaceParent)");

        assertNotNull(session.loadAll(PlainChildWithPlainInterfaceParent.class).iterator().next());
    }


    @Test
    public void plainChildWithPlainConcreteParent() {
        session.save(new PlainChildWithPlainConcreteParent());

        assertSameGraph(getDatabase(), "CREATE (:PlainChildWithPlainConcreteParent:PlainConcreteParent)");

        assertNotNull(session.loadAll(PlainChildWithPlainConcreteParent.class).iterator().next());
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void plainChildWithPlainConcreteParentImplementingInterface() {
        session.save(new PlainChildWithPlainConcreteParentImplementingInterface());

        assertSameGraph(getDatabase(), "CREATE (:PlainChildWithPlainConcreteParentImplementingInterface:PlainConcreteParent)");

        assertNotNull(session.loadAll(PlainChildWithPlainConcreteParentImplementingInterface.class).iterator().next());
        assertNotNull(session.loadAll(PlainConcreteParent.class).iterator().next());
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void plainChildWithPlainInterfaceChild() {
        session.save(new PlainChildWithPlainInterfaceChild());

        assertSameGraph(getDatabase(), "CREATE (:PlainChildWithPlainInterfaceChild)");

        assertNotNull(session.loadAll(PlainChildWithPlainInterfaceChild.class).iterator().next());
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void plainChildWithAnnotatedSuperInterface() {
        /*
        PlainChildWithAnnotatedSuperInterface->PlainInterfaceChildWithAnnotatedParentInterface->AnnotatedInterface
         */
        session.save(new PlainChildWithAnnotatedSuperInterface());

        assertSameGraph(getDatabase(), "CREATE (:PlainChildWithAnnotatedSuperInterface:AnnotatedInterface)");
        assertNotNull(session.loadAll(PlainChildWithAnnotatedSuperInterface.class).iterator().next());
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void annotatedChildWithAnnotatedInterface() {
        /*
        AnnotatedChildWithAnnotatedInterface->AnnotatedInterfaceWithSingleImpl
         */
        session.save(new AnnotatedChildWithAnnotatedInterface());

        assertSameGraph(getDatabase(), "CREATE (:AnnotatedChildWithAnnotatedInterface:AnnotatedInterfaceWithSingleImpl)");
        assertNotNull(session.loadAll(AnnotatedChildWithAnnotatedInterface.class).iterator().next());
        assertNotNull(session.loadAll(AnnotatedInterfaceWithSingleImpl.class).iterator().next()); //AnnotatedInterfaceWithSingleImpl has a single implementation so we should be able to load it
        assertEquals("org.neo4j.ogm.domain.hierarchy.domain.annotated.AnnotatedChildWithAnnotatedInterface", session.loadAll(AnnotatedInterfaceWithSingleImpl.class).iterator().next().getClass().getName());
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void plainChildWithAnnotatedSuperclass() {
        /*
           PlainChildWithAnnotatedConcreteSuperclass->PlainChildWithAnnotatedConcreteParent->AnnotatedConcreteParent
         */
        session.save(new PlainChildWithAnnotatedConcreteSuperclass());

        assertSameGraph(getDatabase(), "CREATE (:PlainChildWithAnnotatedConcreteSuperclass:PlainChildWithAnnotatedConcreteParent:AnnotatedConcreteParent)");
        assertNotNull(session.loadAll(PlainChildWithAnnotatedConcreteSuperclass.class).iterator().next());
        assertNotNull(session.loadAll(PlainChildWithAnnotatedConcreteParent.class).iterator().next());
        assertNotNull(session.loadAll(AnnotatedConcreteParent.class).iterator().next());
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void plainChildWithAbstractParentAndAnnotatedSuperclass() {
        /*
           PlainChildWithAbstractParentAndAnnotatedSuperclass->PlainAbstractWithAnnotatedParent->AnnotatedSingleClass
         */
        session.save(new PlainChildWithAbstractParentAndAnnotatedSuperclass());
        assertSameGraph(getDatabase(), "CREATE (:PlainChildWithAbstractParentAndAnnotatedSuperclass:AnnotatedSingleClass)");
        assertNotNull(session.loadAll(PlainChildWithAbstractParentAndAnnotatedSuperclass.class).iterator().next());
//        assertNotNull(session.loadAll(PlainAbstractWithAnnotatedParent.class).iterator().next());
        assertNotNull(session.loadAll(AnnotatedSingleClass.class).iterator().next());
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void annotatedChildWithMultipleAnnotatedInterfaces() {
        session.save(new AnnotatedChildWithMultipleAnnotatedInterfaces());

        assertSameGraph(getDatabase(), "CREATE (:AnnotatedChildWithMultipleAnnotatedInterfaces:AnnotatedInterface:Parent)");

        assertNotNull(session.loadAll(AnnotatedChildWithMultipleAnnotatedInterfaces.class).iterator().next());
        assertEquals(1, session.loadAll(AnnotatedInterface.class).size());
        assertEquals(1, session.loadAll(AnnotatedNamedInterfaceParent.class).size());
    }

    @Test
    public void plainSingleClass() {
        session.save(new PlainSingleClass());

        assertSameGraph(getDatabase(), "CREATE (:PlainSingleClass)");

        assertNotNull(session.loadAll(PlainSingleClass.class).iterator().next());
    }

    @Test
    public void plainChildOfTransientParent() {
        session.save(new PlainChildOfTransientParent());

        try (Transaction tx = getDatabase().beginTx()) {
            assertFalse(GraphTestUtils.allNodes(getDatabase()).iterator().hasNext());
            tx.success();
        }
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void plainChildOfTransientInterface() {
        session.save(new PlainChildOfTransientInterface());
        try (Transaction tx = getDatabase().beginTx()) {
            assertFalse(GraphTestUtils.allNodes(getDatabase()).iterator().hasNext());
            tx.success();
        }
    }

    @Test
    public void transientChildWithPlainConcreteParent() {
        session.save(new TransientChildWithPlainConcreteParent());

        try (Transaction tx = getDatabase().beginTx()) {
            assertFalse(GraphTestUtils.allNodes(getDatabase()).iterator().hasNext());
            tx.success();
        }
    }

    @Test
    public void transientSingleClass() {
        session.save(new TransientSingleClass());

        try (Transaction tx = getDatabase().beginTx()) {
            assertFalse(GraphTestUtils.allNodes(getDatabase()).iterator().hasNext());
            tx.success();
        }
    }

    @Test
    public void transientSingleClassWithId() {
        session.save(new TransientSingleClassWithId());

        try (Transaction tx = getDatabase().beginTx()) {
            assertFalse(GraphTestUtils.allNodes(getDatabase()).iterator().hasNext());
            tx.success();
        }
    }

    @Test
    public void plainClassWithTransientFields() {

        PlainClassWithTransientFields toSave = new PlainClassWithTransientFields();

        toSave.setAnotherTransientField(new PlainSingleClass());
        toSave.setTransientField(new PlainChildOfTransientParent());
        toSave.setYetAnotherTransientField(new PlainSingleClass());

        session.save(toSave);

        assertSameGraph(getDatabase(), "CREATE (:PlainClassWithTransientFields)");

        assertNotNull(session.loadAll(PlainClassWithTransientFields.class).iterator().next());
    }

    @Test
    public void shouldNotBeAbleToLoadClassOfWrongType() {
        session.save(new AnnotatedNamedSingleClass());
        assertFalse(session.loadAll(PlainSingleClass.class).iterator().hasNext());
    }

    @Test
    public void shouldSaveHierarchy() {
        session.save(new Female("Daniela"));
        session.save(new Male("Michal"));
        session.save(new Bloke("Adam"));

        assertSameGraph(getDatabase(), "CREATE (:Female:Person {name:'Daniela'})," +
                "(:Male:Person {name:'Michal'})," +
                "(:Bloke:Male:Person {name:'Adam'})");
    }

    @Test
    public void shouldSaveHierarchy2() {
        session.save(Arrays.asList(new Female("Daniela"), new Male("Michal"), new Bloke("Adam")));

        assertSameGraph(getDatabase(), "CREATE (:Female:Person {name:'Daniela'})," +
                "(:Male:Person {name:'Michal'})," +
                "(:Bloke:Male:Person {name:'Adam'})");
    }

    @Test
    public void shouldReadHierarchyAndRetrieveBySuperclass() {

        Female daniela = new Female("Daniela");
        Male michal = new Male("Michal");
        Bloke adam = new Bloke("Adam");

        session.save(Arrays.asList(daniela, michal, adam));

        Collection<Entity> entities = session.loadAll(Entity.class);

        Collection<Person> people = session.loadAll(Person.class);
        Collection<Male> males = session.loadAll(Male.class);
        Collection<Female> females = session.loadAll(Female.class);
        Collection<Bloke> blokes = session.loadAll(Bloke.class);

        assertTrue("Shouldn't be able to load by non-annotated, abstract classes", entities.isEmpty());

        assertEquals(3, people.size());
        assertEquals(people.size(), session.countEntitiesOfType(Person.class));
        assertTrue(people.containsAll(Arrays.asList(daniela, michal, adam)));

        assertEquals(2, males.size());
        assertTrue(males.containsAll(Arrays.asList(michal, adam)));

        assertEquals(1, females.size());
        assertEquals(females.size(), session.countEntitiesOfType(Female.class));
        assertTrue(females.contains(daniela));

        assertEquals(1, blokes.size());
        assertTrue(blokes.contains(adam));

    }

    @Test
    public void shouldReadHierarchy2() {

        getDatabase().execute("CREATE (:Female:Person:Entity {name:'Daniela'})," +
                "(:Male:Person:Entity {name:'Michal'})," +
                "(:Bloke:Male:Person:Entity {name:'Adam'})");

        Female daniela = new Female("Daniela");
        Male michal = new Male("Michal");
        Bloke adam = new Bloke("Adam");

        Collection<Entity> entities = session.loadAll(Entity.class);
        Collection<Person> people = session.loadAll(Person.class);
        Collection<Male> males = session.loadAll(Male.class);
        Collection<Female> females = session.loadAll(Female.class);
        Collection<Bloke> blokes = session.loadAll(Bloke.class);

        assertEquals(3, entities.size());
        assertTrue(entities.containsAll(Arrays.asList(daniela, michal, adam)));

        assertEquals(3, people.size());
        assertTrue(people.containsAll(Arrays.asList(daniela, michal, adam)));

        assertEquals(2, males.size());
        assertTrue(males.containsAll(Arrays.asList(michal, adam)));

        assertEquals(1, females.size());
        assertTrue(females.contains(daniela));

        assertEquals(1, blokes.size());
        assertTrue(blokes.contains(adam));
    }

    @Test
    public void shouldReadHierarchy3() {
        getDatabase().execute("CREATE (:Female:Person {name:'Daniela'})," +
                "(:Male:Person {name:'Michal'})," +
                "(:Bloke:Male:Person {name:'Adam'})");

        Female daniela = new Female("Daniela");
        Male michal = new Male("Michal");
        Bloke adam = new Bloke("Adam");

        Collection<Person> people = session.loadAll(Person.class);
        Collection<Male> males = session.loadAll(Male.class);
        Collection<Female> females = session.loadAll(Female.class);
        Collection<Bloke> blokes = session.loadAll(Bloke.class);

        assertEquals(3, people.size());
        assertTrue(people.containsAll(Arrays.asList(daniela, michal, adam)));

        assertEquals(2, males.size());
        assertTrue(males.containsAll(Arrays.asList(michal, adam)));

        assertEquals(1, females.size());
        assertTrue(females.contains(daniela));

        assertEquals(1, blokes.size());
        assertTrue(blokes.contains(adam));
    }

    @Test
    public void shouldReadHierarchy4() {
        getDatabase().execute("CREATE (:Female {name:'Daniela'})," +
                "(:Male {name:'Michal'})," +
                "(:Bloke:Male {name:'Adam'})");

        Female daniela = new Female("Daniela");
        Male michal = new Male("Michal");
        Bloke adam = new Bloke("Adam");

        Collection<Male> males = session.loadAll(Male.class);
        Collection<Female> females = session.loadAll(Female.class);
        Collection<Bloke> blokes = session.loadAll(Bloke.class);

        assertEquals(2, males.size());
        assertTrue(males.containsAll(Arrays.asList(michal, adam)));

        assertEquals(1, females.size());
        assertTrue(females.contains(daniela));

        assertEquals(1, blokes.size());
        assertTrue(blokes.contains(adam));
    }

    @Test
    // the logic of this test is debatable. the domain model and persisted schema are not the same.
    public void shouldReadHierarchy5() {

        getDatabase().execute("CREATE (:Female {name:'Daniela'})," +
                "(:Male {name:'Michal'})," +
                "(:Bloke {name:'Adam'})");

        Female daniela = new Female("Daniela");
        Male michal = new Male("Michal");
        Bloke adam = new Bloke("Adam");

        Collection<Male> males = session.loadAll(Male.class);
        Collection<Female> females = session.loadAll(Female.class);
        Collection<Bloke> blokes = session.loadAll(Bloke.class);

        assertEquals(1, males.size());
        assertTrue(males.containsAll(Arrays.asList(michal)));

        assertEquals(1, females.size());
        assertTrue(females.contains(daniela));

        assertEquals(1, blokes.size());
        assertTrue(blokes.contains(adam));
    }

    @Test
    public void shouldNotReadHierarchy() {
        getDatabase().execute("CREATE (:Person {name:'Daniela'})");
        assertEquals(0, session.loadAll(Person.class).size());
    }

    @Test
    public void shouldLeaveExistingLabelsAlone() {
        getDatabase().execute("CREATE (:Female:Person:GoldMember {name:'Daniela'})");

        session.save(session.loadAll(Female.class).iterator().next());

        assertSameGraph(getDatabase(), "CREATE (:Female:Person:GoldMember {name:'Daniela'})");
    }

    //this should throw an exception, but for a different reason than it does now!
    @Test
    public void shouldFailWithConflictingHierarchies() {
        getDatabase().execute("CREATE (:Female:Person {name:'Daniela'})");

        SessionFactory sessionFactory = new SessionFactory(baseConfiguration, "org.neo4j.ogm.domain.hierarchy.domain", "org.neo4j.ogm.domain.hierarchy.conflicting");
        session = sessionFactory.openSession();

        assertEquals(0, session.loadAll(Female.class).size());
    }

    /**
     * @see DATAGRAPH-735
     */
    @Test
    public void shouldLoadRelatedSuperclasses() {
        getDatabase().execute("CREATE (f1:Female:Person {name:'f1'})," +
                "(m1:Male:Person {name:'m1'})," +
                "(c1:Female:Person {name:'c1'})," +
                "(b1:Bloke:Male:Person {name:'b1'})," +
                "(m1)-[:CHILD]->(c1)");

        Male m1 = session.loadAll(Male.class).iterator().next();
        assertNotNull(m1);
        assertEquals("m1", m1.getName());
        assertEquals("c1", m1.getChildren().iterator().next().getName());
    }
}
