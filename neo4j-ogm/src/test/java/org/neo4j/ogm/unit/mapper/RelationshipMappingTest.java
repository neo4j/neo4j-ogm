/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
