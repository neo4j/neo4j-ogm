package org.neo4j.ogm.unit.mapper;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.cineasts.annotated.Actor;
import org.neo4j.ogm.domain.cineasts.annotated.Movie;
import org.neo4j.ogm.domain.cineasts.annotated.Role;

import java.util.HashSet;
import java.util.Set;

public class RelationshipEntityMappingTest extends MappingTest {

    @BeforeClass
    public static void setUp() {
        MappingTest.setUp("org.neo4j.ogm.domain.cineasts.annotated");
    }

    @Test
    public void testThatAnnotatedRelationshipOnRelationshipEntityCreatesTheCorrectRelationshipTypeInTheGraph() {
        Movie hp = new Movie();
        hp.setTitle("Goblet of Fire");
        hp.setYear(2005);

        Actor daniel = new Actor("Daniel Radcliffe");
        daniel.playedIn(hp, "Harry Potter");
        saveAndVerify(daniel, "CREATE (m:Movie {title : 'Goblet of Fire',year:2005 } ) create (a:Actor {name:'Daniel Radcliffe'}) create (a)-[:ACTS_IN {role:'Harry Potter'}]->(m)");

    }

    @Test
    public void testThatRelationshipEntityNameIsUsedAsRelationshipTypeWhenTypeIsNotDefined() {
        Movie hp = new Movie();
        hp.setTitle("Goblet of Fire");
        hp.setYear(2005);

        Actor daniel = new Actor("Daniel Radcliffe");
        daniel.nominatedFor(hp, "Saturn Award", 2005);
        saveAndVerify(daniel, "CREATE (m:Movie {title : 'Goblet of Fire',year:2005 } ) create (a:Actor {name:'Daniel Radcliffe'}) create (a)-[:NOMINATIONS {name:'Saturn Award', year:2005}]->(m)");
        //Not quite sure if the relationship type should be the field name or the RelationshipEntity class name?
    }

}
