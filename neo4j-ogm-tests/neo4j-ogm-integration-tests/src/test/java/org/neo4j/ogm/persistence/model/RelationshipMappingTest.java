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

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.election.Candidate;
import org.neo4j.ogm.domain.election.Voter;
import org.neo4j.ogm.domain.gh640.MyNode;
import org.neo4j.ogm.domain.gh640.MyNodeWithAssignedId;
import org.neo4j.ogm.domain.gh641.Entity1;
import org.neo4j.ogm.domain.gh641.MyRelationship;
import org.neo4j.ogm.domain.gh656.Group;
import org.neo4j.ogm.domain.gh656.GroupVersion;
import org.neo4j.ogm.domain.gh666.MessedUpNode1;
import org.neo4j.ogm.domain.gh666.MessedUpNode2;
import org.neo4j.ogm.domain.gh666.MessedUpNode3;
import org.neo4j.ogm.domain.gh704.Country;
import org.neo4j.ogm.domain.gh737.Gh737Node1;
import org.neo4j.ogm.domain.gh737.Gh737Node2;
import org.neo4j.ogm.domain.policy.Person;
import org.neo4j.ogm.domain.policy.Policy;
import org.neo4j.ogm.domain.typed_relationships.SomeEntity;
import org.neo4j.ogm.domain.typed_relationships.TypedEntity;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.response.model.RelationshipModel;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Mark Angrish
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class RelationshipMappingTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetup() {
        sessionFactory = new SessionFactory(getDriver(),
            "org.neo4j.ogm.domain.election",
            "org.neo4j.ogm.domain.gh640",
            "org.neo4j.ogm.domain.gh641",
            "org.neo4j.ogm.domain.gh656",
            "org.neo4j.ogm.domain.gh666",
            "org.neo4j.ogm.domain.gh704",
            "org.neo4j.ogm.domain.gh737",
            "org.neo4j.ogm.domain.policy",
            "org.neo4j.ogm.domain.typed_relationships");
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

        session.clear();
        assertThat(session.query(
            "MATCH (n:Policy:DomainObject {name:'Health'})-[:INFLUENCERS]->"
                + "(m:Person:DomainObject {name:'Jim'})-[:INFLUENCED]->(n) return n, m", emptyMap()).queryResults())
            .hasSize(1);
    }

    @Test
    public void testThatAnAnnotatedRelationshipOnTwoObjectsThatIsSavedFromTheOutgoingCreatesTheCorrectRelationshipInTheGraph() {

        Person jim = new Person("Jim");
        Policy policy = new Policy("Health");

        jim.getWritten().add(policy);

        session.save(jim);
        session.clear();
        assertThat(session.query("MATCH (n:Policy:DomainObject {name:'Health'})<-[:WRITES_POLICY]"
            + "-(m:Person:DomainObject {name:'Jim'}) return n, m", emptyMap()).queryResults()).hasSize(1);
    }

    @Test
    public void testThatAnAnnotatedRelationshipSavedFromTheIncomingSideCreatesTheCorrectRelationshipInTheGraph() {

        Person jim = new Person("Jim");
        Policy policy = new Policy("Health");

        policy.getWriters().add(jim);

        // we a single relationship, outgoing from person to policy to be established
        session.save(policy);

        session.clear();
        assertThat(session.query("MATCH (n:Policy:DomainObject {name:'Health'})<-[:WRITES_POLICY]"
            + "-(m:Person:DomainObject {name:'Jim'}) return n, m", emptyMap()).queryResults()).hasSize(1);
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
        session.clear();
        assertThat(session.query("MATCH (n:Policy:DomainObject {name:'Health'})<-[:WRITES_POLICY]"
            + "-(m:Person:DomainObject {name:'Jim'}) return n, m", emptyMap()).queryResults()).hasSize(1);
    }

    @Test // DATAGRAPH-674
    public void testAnnotatedRelationshipTypeWhenMethodsAreJsonIgnored() {
        Person jim = new Person("Jim");
        Policy policy = new Policy("Health");

        policy.setAuthorized(jim);
        jim.setAuthorized(policy);

        session.save(jim);

        session.clear();
        assertThat(session.query("MATCH (n:Policy:DomainObject {name:'Health'})<-[:AUTHORIZED_POLICY]"
            + "-(m:Person:DomainObject {name:'Jim'}) return n, m", emptyMap()).queryResults()).hasSize(1);
    }

    @Test
    public void shouldAllowVoterToChangeHerMind() {

        Iterable<Map<String, Object>> executionResult = session.query(
            "CREATE " +
                "(a:Voter:Candidate {name:'A'}), " +
                "(b:Voter:Candidate {name:'B'}), " +
                "(v:Voter {name:'V'})-[:CANDIDATE_VOTED_FOR]->(b) " +
                "RETURN id(a) AS a_id, id(b) AS b_id, id(v) AS v_id", emptyMap()).queryResults();

        Map<String, ?> results = executionResult.iterator().next();

        Long aid = (Long) results.get("a_id");
        Long bid = (Long) results.get("b_id");
        Long vid = (Long) results.get("v_id");
        session.clear();

        Candidate a = session.load(Candidate.class, aid);

        Candidate b = session.load(Candidate.class, bid);

        Voter v = session.load(Voter.class, vid);

        v.candidateVotedFor = a;
        session.save(v);

        session.clear();
        assertThat(session.query("MATCH (v:Voter {name:'V'})-[:CANDIDATE_VOTED_FOR]->(a:Voter:Candidate {name:'A'}) " +
            "MATCH (b:Voter:Candidate {name:'B'}) " +
            " return a, b, v", emptyMap()).queryResults())
            .hasSize(1);
    }

    @Test // GH-640
    public void shouldDealWithTheSameButNotEqualParentEntities() {

        Session tx = sessionFactory.openSession();
        Map<String, Object> result = tx.query("CREATE (n1:MyNode {name: 'node1'})\n"
            + "CREATE (n2:MyNode {name: 'node2'})\n"
            + "CREATE (n3:MyNode {name: 'node3'})\n"
            + "CREATE (n1) - [:REL_TWO] -> (n2)\n"
            + "CREATE (n2) - [:REL_ONE] -> (n1)\n"
            + "RETURN id(n1) as idOfn1, id(n2) as idOfn2, id(n3) as idOfn3", emptyMap()).iterator().next();

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
        session.clear();
        assertThat(session.query("MATCH (n1:MyNode {name: 'Dirty thing.'}) - [:REL_TWO] -> (n2:MyNode {name: 'node2'}) "
            + "MATCH (n3:MyNode {name: 'node3'}) - [:REL_ONE] -> (n1)"
            + "RETURN n1, n2, n3", emptyMap()).queryResults()).hasSize(1);
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
            + "RETURN id(n1) as idOfn1, id(n2) as idOfn2, id(n3) as idOfn3", emptyMap()).iterator().next();

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

        session.clear();
        assertThat(session.query("MATCH (n1:MyNodeWithAssignedId {name: 'node1'}) - [:REL_TWO] -> (n2:MyNodeWithAssignedId {name: 'node2'}) "
            + "MATCH (n3:MyNodeWithAssignedId {name: 'node3'}) - [:REL_ONE] -> (n1) "
            + "RETURN n1, n2, n3", emptyMap()).queryResults()).hasSize(1);
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
            + "RETURN *", emptyMap());
        session.clear();

        Entity1 entity1 = session.queryForObject(Entity1.class,
            "MATCH (e1:Entity1)-[r:MY_RELATIONSHIP]->(e2:Entity2)\n"
                + "RETURN e1, r, e2\n"
                + "ORDER BY r.ordering", emptyMap());
        assertThat(entity1.getEntries()).extracting(MyRelationship::getOrdering).containsExactly(1, 2, 3);
    }

    @Test // GH-528
    public void shouldDealWithTypedRelationships() {
        SomeEntity someEntity = new SomeEntity();

        someEntity.setThing(new TypedEntity<>(42.21));
        someEntity.setMoreThings(Arrays.asList(new TypedEntity<>("Die halbe Wahrheit"), new TypedEntity<>("21")));
        someEntity.setSomeOtherStuff(Arrays.asList("A", "B", "C"));

        session.save(someEntity);
        session.clear();

        someEntity = session.load(SomeEntity.class, someEntity.getId());
        assertThat(someEntity.getThing().getSomeThing())
            .isEqualTo(42.21);
        assertThat(someEntity.getMoreThings())
            .extracting(t -> (String) t.getSomeThing())
            .containsExactlyInAnyOrder("Die halbe Wahrheit", "21");
        assertThat(someEntity.getSomeOtherStuff())
            .containsExactlyInAnyOrder("A", "B", "C");
    }

    @Test // GH-656
    public void genericRelationshipsInParentClassesShouldWork() {
        Group group = new Group();
        GroupVersion groupVersion = new GroupVersion();
        group.setVersions(Collections.singleton(groupVersion));

        sessionFactory.openSession().save(group);

        group = sessionFactory.openSession().load(Group.class, group.getUuid());
        assertThat(group.getVersions()).hasSize(1);
    }

    @Test // GH-704
    public void generic1To1RelationshipsShouldWork() {

        // Arrange an old country
        Country oldRev = new Country();
        oldRev.setName("oldCountry");
        sessionFactory.openSession().save(oldRev);

        // Create a new one
        Session session = sessionFactory.openSession();
        oldRev = session.load(Country.class, oldRev.getId());
        assertThat(oldRev.getPreviousRevision()).isNull();

        Country newRev = new Country();
        newRev.setName("newCountry");
        newRev.setPreviousRevision(oldRev);

        session.save(newRev);

        // Assert presence of revision
        newRev = sessionFactory.openSession().load(Country.class, newRev.getId());
        assertThat(newRev.getPreviousRevision()).isNotNull();
        assertThat(newRev.getPreviousRevision().getName()).isEqualTo("oldCountry");
        assertThat(newRev.getPreviousRevision()).isEqualTo(oldRev);

    }

    @Test // GH-657
    public void deletesOfEntitiesWithTheSameButNotEqualParentShouldWork() {

        Session tx = sessionFactory.openSession();
        Map<String, Object> result = tx.query("CREATE (n1:MyNode {name: 'node1'})\n"
            + "CREATE (n2:MyNode {name: 'node2'})\n"
            + "CREATE (n3:MyNode {name: 'node3'})\n"
            + "CREATE (n1) - [:REL_TWO] -> (n2)\n"
            + "CREATE (n2) - [:REL_ONE] -> (n1)\n"
            + "RETURN id(n1) as idOfn1, id(n2) as idOfn2, id(n3) as idOfn3", Collections.emptyMap()).iterator().next();

        tx = sessionFactory.openSession();

        MyNode node1 = tx.load(MyNode.class, (Long)result.get("idOfn1"));
        MyNode node2 = tx.load(MyNode.class, (Long)result.get("idOfn2"));
        MyNode node3 = tx.load(MyNode.class, (Long)result.get("idOfn3"));

        assertThat(node1).isNotNull();
        assertThat(node2).isNotNull();
        assertThat(node3).isNotNull();

        assertThat(node1.getRefOne()).isEqualTo(node2);
        assertThat(node1.getRefTwo()).containsOnly(node2);
        assertThat(node2.getRefTwo()).containsOnly(node1);

        tx = sessionFactory.openSession();
        MyNode changed = tx.load(MyNode.class, node1.getId()).copy();
        changed.setName("Dirty thing.");
        changed.setRefTwo(Collections.emptyList());

        tx.save(changed);

        // Again, verify in a new session.
        tx = sessionFactory.openSession();
        node1 = tx.load(MyNode.class, changed.getId());
        assertThat(node1.getRefOne()).isEqualTo(node2);
        assertThat(node1.getRefTwo()).isEmpty();

        Iterable<Map<String, Object>> actual = sessionFactory.openSession().query(""
            + " MATCH (n1:MyNode {name: 'Dirty thing.'}) <- [:REL_ONE] -(n2:MyNode {name: 'node2'}) "
            + " MATCH (n3:MyNode {name: 'node3'}) "
            + " RETURN n1,n2,n3,exists((n1) - [:REL_TWO] -> (n2)) as relTwo",
            emptyMap()).queryResults();
        assertThat(actual).hasSize(1);
        assertThat(actual.iterator().next()).containsEntry("relTwo", false);
    }

    @Test // GH-657
    public void deletesOfEntitiesWithTheSameButNotEqualParentShouldWork2() {

        Session tx = sessionFactory.openSession();
        Map<String, Object> result = tx.query("CREATE (n1:MyNode {name: 'node1'})\n"
            + "CREATE (n2:MyNode {name: 'node2'})\n"
            + "CREATE (n3:MyNode {name: 'node3'})\n"
            + "CREATE (n1) - [:REL_TWO] -> (n2)\n"
            + "CREATE (n2) - [:REL_ONE] -> (n1)\n"
            + "RETURN id(n1) as idOfn1, id(n2) as idOfn2, id(n3) as idOfn3", Collections.emptyMap()).iterator().next();

        tx = sessionFactory.openSession();

        MyNode node1 = tx.load(MyNode.class, (Long)result.get("idOfn1"));
        MyNode node2 = tx.load(MyNode.class, (Long)result.get("idOfn2"));
        MyNode node3 = tx.load(MyNode.class, (Long)result.get("idOfn3"));

        assertThat(node1).isNotNull();
        assertThat(node2).isNotNull();
        assertThat(node3).isNotNull();

        assertThat(node1.getRefOne()).isEqualTo(node2);
        assertThat(node1.getRefTwo()).containsOnly(node2);

        assertThat(node2.getRefOne()).isNull();
        assertThat(node2.getRefTwo()).containsOnly(node1);

        tx = sessionFactory.openSession();
        MyNode changed = tx.load(MyNode.class, node2.getId()).copy();
        changed.setName("Dirty thing.");
        changed.setRefTwo(Collections.emptyList());

        tx.save(changed);

        // Again, verify in a new session.
        tx = sessionFactory.openSession();
        node2 = tx.load(MyNode.class, changed.getId());
        assertThat(node2.getRefOne()).isNull();
        assertThat(node2.getRefTwo()).isEmpty();

        Iterable<Map<String, Object>> actual = sessionFactory.openSession().query(""
                + " MATCH (n1:MyNode {name: 'node1'}) <- [:REL_ONE] - (n2:MyNode {name: 'Dirty thing.'}) "
                + " MATCH (n3:MyNode {name: 'node3'})"
                + " RETURN n1,n2,n3,exists((n1) - [:REL_TWO] -> (n2)) as relTwo",
            emptyMap()).queryResults();
        assertThat(actual).hasSize(1);
        assertThat(actual.iterator().next()).containsEntry("relTwo", false);
    }

    @Test // GH-657
    public void deletesOfEntitiesWithTheSameButNotEqualParentShouldWork3() {

        Session tx = sessionFactory.openSession();
        Map<String, Object> result = tx.query("CREATE (n1:MyNode {name: 'node1'})\n"
            + "CREATE (n2:MyNode {name: 'node2'})\n"
            + "CREATE (n3:MyNode {name: 'node3'})\n"
            + "CREATE (n2) - [:REL_ONE] -> (n1)\n"
            + "RETURN id(n1) as idOfn1, id(n2) as idOfn2, id(n3) as idOfn3", Collections.emptyMap()).iterator().next();

        tx = sessionFactory.openSession();

        MyNode node1 = tx.load(MyNode.class, (Long)result.get("idOfn1"));
        MyNode node2 = tx.load(MyNode.class, (Long)result.get("idOfn2"));
        MyNode node3 = tx.load(MyNode.class, (Long)result.get("idOfn3"));

        assertThat(node1).isNotNull();
        assertThat(node2).isNotNull();
        assertThat(node3).isNotNull();

        assertThat(node1.getRefOne()).isEqualTo(node2);
        assertThat(node1.getRefTwo()).isEmpty();
        assertThat(node2.getRefTwo()).isEmpty();

        tx = sessionFactory.openSession();
        MyNode changed = tx.load(MyNode.class, node1.getId()).copy();
        changed.setName("Dirty thing.");
        changed.setRefTwo(Collections.singletonList(node2));
        tx.save(changed);

        // Again, verify in a new session.
        tx = sessionFactory.openSession();
        node1 = tx.load(MyNode.class, changed.getId());
        node2 = tx.load(MyNode.class, node2.getId());
        assertThat(node1.getRefOne()).isEqualTo(node2);
        assertThat(node1.getRefTwo()).containsOnly(node2);
        assertThat(node2.getRefTwo()).containsOnly(node1);

        Iterable<Map<String, Object>> actual = sessionFactory.openSession().query(""
                + " MATCH (n1:MyNode {name: 'Dirty thing.'}) <- [:REL_ONE] - (n2:MyNode {name: 'node2'})"
                + " MATCH (n3:MyNode {name: 'node3'})"
                + " RETURN n1,n2,n3,exists((n1) - [:REL_TWO] - (n2)) as relTwo",
            emptyMap()).queryResults();
        assertThat(actual).hasSize(1);
        assertThat(actual.iterator().next()).containsEntry("relTwo", true);
    }

    @Test // GH-727
    public void shouldNotDropUnmappedRelationshipModels() {

        Session session = sessionFactory.openSession();
        Voter voter = new Voter("V");
        voter.candidateVotedFor = new Candidate("C");
        session.save(voter);

        session = sessionFactory.openSession();
        Iterable<Map<String, Object>> results = session
            .query("MATCH (v) - [r] - (c) WHERE id(v) = $id RETURN r",
                Collections.singletonMap("id", voter.getId())).queryResults();

        assertThat(results).hasSize(1);
        Map<String, Object> row = results.iterator().next();
        assertThat(row).containsKeys("r");
        assertThat(row.get("r")).isNotNull().isInstanceOf(RelationshipModel.class);
    }

    @Test // GH-737
    public void patternComprehensionShouldBeMappedToCorrectRelationships() {

        Map<String, Object> ids = sessionFactory.openSession().query(""
                + "CREATE (n1:Gh737Node1) - [:RELATION_A] -> (n2:Gh737Node2) RETURN id(n1) AS idn1, id(n2) AS idn2",
            Collections.emptyMap()
        ).queryResults().iterator().next();

        Session session;
        session = sessionFactory.openSession();

        // Assert preconditions
        Gh737Node1 node1 = session.load(Gh737Node1.class, (Long) ids.get("idn1"));
        Gh737Node2 node2 = session.load(Gh737Node2.class, (Long) ids.get("idn2"));
        assertThat(node1).isNotNull();
        assertThat(node2).isNotNull();
        assertThat(node1.getRef()).isEqualTo(node2);

        String query = "MATCH (n:Gh737Node1) RETURN n, [(n)-[rn:RELATION_A]-(n2) | [rn, n2]]";

        // Query for dedicated class
        session = sessionFactory.openSession();
        node1 = session.queryForObject(Gh737Node1.class, query, Collections.emptyMap());
        assertThat(node1).isNotNull();
        assertThat(node1.getRef()).isNotNull();

        // Generic query
        session = sessionFactory.openSession();
        Result result = session.query(query, Collections.emptyMap(), true);
        assertThat(result.iterator().hasNext()).isTrue();
        result.forEach(m -> {
            Gh737Node1 n = (Gh737Node1) m.get("n");
            assertThat(n).isNotNull();
            assertThat(n.getRef()).isNotNull();
        });
    }

    @Test // GH-666
    public void shouldNotMessUpNodes() {
        assertMapping(MessedUpNode1.class, node -> node.getRef(), false);
    }

    @Test // GH-666
    public void mayMessUpNodes() {
        assertMapping(MessedUpNode3.class, node -> node.getRef(), true);
    }

    private <T> void assertMapping(Class<T> targetClass, Function<T, MessedUpNode2> refExtractor,
        boolean wildCardMappingAllowed) {

        ClassInfo targetclassInfo = sessionFactory.metaData().classInfo(targetClass);

        Map<String, Object> ids = sessionFactory.openSession().query(""
                + "MERGE (n3:MessedUpNode2:TypeY) <-[:RELATION_A] - (n1:" + targetclassInfo.neo4jName()
                + ")-[:RELATION_NOT_MODELED]->(n2:MessedUpNode2:TypeX) "
                + "RETURN id(n1) as id1, id(n2) as id2, id(n3) as id3",
            Collections.emptyMap()
        ).queryResults().iterator().next();

        Session session = sessionFactory.openSession();
        T n1 = session.load(targetClass, (long) ids.get("id1"));
        MessedUpNode2 n2 = session.load(MessedUpNode2.class, (long) ids.get("id2"));
        MessedUpNode2 n3 = session.load(MessedUpNode2.class, (long) ids.get("id3"));

        assertThat(n1).isNotNull();
        assertThat(n2).isNotNull();
        assertThat(n3).isNotNull();

        // Expected behaviour
        assertThat(refExtractor.apply(n1)).isEqualTo(n3);

        // Loaded from the session
        String query = "MATCH (n1:" + targetclassInfo.neo4jName()
            + ")-[rel:RELATION_NOT_MODELED]->(n2:`TypeX`) WHERE id(n1) = $id1 RETURN n1, rel, n2";
        Iterable<Map<String, Object>> r = session
            .query(query, Collections.singletonMap("id1", (long) ids.get("id1")), true).queryResults();
        assertCorrectRelationShip(r, wildCardMappingAllowed ? n2 : n3);

        // Loaded from the database
        session = sessionFactory.openSession();
        r = session.query(query, Collections.singletonMap("id1", (long) ids.get("id1")), true).queryResults();
        assertCorrectRelationShip(r, wildCardMappingAllowed ? n2 : null);
    }

    private static void assertCorrectRelationShip(Iterable<Map<String, Object>> result, MessedUpNode2 expectedNode) {
        assertThat(result).hasSize(1);
        assertThat(result).allSatisfy(row -> {
            assertThat(row).containsKeys("n1");
            if (expectedNode != null) {
                assertThat(row.get("n1")).extracting("ref").extracting("id").containsExactly(expectedNode.getId());
            } else {
                assertThat(row.get("n1")).extracting("ref").hasSize(1).containsOnlyNulls();
            }
        });
    }
}
