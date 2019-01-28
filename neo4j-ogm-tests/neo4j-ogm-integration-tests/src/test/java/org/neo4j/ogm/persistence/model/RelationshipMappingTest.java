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

import java.io.IOException;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Result;
import org.neo4j.ogm.domain.election.Candidate;
import org.neo4j.ogm.domain.election.Voter;
import org.neo4j.ogm.domain.policy.Person;
import org.neo4j.ogm.domain.policy.Policy;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.GraphTestUtils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Mark Angrish
 * @author Luanne Misquitta
 */
public class RelationshipMappingTest extends MultiDriverTestClass {

    private Session session;

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.policy", "org.neo4j.ogm.domain.election");
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

    /**
     * @see DATAGRAPH-674
     */
    @Test
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
}
