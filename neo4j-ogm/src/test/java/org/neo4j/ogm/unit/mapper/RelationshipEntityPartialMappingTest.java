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
import org.neo4j.ogm.domain.cineasts.partial.Actor;
import org.neo4j.ogm.domain.cineasts.partial.Movie;

/*
 * The purpose of these tests is to describe the behaviour of the
 * mapper when a RelationshipEntity object is not referenced by
 * both of its Related entities.
 */
public class RelationshipEntityPartialMappingTest extends MappingTest {

    @BeforeClass
    public static void setUp() {
        MappingTest.setUp("org.neo4j.ogm.domain.cineasts.partial");
    }

    @Test
    public void testCreateActorRoleAndMovie() {

        Actor keanu = new Actor("Keanu Reeves");
        Movie matrix = new Movie("The Matrix");

        // note: this does not establish a role relationsip on the matrix
        keanu.addRole("Neo", matrix);

        saveAndVerify(keanu,
                        "create (a:Actor {name:'Keanu Reeves'}) " +
                        "create (m:Movie {name:'The Matrix'}) " +
                        "create (a)-[:ACTS_IN {played:'Neo'}]->(m)");
    }

}
