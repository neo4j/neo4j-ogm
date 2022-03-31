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
package org.neo4j.ogm.persistence.identity;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.neo4j.ogm.annotation.Relationship.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * These tests relate to the concept of node and relationship identity in the OGM. Identity
 * should be independent of any notion of equality as described by the equals or hashcode
 * methods on Java objects.
 *
 * @author Vince Bickers
 * @author Michael J. Simons
 */
public class IdentityTest extends TestContainersTestBase {

    private static final Logger logger = LoggerFactory.getLogger(IdentityTest.class);

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.persistence.identity");
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();
    }

    @After
    public void cleanup() {
        session.purgeDatabase();
    }

    @Test
    public void shouldCreateRelationshipEntityWhenDifferentStartAndEndNodesAreHashCodeEqual() {

        Node start = new Node();
        Node end = new Node();

        // user code deliberately sets the nodes to be equal
        assertThat(end).isEqualTo(start);
        Set<Node> nodes = new HashSet<>();
        nodes.add(start);
        nodes.add(end);

        // same hashcode, so a single object, not two in set
        assertThat(nodes).hasSize(1);

        session.save(start);
        session.save(end);

        Edge edge = new Edge();

        edge.start = start;
        edge.end = end;

        start.link = edge;

        session.save(edge);

        session.clear();

        Node checkNode = session.load(Node.class, start.id);

        assertThat(checkNode.link).isNotNull();
        assertThat(checkNode.link.start.id).isEqualTo(start.id);
        assertThat(checkNode.link.end.id).isEqualTo(end.id);
    }

    @Test
    public void shouldBeAbleToLoadAllNodesOfATypeEvenIfTheyAreConsideredEqual() {
        Node nodeA = new Node();
        session.save(nodeA);

        Node nodeB = new Node();
        session.save(nodeB);

        session.clear();
        Collection<Node> allNodes = session.loadAll(Node.class);
        assertThat(allNodes).hasSize(2);
    }

    @Test
    public void shouldBeAbleToLoadAllREsEvenIfTheyAreConsideredEqual() {
        Node nodeA = new Node();
        session.save(nodeA);

        Node nodeB = new Node();
        session.save(nodeB);

        Node nodeC = new Node();
        session.save(nodeC);

        Edge edge1 = new Edge();
        edge1.start = nodeA;
        edge1.end = nodeB;
        session.save(edge1);

        Edge edge2 = new Edge();
        edge2.start = nodeB;
        edge2.end = nodeC;
        session.save(edge2);

        session.clear();
        Collection<Edge> allEdges = session.loadAll(Edge.class);
        assertThat(allEdges).hasSize(2);
    }

    @Test
    public void shouldBeAbleToLoadAllRelatedNodesIfTheyAreConsideredEqual() throws Exception {
        Node nodeA = new Node();

        Node nodeB = new Node();

        Node nodeC = new Node();

        nodeA.related = Arrays.asList(nodeB, nodeC);

        session.save(nodeA);
        logger.info("related: {}", nodeA.related);

        session.clear();

        Node loadedA = session.load(Node.class, nodeA.id);
        logger.info("related: {}", loadedA.related);
        assertThat(loadedA.related).hasSize(2);
    }

    @Test
    public void indistinguishableRelationshipsMapAsSingleRelatedEntityInstance() {

        Map<String, Object> ids = session.query("CREATE "
            + "(n1:NODE), (n2:NODE),"
            + "(n1)-[:RELATED]->(n2),"
            + "(n1)-[:RELATED]->(n2)"
            + "RETURN id(n1) AS id1, id(n2) AS id2", emptyMap()).queryResults().iterator().next();

        Node node = session.load(Node.class, (Long) ids.get("id1"));
        assertThat(node.related).hasSize(1);
    }

    @Test
    public void indistinguishableEntityInstancesMapAsSingleRelationship() {
        Node nodeA = new Node();
        Node nodeB = new Node();

        nodeA.related = Arrays.asList(nodeB, nodeB);

        session.save(nodeA);

        session.clear();
        assertThat(session.query("MATCH (n1:NODE) -[:RELATED]-> (n2:NODE) RETURN n1, n2", emptyMap())
            .queryResults()).hasSize(1);
    }

    @NodeEntity(label = "NODE")
    public static class Node {

        Long id;

        @Relationship(type = "EDGE")
        Edge link;

        @Relationship(type = "RELATED", direction = Direction.OUTGOING)
        List<Node> related;

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }

            return !(o == null || getClass() != o.getClass());
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public String toString() {
            return "Node{" +
                "id=" + id +
                '}';
        }
    }

    @RelationshipEntity(type = "EDGE")
    public static class Edge {

        Long id;

        @StartNode
        Node start;

        @EndNode
        Node end;

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }

            return !(o == null || getClass() != o.getClass());
        }

        @Override
        public int hashCode() {
            return 1;
        }
    }
}
