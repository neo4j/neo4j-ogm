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
package org.neo4j.ogm.persistence.examples.simpleNetwork;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.simpleNetwork.classes.IdentityNode;
import org.neo4j.ogm.domain.simpleNetwork.classes.StateNode;
import org.neo4j.ogm.domain.simpleNetwork.classes.TimeRelation;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author vince
 */
public class SimpleNetworkIntegrationTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.simpleNetwork");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    /**
     * @see issue #42
     */
    @Test
    public void shouldReadIdentityNodeAlongWithStates() {
        // arrange

        // an identity
        IdentityNode identityNode = new IdentityNode();

        // the first state node
        StateNode stateNode1 = new StateNode();
        stateNode1.setName("Good graph databases");
        stateNode1.setDescription("We sell World's Leading Graph Database");

        // the second state node
        StateNode stateNode2 = new StateNode();
        stateNode2.setName("Great graph databases");
        stateNode2.setDescription("We sell World's Leading Graph Database");

        // create a new state relationship
        TimeRelation identityState1 = new TimeRelation();
        identityState1.setSourceNode(identityNode);
        identityState1.setTargetNode(stateNode1);
        // user code does not do this, but it probably ought to
        //stateNode1.setIdentityState(identityState1);

        // create a second state relationship
        TimeRelation identityState2 = new TimeRelation();
        identityState2.setSourceNode(identityNode);
        identityState2.setTargetNode(stateNode2);
        // user code does not do this, but it probably ought to
        //stateNode2.setIdentityState(identityState2);

        // add the identityStates to the identityNode
        Set<TimeRelation> identityStates = new HashSet<>();
        identityStates.add(identityState1);
        identityStates.add(identityState2);
        identityNode.setStates(identityStates);

        // save
        session.save(identityNode);
        session.clear();

        // reload
        IdentityNode loadedIdentityNode = session.load(IdentityNode.class, identityNode.getId());

        // assert
        assertThat(loadedIdentityNode.getStates()).hasSize(2);
    }
}

