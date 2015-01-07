package org.neo4j.ogm.unit.mapper;


import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.policy.Person;
import org.neo4j.ogm.domain.policy.Policy;

public class RelationshipMappingTest extends MappingTest {


    @BeforeClass
    public static void setUp() {
        MappingTest.setUp("org.neo4j.ogm.domain.policy");
    }

    @Test
    public void testThatABiDirectionalMappingIsEstablishedWhenAMutualRelationshipWithNoAnnotationsIsSaved() {

        Person jim = new Person("Jim");
        Policy policy = new Policy("Health");

        // create a mutual relationship
        policy.getInfluencers().add(jim);
        jim.getInfluenced().add(policy);

        // expect both objects to have a relationship to each other
        saveAndVerify(policy, "CREATE (n:Policy:DomainObject {name:'Health'})-[:INFLUENCERS]->(m:Person:DomainObject {name:'Jim'})-[:INFLUENCED]->(n)");

    }

    @Test
    public void testThatAnAnnotatedRelationshipOnTwoObjectsThatIsSavedFromTheOutgoingCreatesTheCorrectRelationshipInTheGraph() {

        Person jim = new Person("Jim");
        Policy policy = new Policy("Health");

        jim.getWritten().add(policy);

        saveAndVerify(jim, "CREATE (n:Policy:DomainObject {name:'Health'})<-[:WRITES_POLICY]-(m:Person:DomainObject {name:'Jim'})");

    }

    @Test
    public void testThatAnAnnotatedRelationshipSavedFromTheIncomingSideCreatesTheCorrectRelationshipInTheGraph() {

        Person jim = new Person("Jim");
        Policy policy = new Policy("Health");

        policy.getWriters().add(jim);

        // we a single relationship, outgoing from person to policy to be established
        saveAndVerify(policy, "CREATE (n:Policy:DomainObject {name:'Health'})<-[:WRITES_POLICY]-(m:Person:DomainObject {name:'Jim'})");

    }

    @Test
    public void testPersistAnnotatedSingleRelationshipMappingBothDomainObjectsParticipating() {

        Person jim = new Person("Jim");
        Policy policy = new Policy("Health");

        // establish the relationship in both directions in the domain model
        policy.getWriters().add(jim);
        jim.getWritten().add(policy);

        // verify we create only one directed relationship in the graph
        saveAndVerify(policy, "CREATE (n:Policy:DomainObject {name:'Health'})<-[:WRITES_POLICY]-(m:Person:DomainObject {name:'Jim'})");

    }



}
