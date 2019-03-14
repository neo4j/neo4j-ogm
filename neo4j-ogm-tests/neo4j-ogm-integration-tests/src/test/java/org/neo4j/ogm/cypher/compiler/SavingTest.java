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
package org.neo4j.ogm.cypher.compiler;

import static org.assertj.core.api.Assertions.*;

import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.domain.gh613.Label;
import org.neo4j.ogm.domain.gh613.Node;
import org.neo4j.ogm.domain.gh613.NodeType;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.testutil.TestUtils;

/**
 * @author Andreas Berger
 * @author Michael J. Simons
 */
public class SavingTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void initSesssionFactory() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.gh613");
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();
        session.purgeDatabase();
        session.clear();

        session.query(TestUtils.readCQLFile("org/neo4j/ogm/cql/gh613.cql").toString(), Collections.emptyMap());
    }

    @Test // GH-613
    public void testSaveParentAfterChild() {

        Node loc1_1 = queryNode("loc1_1");
        assertThat(loc1_1.getNodeType()).isNotNull();
        loc1_1.setLabels(null);
        session.save(loc1_1);

        Node loc1 = queryNode("loc1");
        assertThat(loc1.getChildNodes()).hasSize(3)
            .first().extracting(Node::getNodeType).isNull(); // The thing hasn't been initialized on purpose, see below
        assertThat(loc1.getNodeType()).isNotNull();
        session.save(loc1);

        loc1_1 = queryNode("loc1_1");
        assertThat(loc1_1.getNodeType()).isNotNull();
    }

    @Test
    public void testChangeParent() {

        Node loc2 = queryNode("loc2");
        Node loc1_1 = queryNode("loc1_1");
        Node loc1_2 = queryNode("loc1_2");

        Node oldParentOfLoc1_1 = loc1_1.setChildOfBidirectional(loc2);
        session.save(loc1_1);
        session.save(oldParentOfLoc1_1);
        Node oldParentOfLoc1_2 = loc1_2.setChildOfBidirectional(loc2);
        session.save(loc1_2);
        session.save(oldParentOfLoc1_2);

        Node loc1 = queryNode("loc1");
        assertThat(loc1.getChildNodes()).hasSize(1);

        loc2 = queryNode("loc2");
        assertThat(loc2.getChildNodes()).hasSize(2);
    }

    @Test
    public void indirectReachableStaleRelationshipsShouldBeCleared() {
        Node loc1 = queryNode("loc1");
        assertThat(loc1.getNodeType()).isNotNull();

        Node loc1_1 = queryNode("loc1_1");
        assertThat(loc1_1.getLabels()).hasSize(1);

        loc1_1.setLabels(null);
        session.save(loc1_1);

        Node root = queryNode("root");
        assertThat(root.getChildNodes()).hasSize(2);
        session.save(root);

        loc1 = queryNode("loc1");
        assertThat(loc1.getNodeType()).isNotNull();
    }

    private Node queryNode(String nodeId) {
        Collection<Node> nodeTypes = session
            .loadAll(Node.class, new Filters(new Filter("nodeId", ComparisonOperator.EQUALS, nodeId)), 1);
        assertThat(nodeTypes).hasSize(1);
        return nodeTypes.iterator().next();
    }
}
