/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.ogm.persistence.types;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.neo4j.ogm.testutil.GraphTestUtils.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;
import org.neo4j.ogm.domain.hierarchy.domain.custom_id.MostBasicEntity;
import org.neo4j.ogm.domain.hierarchy.domain.custom_id.RootEntity;
import org.neo4j.ogm.domain.hierarchy.domain.custom_id.SubEntity;
import org.neo4j.ogm.domain.hierarchy.domain.annotated.*;
import org.neo4j.ogm.domain.hierarchy.domain.custom_id.SubSubEntity;
import org.neo4j.ogm.domain.hierarchy.domain.people.Bloke;
import org.neo4j.ogm.domain.hierarchy.domain.people.Entity;
import org.neo4j.ogm.domain.hierarchy.domain.people.Female;
import org.neo4j.ogm.domain.hierarchy.domain.people.Male;
import org.neo4j.ogm.domain.hierarchy.domain.people.Person;
import org.neo4j.ogm.domain.hierarchy.domain.plain.*;
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

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.hierarchy.domain");
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void annotatedChildWithAnnotatedAbstractNamedParent() {
        session.save(new AnnotatedChildWithAnnotatedAbstractNamedParent());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:AnnotatedChildWithAnnotatedAbstractNamedParent:Parent)");

        assertThat(session.loadAll(AnnotatedChildWithAnnotatedAbstractNamedParent.class).iterator().next()).isNotNull();
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void annotatedChildWithAnnotatedNamedInterfaceParent() {
        session.save(new AnnotatedChildWithAnnotatedNamedInterfaceParent());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:AnnotatedChildWithAnnotatedNamedInterfaceParent:Parent)");

        assertThat(session.loadAll(AnnotatedChildWithAnnotatedNamedInterfaceParent.class).iterator().next())
            .isNotNull();
    }

    @Test
    public void annotatedChildWithAnnotatedAbstractParent() {
        session.save(new AnnotatedChildWithAnnotatedAbstractParent());

        assertSameGraph(getGraphDatabaseService(),
            "CREATE (:AnnotatedChildWithAnnotatedAbstractParent:AnnotatedAbstractParent)");

        assertThat(session.loadAll(AnnotatedChildWithAnnotatedAbstractParent.class).iterator().next()).isNotNull();
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void annotatedChildWithAnnotatedInterfaceParent() {
        session.save(new AnnotatedChildWithAnnotatedInterfaceParent());

        assertSameGraph(getGraphDatabaseService(),
            "CREATE (:AnnotatedChildWithAnnotatedInterfaceParent:AnnotatedInterface)");

        assertThat(session.loadAll(AnnotatedChildWithAnnotatedInterfaceParent.class).iterator().next()).isNotNull();
    }

    @Test
    public void annotatedChildWithAnnotatedConcreteNamedParent() {
        session.save(new AnnotatedChildWithAnnotatedConcreteNamedParent());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:AnnotatedChildWithAnnotatedConcreteNamedParent:Parent)");

        assertThat(session.loadAll(AnnotatedChildWithAnnotatedConcreteNamedParent.class).iterator().next()).isNotNull();
    }

    @Test
    public void annotatedChildWithAnnotatedConcreteParent() {
        session.save(new AnnotatedChildWithAnnotatedConcreteParent());

        assertSameGraph(getGraphDatabaseService(),
            "CREATE (:AnnotatedChildWithAnnotatedConcreteParent:AnnotatedConcreteParent)");

        assertThat(session.loadAll(AnnotatedChildWithAnnotatedConcreteParent.class).iterator().next()).isNotNull();
    }

    @Test
    public void annotatedChildWithPlainAbstractParent() {
        session.save(new AnnotatedChildWithPlainAbstractParent());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:AnnotatedChildWithPlainAbstractParent)");

        assertThat(session.loadAll(AnnotatedChildWithPlainAbstractParent.class).iterator().next()).isNotNull();
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void annotatedChildWithPlainInterfaceParent() {
        session.save(new AnnotatedChildWithPlainInterfaceParent());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:AnnotatedChildWithPlainInterfaceParent)");

        assertThat(session.loadAll(AnnotatedChildWithPlainInterfaceParent.class).iterator().next()).isNotNull();
    }

    @Test
    public void annotatedChildWithPlainConcreteParent() {
        session.save(new AnnotatedChildWithPlainConcreteParent());

        assertSameGraph(getGraphDatabaseService(),
            "CREATE (:AnnotatedChildWithPlainConcreteParent:PlainConcreteParent)");

        assertThat(session.loadAll(AnnotatedChildWithPlainConcreteParent.class).iterator().next()).isNotNull();
    }

    @Test
    @Ignore("class hierarchies are invalid for this test: multiple classes labelled 'Child' and 'Parent'")
    public void annotatedNamedChildWithAnnotatedAbstractNamedParent() {
        session.save(new AnnotatedNamedChildWithAnnotatedAbstractNamedParent());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:Child:Parent)");

        assertThat(session.loadAll(AnnotatedNamedChildWithAnnotatedAbstractNamedParent.class).iterator().next())
            .isNotNull();
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    @Ignore("class hierarchies are invalid for this test: multiple classes labelled 'Child' and 'Parent'")
    public void annotatedNamedChildWithAnnotatedNamedInterface() {
        session.save(new AnnotatedNamedChildWithAnnotatedNamedInterfaceParent());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:Child:Parent)");

        assertThat(session.loadAll(AnnotatedNamedChildWithAnnotatedNamedInterfaceParent.class).iterator().next())
            .isNotNull();
    }

    @Test
    //@Ignore("class hierarchies are invalid for this test: multiple classes labelled 'Child' and 'Parent'")
    public void annotatedNamedChildWithAnnotatedAbstractParent() {
        session.save(new AnnotatedNamedChildWithAnnotatedAbstractParent());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:Child:AnnotatedAbstractParent)");

        assertThat(session.loadAll(AnnotatedNamedChildWithAnnotatedAbstractParent.class).iterator().next()).isNotNull();
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    @Ignore("class hierarchies are invalid for this test: multiple classes labelled 'Child' and 'Parent'")
    public void annotatedNamedChildWithAnnotatedInterfaceParent() {
        session.save(new AnnotatedNamedChildWithAnnotatedInterfaceParent());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:Child:AnnotatedInterface)");

        assertThat(session.loadAll(AnnotatedNamedChildWithAnnotatedInterfaceParent.class).iterator().next())
            .isNotNull();
    }

    @Test
    @Ignore("class hierarchies are invalid for this test: multiple classes labelled 'Child' and 'Parent'")
    public void annotatedNamedChildWithAnnotatedConcreteNamedParent() {
        session.save(new AnnotatedNamedChildWithAnnotatedConcreteNamedParent());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:Child:Parent)");

        assertThat(session.loadAll(AnnotatedNamedChildWithAnnotatedConcreteNamedParent.class).iterator().next())
            .isNotNull();
    }

    @Test
    @Ignore("class hierarchies are invalid for this test: multiple classes labelled 'Child' and 'Parent'")
    public void annotatedNamedChildWithAnnotatedConcreteParent() {
        session.save(new AnnotatedNamedChildWithAnnotatedConcreteParent());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:Child:AnnotatedConcreteParent)");

        assertThat(session.loadAll(AnnotatedNamedChildWithAnnotatedConcreteParent.class).iterator().next()).isNotNull();
    }

    @Test
    @Ignore("class hierarchies are invalid for this test: multiple classes labelled 'Child' and 'Parent'")
    public void annotatedNamedChildWithPlainAbstractParent() {
        session.save(new AnnotatedNamedChildWithPlainAbstractParent());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:Child)");

        assertThat(session.loadAll(AnnotatedNamedChildWithPlainAbstractParent.class).iterator().next()).isNotNull();
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    @Ignore("class hierarchies are invalid for this test: multiple classes labelled 'Child' and 'Parent'")
    public void annotatedNamedChildWithPlainInterfaceParent() {
        session.save(new AnnotatedNamedChildWithPlainInterfaceParent());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:Child)");

        assertThat(session.loadAll(AnnotatedNamedChildWithPlainInterfaceParent.class).iterator().next()).isNotNull();
    }

    @Test
    @Ignore("class hierarchies are invalid for this test: multiple classes labelled 'Child' and 'Parent'")
    public void annotatedNamedChildWithPlainConcreteParent() {
        session.save(new AnnotatedNamedChildWithPlainConcreteParent());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:Child:PlainConcreteParent)");

        assertThat(session.loadAll(AnnotatedNamedChildWithPlainConcreteParent.class).iterator().next()).isNotNull();
    }

    @Test
    public void annotatedNamedSingleClass() {
        session.save(new AnnotatedNamedSingleClass());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:Single)");

        assertThat(session.loadAll(AnnotatedNamedSingleClass.class).iterator().next()).isNotNull();
    }

    @Test
    public void annotatedSingleClass() {
        session.save(new AnnotatedSingleClass());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:AnnotatedSingleClass)");

        assertThat(session.loadAll(AnnotatedSingleClass.class).iterator().next()).isNotNull();
    }

    @Test
    public void plainChildWithAnnotatedAbstractNamedParent() {
        session.save(new PlainChildWithAnnotatedAbstractNamedParent());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:PlainChildWithAnnotatedAbstractNamedParent:Parent)");

        assertThat(session.loadAll(PlainChildWithAnnotatedAbstractNamedParent.class).iterator().next()).isNotNull();
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void plainChildWithAnnotatedNamedInterfaceParent() {
        session.save(new PlainChildWithAnnotatedNamedInterfaceParent());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:PlainChildWithAnnotatedNamedInterfaceParent:Parent)");

        assertThat(session.loadAll(PlainChildWithAnnotatedNamedInterfaceParent.class).iterator().next()).isNotNull();
    }

    @Test
    public void plainChildWithAnnotatedAbstractParent() {
        session.save(new PlainChildWithAnnotatedAbstractParent());

        assertSameGraph(getGraphDatabaseService(),
            "CREATE (:PlainChildWithAnnotatedAbstractParent:AnnotatedAbstractParent)");

        assertThat(session.loadAll(PlainChildWithAnnotatedAbstractParent.class).iterator().next()).isNotNull();
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void plainChildWithAnnotatedInterfaceParent() {
        session.save(new PlainChildWithAnnotatedInterfaceParent());

        assertSameGraph(getGraphDatabaseService(),
            "CREATE (:PlainChildWithAnnotatedInterfaceParent:AnnotatedInterface)");

        assertThat(session.loadAll(PlainChildWithAnnotatedInterfaceParent.class).iterator().next()).isNotNull();
    }

    @Test
    public void plainChildWithAnnotatedConcreteNamedParent() {
        session.save(new PlainChildWithAnnotatedConcreteNamedParent());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:PlainChildWithAnnotatedConcreteNamedParent:Parent)");

        assertThat(session.loadAll(PlainChildWithAnnotatedConcreteNamedParent.class).iterator().next()).isNotNull();
    }

    @Test
    public void plainChildWithAnnotatedConcreteParent() {
        session.save(new PlainChildWithAnnotatedConcreteParent());

        assertSameGraph(getGraphDatabaseService(),
            "CREATE (:PlainChildWithAnnotatedConcreteParent:AnnotatedConcreteParent)");

        assertThat(session.loadAll(PlainChildWithAnnotatedConcreteParent.class).iterator().next()).isNotNull();
    }

    @Test
    public void plainChildWithPlainAbstractParent() {
        session.save(new PlainChildWithPlainAbstractParent());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:PlainChildWithPlainAbstractParent)");

        assertThat(session.loadAll(PlainChildWithPlainAbstractParent.class).iterator().next()).isNotNull();
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void plainChildWithPlainInterfaceParent() {
        session.save(new PlainChildWithPlainInterfaceParent());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:PlainChildWithPlainInterfaceParent)");

        assertThat(session.loadAll(PlainChildWithPlainInterfaceParent.class).iterator().next()).isNotNull();
    }

    @Test
    public void plainChildWithPlainConcreteParent() {
        session.save(new PlainChildWithPlainConcreteParent());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:PlainChildWithPlainConcreteParent:PlainConcreteParent)");

        assertThat(session.loadAll(PlainChildWithPlainConcreteParent.class).iterator().next()).isNotNull();
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void plainChildWithPlainConcreteParentImplementingInterface() {
        session.save(new PlainChildWithPlainConcreteParentImplementingInterface());

        assertSameGraph(getGraphDatabaseService(),
            "CREATE (:PlainChildWithPlainConcreteParentImplementingInterface:PlainConcreteParent)");

        assertThat(session.loadAll(PlainChildWithPlainConcreteParentImplementingInterface.class).iterator().next())
            .isNotNull();
        assertThat(session.loadAll(PlainConcreteParent.class).iterator().next()).isNotNull();
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void plainChildWithPlainInterfaceChild() {
        session.save(new PlainChildWithPlainInterfaceChild());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:PlainChildWithPlainInterfaceChild)");

        assertThat(session.loadAll(PlainChildWithPlainInterfaceChild.class).iterator().next()).isNotNull();
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

        assertSameGraph(getGraphDatabaseService(),
            "CREATE (:PlainChildWithAnnotatedSuperInterface:AnnotatedInterface)");
        assertThat(session.loadAll(PlainChildWithAnnotatedSuperInterface.class).iterator().next()).isNotNull();
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

        assertSameGraph(getGraphDatabaseService(),
            "CREATE (:AnnotatedChildWithAnnotatedInterface:AnnotatedInterfaceWithSingleImpl)");
        assertThat(session.loadAll(AnnotatedChildWithAnnotatedInterface.class).iterator().next()).isNotNull();
        assertThat(session.loadAll(AnnotatedInterfaceWithSingleImpl.class).iterator().next())
            .isNotNull(); //AnnotatedInterfaceWithSingleImpl has a single implementation so we should be able to load it
        assertThat(session.loadAll(AnnotatedInterfaceWithSingleImpl.class).iterator().next().getClass().getName())
            .isEqualTo("org.neo4j.ogm.domain.hierarchy.domain.annotated.AnnotatedChildWithAnnotatedInterface");
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

        assertSameGraph(getGraphDatabaseService(),
            "CREATE (:PlainChildWithAnnotatedConcreteSuperclass:PlainChildWithAnnotatedConcreteParent:AnnotatedConcreteParent)");
        assertThat(session.loadAll(PlainChildWithAnnotatedConcreteSuperclass.class).iterator().next()).isNotNull();
        assertThat(session.loadAll(PlainChildWithAnnotatedConcreteParent.class).iterator().next()).isNotNull();
        assertThat(session.loadAll(AnnotatedConcreteParent.class).iterator().next()).isNotNull();
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
        assertSameGraph(getGraphDatabaseService(),
            "CREATE (:PlainChildWithAbstractParentAndAnnotatedSuperclass:AnnotatedSingleClass)");
        assertThat(session.loadAll(PlainChildWithAbstractParentAndAnnotatedSuperclass.class).iterator().next())
            .isNotNull();
        //        assertThat(session.loadAll(PlainAbstractWithAnnotatedParent.class).iterator().next()).isNotNull();
        assertThat(session.loadAll(AnnotatedSingleClass.class).iterator().next()).isNotNull();
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void annotatedChildWithMultipleAnnotatedInterfaces() {
        session.save(new AnnotatedChildWithMultipleAnnotatedInterfaces());

        assertSameGraph(getGraphDatabaseService(),
            "CREATE (:AnnotatedChildWithMultipleAnnotatedInterfaces:AnnotatedInterface:Parent)");

        assertThat(session.loadAll(AnnotatedChildWithMultipleAnnotatedInterfaces.class).iterator().next()).isNotNull();
        assertThat(session.loadAll(AnnotatedInterface.class)).hasSize(1);
        assertThat(session.loadAll(AnnotatedNamedInterfaceParent.class)).hasSize(1);
    }

    @Test
    public void plainSingleClass() {
        session.save(new PlainSingleClass());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:PlainSingleClass)");

        assertThat(session.loadAll(PlainSingleClass.class).iterator().next()).isNotNull();
    }

    @Test
    public void plainChildOfTransientParent() {
        assertThatThrownBy(() -> session.save(new PlainChildOfTransientParent()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is not a valid entity class");

        try (Transaction tx = getGraphDatabaseService().beginTx()) {
            assertThat(GraphTestUtils.allNodes(getGraphDatabaseService()).iterator().hasNext()).isFalse();
            tx.success();
        }
    }

    /**
     * @see DATAGRAPH-577
     */
    @Test
    public void plainChildOfTransientInterface() {
        assertThatThrownBy(() -> session.save(new PlainChildOfTransientInterface()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is not a valid entity class");

        try (Transaction tx = getGraphDatabaseService().beginTx()) {
            assertThat(GraphTestUtils.allNodes(getGraphDatabaseService()).iterator().hasNext()).isFalse();
            tx.success();
        }
    }

    @Test
    public void transientChildWithPlainConcreteParent() {
        assertThatThrownBy(() -> session.save(new TransientChildWithPlainConcreteParent()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is not a valid entity class");


        try (Transaction tx = getGraphDatabaseService().beginTx()) {
            assertThat(GraphTestUtils.allNodes(getGraphDatabaseService()).iterator().hasNext()).isFalse();
            tx.success();
        }
    }

    @Test
    public void transientSingleClass() {
        assertThatThrownBy(() -> session.save(new TransientSingleClass()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is not a valid entity class");
    }

    @Test
    public void transientSingleClassWithId() {
        assertThatThrownBy(() -> session.save(new TransientSingleClassWithId()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is not a valid entity class");

        try (Transaction tx = getGraphDatabaseService().beginTx()) {
            assertThat(GraphTestUtils.allNodes(getGraphDatabaseService()).iterator().hasNext()).isFalse();
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

        assertSameGraph(getGraphDatabaseService(), "CREATE (:PlainClassWithTransientFields)");

        assertThat(session.loadAll(PlainClassWithTransientFields.class).iterator().next()).isNotNull();
    }

    @Test
    public void shouldNotBeAbleToLoadClassOfWrongType() {
        session.save(new AnnotatedNamedSingleClass());
        assertThat(session.loadAll(PlainSingleClass.class).iterator().hasNext()).isFalse();
    }

    @Test
    public void shouldSaveHierarchy() {
        session.save(new Female("Daniela"));
        session.save(new Male("Michal"));
        session.save(new Bloke("Adam"));

        assertSameGraph(getGraphDatabaseService(), "CREATE (:Female:Person {name:'Daniela'})," +
            "(:Male:Person {name:'Michal'})," +
            "(:Bloke:Male:Person {name:'Adam'})");
    }

    @Test
    public void shouldSaveHierarchy2() {
        session.save(Arrays.asList(new Female("Daniela"), new Male("Michal"), new Bloke("Adam")));

        assertSameGraph(getGraphDatabaseService(), "CREATE (:Female:Person {name:'Daniela'})," +
            "(:Male:Person {name:'Michal'})," +
            "(:Bloke:Male:Person {name:'Adam'})");
    }

    @Test
    public void shouldReadHierarchyAndRetrieveBySuperclass() {

        Female daniela = new Female("Daniela");
        Male michal = new Male("Michal");
        Bloke adam = new Bloke("Adam");

        session.save(Arrays.asList(daniela, michal, adam));
        session.query("CREATE (:Test)", emptyMap());

        Collection<Entity> entities = session.loadAll(Entity.class);

        Collection<Person> people = session.loadAll(Person.class);
        Collection<Male> males = session.loadAll(Male.class);
        Collection<Female> females = session.loadAll(Female.class);
        Collection<Bloke> blokes = session.loadAll(Bloke.class);

        assertThat(entities.isEmpty()).as("Shouldn't be able to load by non-annotated, abstract classes").isTrue();

        assertThat(people).hasSize(3);
        assertThat(session.countEntitiesOfType(Person.class)).isEqualTo(people.size());
        assertThat(people.containsAll(Arrays.asList(daniela, michal, adam))).isTrue();

        assertThat(males).hasSize(2);
        assertThat(males.containsAll(Arrays.asList(michal, adam))).isTrue();

        assertThat(females).hasSize(1);
        assertThat(session.countEntitiesOfType(Female.class)).isEqualTo(females.size());
        assertThat(females.contains(daniela)).isTrue();

        assertThat(blokes).hasSize(1);
        assertThat(blokes.contains(adam)).isTrue();
    }

    @Test
    public void shouldNotCountAbstractNonAnnotatedClass() {

        Female daniela = new Female("Daniela");
        Male michal = new Male("Michal");
        Bloke adam = new Bloke("Adam");

        session.save(Arrays.asList(daniela, michal, adam));
        session.query("CREATE (:Test)", emptyMap());

        assertThat(session.countEntitiesOfType(Entity.class))
            .as("Shouldn't be able to count by non-annotated, abstract classes").isEqualTo(0);

        assertThat(session.countEntitiesOfType(Person.class)).isEqualTo(3);
        assertThat(session.countEntitiesOfType(Male.class)).isEqualTo(2);
        assertThat(session.countEntitiesOfType(Female.class)).isEqualTo(1);
        assertThat(session.countEntitiesOfType(Bloke.class)).isEqualTo(1);
    }

    @Test
    public void shouldNotDeleteAbstractNonAnnotatedClass() {

        Female daniela = new Female("Daniela");
        Male michal = new Male("Michal");
        Bloke adam = new Bloke("Adam");

        session.save(Arrays.asList(daniela, michal, adam));
        session.query("CREATE (:Test)", emptyMap());

        session.deleteAll(Entity.class);
        assertThat(session.countEntitiesOfType(Person.class)).isEqualTo(3);
        assertThat(session.query("MATCH (t:Test) RETURN t", emptyMap())).hasSize(1);
    }

    @Test
    public void shouldReadHierarchy2() {

        getGraphDatabaseService().execute("CREATE (:Female:Person:Entity {name:'Daniela'})," +
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

        // to be consistent with the fact that non annotated abstract classes are not managed,
        // this should not be loaded even if the nodes have the Entity label.
        // see #404
        assertThat(entities).isEmpty();

        assertThat(people).hasSize(3);
        assertThat(people.containsAll(Arrays.asList(daniela, michal, adam))).isTrue();

        assertThat(males).hasSize(2);
        assertThat(males.containsAll(Arrays.asList(michal, adam))).isTrue();

        assertThat(females).hasSize(1);
        assertThat(females.contains(daniela)).isTrue();

        assertThat(blokes).hasSize(1);
        assertThat(blokes.contains(adam)).isTrue();
    }

    @Test
    public void shouldReadHierarchy3() {
        getGraphDatabaseService().execute("CREATE (:Female:Person {name:'Daniela'})," +
            "(:Male:Person {name:'Michal'})," +
            "(:Bloke:Male:Person {name:'Adam'})");

        Female daniela = new Female("Daniela");
        Male michal = new Male("Michal");
        Bloke adam = new Bloke("Adam");

        Collection<Person> people = session.loadAll(Person.class);
        Collection<Male> males = session.loadAll(Male.class);
        Collection<Female> females = session.loadAll(Female.class);
        Collection<Bloke> blokes = session.loadAll(Bloke.class);

        assertThat(people).hasSize(3);
        assertThat(people.containsAll(Arrays.asList(daniela, michal, adam))).isTrue();

        assertThat(males).hasSize(2);
        assertThat(males.containsAll(Arrays.asList(michal, adam))).isTrue();

        assertThat(females).hasSize(1);
        assertThat(females.contains(daniela)).isTrue();

        assertThat(blokes).hasSize(1);
        assertThat(blokes.contains(adam)).isTrue();
    }

    @Test
    public void shouldReadHierarchy4() {
        getGraphDatabaseService().execute("CREATE (:Female {name:'Daniela'})," +
            "(:Male {name:'Michal'})," +
            "(:Bloke:Male {name:'Adam'})");

        Female daniela = new Female("Daniela");
        Male michal = new Male("Michal");
        Bloke adam = new Bloke("Adam");

        Collection<Male> males = session.loadAll(Male.class);
        Collection<Female> females = session.loadAll(Female.class);
        Collection<Bloke> blokes = session.loadAll(Bloke.class);

        assertThat(males).hasSize(2);
        assertThat(males.containsAll(Arrays.asList(michal, adam))).isTrue();

        assertThat(females).hasSize(1);
        assertThat(females.contains(daniela)).isTrue();

        assertThat(blokes).hasSize(1);
        assertThat(blokes.contains(adam)).isTrue();
    }

    @Test
    // the logic of this test is debatable. the domain model and persisted schema are not the same.
    public void shouldReadHierarchy5() {

        getGraphDatabaseService().execute("CREATE (:Female {name:'Daniela'})," +
            "(:Male {name:'Michal'})," +
            "(:Bloke {name:'Adam'})");

        Female daniela = new Female("Daniela");
        Male michal = new Male("Michal");
        Bloke adam = new Bloke("Adam");

        Collection<Male> males = session.loadAll(Male.class);
        Collection<Female> females = session.loadAll(Female.class);
        Collection<Bloke> blokes = session.loadAll(Bloke.class);

        assertThat(males).hasSize(1);
        assertThat(males.containsAll(Arrays.asList(michal))).isTrue();

        assertThat(females).hasSize(1);
        assertThat(females.contains(daniela)).isTrue();

        assertThat(blokes).hasSize(1);
        assertThat(blokes.contains(adam)).isTrue();
    }

    @Test
    public void shouldNotReadHierarchy() {
        getGraphDatabaseService().execute("CREATE (:Person {name:'Daniela'})");
        assertThat(session.loadAll(Person.class)).isEmpty();
    }

    @Test
    public void shouldLeaveExistingLabelsAlone() {
        getGraphDatabaseService().execute("CREATE (:Female:Person:GoldMember {name:'Daniela'})");

        session.save(session.loadAll(Female.class).iterator().next());

        assertSameGraph(getGraphDatabaseService(), "CREATE (:Female:Person:GoldMember {name:'Daniela'})");
    }

    //this should throw an exception, but for a different reason than it does now!
    @Test
    public void shouldFailWithConflictingHierarchies() {
        getGraphDatabaseService().execute("CREATE (:Female:Person {name:'Daniela'})");

        SessionFactory sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.hierarchy.domain",
            "org.neo4j.ogm.domain.hierarchy.conflicting");
        session = sessionFactory.openSession();

        assertThat(session.loadAll(Female.class)).isEmpty();
    }

    /**
     * @see DATAGRAPH-735
     */
    @Test
    public void shouldLoadRelatedSuperclasses() {
        getGraphDatabaseService().execute("CREATE (f1:Female:Person {name:'f1'})," +
            "(m1:Male:Person {name:'m1'})," +
            "(c1:Female:Person {name:'c1'})," +
            "(m1)-[:CHILD]->(c1)");

        Male m1 = session.loadAll(Male.class).iterator().next();
        assertThat(m1).isNotNull();
        assertThat(m1.getName()).isEqualTo("m1");
        assertThat(m1.getChildren().iterator().next().getName()).isEqualTo("c1");
    }

    /**
     * #553
     */
    @Test
    public void shouldLoadImplementationWhenParentClassIsQueriedDirectSubclass() {
        UUID uuid = UUID.randomUUID();
        SubEntity subEntity = new SubEntity();
        subEntity.setMyId(uuid);
        subEntity.setName("test");

        session.save(subEntity);
        session.clear();

        RootEntity rootEntity = session.load(RootEntity.class, uuid);

        assertThat(rootEntity).isNotNull();
    }

    /**
     * #553
     */
    @Test
    public void shouldLoadImplementationWhenParentClassIsQueriedDeepSubclass() {
        UUID uuid = UUID.randomUUID();
        SubSubEntity subsubEntity = new SubSubEntity();
        subsubEntity.setMyId(uuid);
        subsubEntity.setName("test");

        session.save(subsubEntity);
        session.clear();

        RootEntity rootEntity = session.load(RootEntity.class, uuid);

        assertThat(rootEntity).isNotNull();
    }

    /**
     * #553
     */
    @Test
    public void shouldLoadImplementationWhenParentClassIsQueriedDeepSubclasWithsMostBasicEntity() {
        UUID uuid = UUID.randomUUID();
        SubSubEntity subsubEntity = new SubSubEntity();
        subsubEntity.setMyId(uuid);
        subsubEntity.setName("test");

        session.save(subsubEntity);
        session.clear();

        MostBasicEntity rootEntity = session.load(MostBasicEntity.class, uuid);

        assertThat(rootEntity).isNull();
    }

    /**
     * #553
     */
    @Test
    public void shouldLoadImplementationWhenParentClassIsQueriedLoadAll() {
        UUID uuid = UUID.randomUUID();
        SubSubEntity subsubEntity = new SubSubEntity();
        subsubEntity.setMyId(uuid);
        subsubEntity.setName("test");

        session.save(subsubEntity);
        session.clear();

        Collection<RootEntity> rootEntity = session.loadAll(RootEntity.class);

        assertThat(rootEntity).isNotEmpty();
    }

    /**
     * #553
     */
    @Test
    public void shouldLoadImplementationWhenParentClassIsQueriedLoadAllWithAbstractNonAnnotatedBaseClass() {
        UUID uuid = UUID.randomUUID();
        SubSubEntity subsubEntity = new SubSubEntity();
        subsubEntity.setMyId(uuid);
        subsubEntity.setName("test");

        session.save(subsubEntity);
        session.clear();

        Collection<MostBasicEntity> rootEntity = session.loadAll(MostBasicEntity.class);

        assertThat(rootEntity).isEmpty();
    }

}
