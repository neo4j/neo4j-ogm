/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author vince
 */
public class SimpleNetworkIntegrationTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.simpleNetwork");
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

