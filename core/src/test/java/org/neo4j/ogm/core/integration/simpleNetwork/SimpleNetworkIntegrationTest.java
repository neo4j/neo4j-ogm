package org.neo4j.ogm.core.integration.simpleNetwork;

import org.junit.*;
import org.junit.rules.TestRule;
import org.neo4j.ogm.api.driver.Driver;
import org.neo4j.ogm.core.session.Session;
import org.neo4j.ogm.core.session.SessionFactory;
import org.neo4j.ogm.api.service.Components;
import org.neo4j.ogm.core.testutil.IntegrationTestRule;
import org.neo4j.ogm.domain.simpleNetwork.classes.IdentityNode;
import org.neo4j.ogm.domain.simpleNetwork.classes.StateNode;
import org.neo4j.ogm.domain.simpleNetwork.classes.TimeRelation;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author vince
 */
public class SimpleNetworkIntegrationTest {

    private static final Driver driver = Components.driver();

    @ClassRule
    public static final TestRule server = new IntegrationTestRule(driver);

    private Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory("org.neo4j.ogm.domain.simpleNetwork").openSession(driver);
    }

    @After
    public void teardown() {
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
        Assert.assertEquals(2, loadedIdentityNode.getStates().size());

    }
}

