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
package org.neo4j.ogm.persistence.model;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Result;
import org.neo4j.ogm.domain.election.Candidate;
import org.neo4j.ogm.domain.election.Voter;
import org.neo4j.ogm.domain.gh640.MyNode;
import org.neo4j.ogm.domain.gh640.MyNodeWithAssignedId;
import org.neo4j.ogm.domain.gh641.Entity1;
import org.neo4j.ogm.domain.gh641.MyRelationship;
import org.neo4j.ogm.domain.policy.Person;
import org.neo4j.ogm.domain.policy.Policy;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.GraphTestUtils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Mark Angrish
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class RelationshipMappingTest extends MultiDriverTestClass {

    private Session session;

    @BeforeClass
    public static void oneTimeSetup() {
        sessionFactory = new SessionFactory(driver,
            "org.neo4j.ogm.domain.policy",
            "org.neo4j.ogm.domain.election",
            "org.neo4j.ogm.domain.gh640",
            "org.neo4j.ogm.domain.gh641");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void testThatABiDirectionalMappingIsEstablishedWhenAMutualRelationshipWithNoAnnotationsIsSaved() {

        Person jim = new Person("Jim");
        Policy policy = new Policy("Health");

        // create a mutual relationship
        policy.getInfluencers().add(jim);
        jim.getInfluenced().add(policy);

        // expect both objects to have a relationship to each other
        session.save(policy);
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (n:Policy:DomainObject {name:'Health'})-[:INFLUENCERS]->(m:Person:DomainObject {name:'Jim'})-[:INFLUENCED]->(n)");
    }

    @Test
    public void testThatAnAnnotatedRelationshipOnTwoObjectsThatIsSavedFromTheOutgoingCreatesTheCorrectRelationshipInTheGraph() {

        Person jim = new Person("Jim");
        Policy policy = new Policy("Health");

        jim.getWritten().add(policy);

        session.save(jim);
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (n:Policy:DomainObject {name:'Health'})<-[:WRITES_POLICY]-(m:Person:DomainObject {name:'Jim'})");
    }

    @Test
    public void testThatAnAnnotatedRelationshipSavedFromTheIncomingSideCreatesTheCorrectRelationshipInTheGraph() {

        Person jim = new Person("Jim");
        Policy policy = new Policy("Health");

        policy.getWriters().add(jim);

        // we a single relationship, outgoing from person to policy to be established
        session.save(policy);
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (n:Policy:DomainObject {name:'Health'})<-[:WRITES_POLICY]-(m:Person:DomainObject {name:'Jim'})");
    }

    @Test
    public void testPersistAnnotatedSingleRelationshipMappingBothDomainObjectsParticipating() {

        Person jim = new Person("Jim");
        Policy policy = new Policy("Health");

        // establish the relationship in both directions in the domain model
        policy.getWriters().add(jim);
        jim.getWritten().add(policy);

        // verify we create only one directed relationship in the graph
        session.save(policy);
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (n:Policy:DomainObject {name:'Health'})<-[:WRITES_POLICY]-(m:Person:DomainObject {name:'Jim'})");
    }

    @Test // DATAGRAPH-674
    public void testAnnotatedRelationshipTypeWhenMethodsAreJsonIgnored() {
        Person jim = new Person("Jim");
        Policy policy = new Policy("Health");

        policy.setAuthorized(jim);
        jim.setAuthorized(policy);

        session.save(jim);
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (n:Policy:DomainObject {name:'Health'})<-[:AUTHORIZED_POLICY]-(m:Person:DomainObject {name:'Jim'})");
    }

    @Test
    public void shouldAllowVoterToChangeHerMind() {

        // create the graph
        Result executionResult = getGraphDatabaseService().execute(
            "CREATE " +
                "(a:Voter:Candidate {name:'A'}), " +
                "(b:Voter:Candidate {name:'B'}), " +
                "(v:Voter {name:'V'})-[:CANDIDATE_VOTED_FOR]->(b) " +
                "RETURN id(a) AS a_id, id(b) AS b_id, id(v) AS v_id");

        // build the object map
        Map<String, ?> results = executionResult.next();

        Long aid = (Long) results.get("a_id");
        Long bid = (Long) results.get("b_id");
        Long vid = (Long) results.get("v_id");

        Candidate a = session.load(Candidate.class, aid);

        Candidate b = session.load(Candidate.class, bid);

        Voter v = session.load(Voter.class, vid);

        // voter changes her mind
        v.candidateVotedFor = a;
        session.save(v);
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (a:Voter:Candidate {name:'A'}) " +
                "CREATE (b:Voter:Candidate {name:'B'}) " +
                "CREATE (v:Voter {name:'V'})-[:CANDIDATE_VOTED_FOR]->(a)");
    }

    @Test // GH-640
    public void shouldDealWithTheSameButNotEqualParentEntities() {

        Session tx = sessionFactory.openSession();
        Map<String, Object> result = tx.query("CREATE (n1:MyNode {name: 'node1'})\n"
            + "CREATE (n2:MyNode {name: 'node2'})\n"
            + "CREATE (n3:MyNode {name: 'node3'})\n"
            + "CREATE (n1) - [:REL_TWO] -> (n2)\n"
            + "CREATE (n2) - [:REL_ONE] -> (n1)\n"
            + "RETURN id(n1) as idOfn1, id(n2) as idOfn2, id(n3) as idOfn3", Collections.emptyMap()).iterator().next();

        // Lets flush the session and thus basically creating a new tx, at least as far as the cache is concerned
        tx = sessionFactory.openSession();

        // Let's go through a bunch of queries to make sure the associations are loaded as OGM would do by default…
        MyNode node1 = tx.load(MyNode.class, (Long)result.get("idOfn1"));
        MyNode node2 = tx.load(MyNode.class, (Long)result.get("idOfn2"));
        MyNode node3 = tx.load(MyNode.class, (Long)result.get("idOfn3"));

        // Let's check some preconditions, shall we?
        assertThat(node1).isNotNull();
        assertThat(node2).isNotNull();
        assertThat(node3).isNotNull();

        assertThat(node1.getRefOne()).isEqualTo(node2);
        assertThat(node1.getRefTwo()).containsOnly(node2);

        // We start a new tx, but keep working on the copy of the previously loaded node
        tx = sessionFactory.openSession();
        MyNode changed = tx.load(MyNode.class, node1.getId()).copy();
        changed.setName("Dirty thing.");
        changed.setRefOne(node3);
        tx.save(changed);

        // Again, verify in a new session.
        tx = sessionFactory.openSession();
        node1 = tx.load(MyNode.class, changed.getId());
        assertThat(node1.getRefOne()).isEqualTo(node3);
        assertThat(node1.getRefTwo()).containsOnly(node2);

        // Better safe than sorry.
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (n1:MyNode {name: 'Dirty thing.'})\n"
                + "CREATE (n2:MyNode {name: 'node2'})\n"
                + "CREATE (n3:MyNode {name: 'node3'})\n"
                + "CREATE (n1) - [:REL_TWO] -> (n2)\n"
                + "CREATE (n3) - [:REL_ONE] -> (n1)");
    }

    @Test // GH-640
    public void shouldDealWithTheSameButNotEqualParentEntitiesWithAssignedId() {
        // Sames as above but this time the entity has an assigned id.

        Session tx = sessionFactory.openSession();
        tx.query("CREATE (n1:MyNodeWithAssignedId {name: 'node1'})\n"
            + "CREATE (n2:MyNodeWithAssignedId {name: 'node2'})\n"
            + "CREATE (n3:MyNodeWithAssignedId {name: 'node3'})\n"
            + "CREATE (n1) - [:REL_TWO] -> (n2)\n"
            + "CREATE (n2) - [:REL_ONE] -> (n1)\n"
            + "RETURN id(n1) as idOfn1, id(n2) as idOfn2, id(n3) as idOfn3", Collections.emptyMap()).iterator().next();

        tx = sessionFactory.openSession();

        MyNodeWithAssignedId node1 = tx.load(MyNodeWithAssignedId.class, "node1");
        MyNodeWithAssignedId node2 = tx.load(MyNodeWithAssignedId.class, "node2");
        MyNodeWithAssignedId node3 = tx.load(MyNodeWithAssignedId.class, "node3");

        assertThat(node1).isNotNull();
        assertThat(node2).isNotNull();
        assertThat(node3).isNotNull();

        assertThat(node1.getRefOne()).isEqualTo(node2);
        assertThat(node1.getRefTwo()).containsOnly(node2);

        tx = sessionFactory.openSession();
        MyNodeWithAssignedId changed = tx.load(MyNodeWithAssignedId.class, node1.getName()).copy();
        changed.setRefOne(node3);
        tx.save(changed);

        tx = sessionFactory.openSession();
        node1 = tx.load(MyNodeWithAssignedId.class, changed.getName());
        assertThat(node1.getRefOne()).isEqualTo(node3);
        assertThat(node1.getRefTwo()).containsOnly(node2);

        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
            "CREATE (n1:MyNodeWithAssignedId {name: 'node1'})\n"
                + "CREATE (n2:MyNodeWithAssignedId {name: 'node2'})\n"
                + "CREATE (n3:MyNodeWithAssignedId {name: 'node3'})\n"
                + "CREATE (n1) - [:REL_TWO] -> (n2)\n"
                + "CREATE (n3) - [:REL_ONE] -> (n1)");
    }

    @Test // GH-641
    public void shouldKeepOrderOfRelatedElements() {
        // This test doesn't fit too well into here, as it is a broader problem than relationships,
        // it also is tackled in org.neo4j.ogm.persistence.relationships.transitive.abb.ABBTest,
        // org.neo4j.ogm.persistence.relationships.direct.abb.ABBTest and some others, but there it fits
        // even worse.

        session.query("CREATE (e1:Entity1)\n"
            + "CREATE (e2:Entity2)\n"
            + "CREATE (e3:Entity2)\n"
            + "CREATE (e4:Entity2)\n"
            + "CREATE (e1) - [:MY_RELATIONSHIP {ordering: 1}] -> (e3)\n"
            + "CREATE (e1) - [:MY_RELATIONSHIP {ordering: 2}] -> (e4)\n"
            + "CREATE (e1) - [:MY_RELATIONSHIP {ordering: 3}] -> (e2)\n"
            + "RETURN *", Collections.emptyMap());
        session.clear();

        Entity1 entity1 = session.queryForObject(Entity1.class,
            "MATCH (e1:Entity1)-[r:MY_RELATIONSHIP]->(e2:Entity2)\n"
                + "RETURN e1, r, e2\n"
                + "ORDER BY r.ordering", Collections.emptyMap());
        assertThat(entity1.getEntries()).extracting(MyRelationship::getOrdering).containsExactly(1, 2, 3);
    }
}
