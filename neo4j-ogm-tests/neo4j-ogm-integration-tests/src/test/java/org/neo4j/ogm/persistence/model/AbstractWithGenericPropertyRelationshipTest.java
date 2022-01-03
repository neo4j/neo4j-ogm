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
package org.neo4j.ogm.persistence.model;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.persistence.examples.stage.edges.LastDrama;
import org.neo4j.ogm.persistence.examples.stage.edges.PlayedInDrama;
import org.neo4j.ogm.persistence.examples.stage.nodes.Drama;
import org.neo4j.ogm.persistence.examples.stage.nodes.StageActor;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * Issue #54, ##66, #186 & #298 (Zendesk ticket XXX)
 *
 * @author Mihai Raulea
 * @author Mark Angrish
 * @author Vince Bickers
 */

public class AbstractWithGenericPropertyRelationshipTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.persistence.model",
            "org.neo4j.ogm.persistence.examples.stage");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void shouldReloadEntitiesJustSaved() {
        StageActor stageActor = new StageActor("first actor");
        Drama firstDrama = new Drama("malade imaginaire");
        Drama secondDrama = new Drama("Le cid");

        stageActor.dramas.add(new PlayedInDrama(stageActor, firstDrama, "rel1"));
        stageActor.dramas.add(new PlayedInDrama(stageActor, secondDrama, "rel2"));
        stageActor.lastDrama = new LastDrama(stageActor, secondDrama, "last");

        session.save(stageActor);

        session.clear();

        StageActor reloadedActor = session.load(StageActor.class, stageActor.id);

        assertThat(reloadedActor.dramas).hasSize(2);
        assertThat(reloadedActor.lastDrama).isNotNull();
    }

    /**
     * TODO:
     * This test is unclear to me in what it is expecting from the domain class setup. However, it should be possible
     * in theory to query by a generic type. Requires further investigation.
     */
    @Ignore
    @Test
    public void testQueryByGenericRelationshipType() {
        SomeNode<Integer> someNode = new SomeNode<>();
        session.save(someNode);

        SomeOtherNode someOtherNode = new SomeOtherNode();
        session.save(someOtherNode);

        RelationshipImplementation<Integer> relationshipImplementation = new RelationshipImplementation<>();
        relationshipImplementation.a = someNode;
        relationshipImplementation.b = someOtherNode;
        session.save(relationshipImplementation);

        AnotherRelationshipImplementation<Integer> anotherRelationshipImplementation = new AnotherRelationshipImplementation<>();
        anotherRelationshipImplementation.a = someNode;
        anotherRelationshipImplementation.b = someOtherNode;
        session.save(anotherRelationshipImplementation);

        // we can not query by abstract class
        Collection<L> relationship = session.loadAll(L.class);
        assertThat(relationship).hasSize(1);
    }

    @Test
    public void testGenericTypeInConcreteRelationship() {
        SomeNode<Integer> someNode = new SomeNode<>();
        session.save(someNode);

        SomeOtherNode someOtherNode = new SomeOtherNode();
        session.save(someOtherNode);

        RelationshipImplementation<Integer> relationshipImplementation = new RelationshipImplementation<>();
        relationshipImplementation.a = someNode;
        relationshipImplementation.b = someOtherNode;
        session.save(relationshipImplementation);

        AnotherRelationshipImplementation<String> anotherRelationshipImplementation = new AnotherRelationshipImplementation<>();
        anotherRelationshipImplementation.a = someNode;
        anotherRelationshipImplementation.b = someOtherNode;
        session.save(anotherRelationshipImplementation);

        AnotherRelationshipImplementation<String> retrieved = session
            .load(AnotherRelationshipImplementation.class, anotherRelationshipImplementation.id);
        assertThat(someNode.id).isEqualTo(retrieved.a.id);
    }

    @RelationshipEntity(type = "otherRelDynamicProperty")
    public class AnotherRelationshipImplementation<T> extends L<T> {

    }

    @RelationshipEntity(type = "relDynamicProperty")
    public class RelationshipImplementation<T> extends L<T> {

    }

    @RelationshipEntity(type = "abstractDynamicProperty")
    public abstract class L<T> {

        Long id;

        private T property;

        @StartNode
        public SomeNode a;

        @EndNode
        public SomeOtherNode b;
    }

    @NodeEntity
    public final class SomeNode<T> {

        Long id;

        @Relationship
        private List<L<T>> listOfLs;
    }

    @NodeEntity
    public final class SomeOtherNode {

        Long id;
    }
}
