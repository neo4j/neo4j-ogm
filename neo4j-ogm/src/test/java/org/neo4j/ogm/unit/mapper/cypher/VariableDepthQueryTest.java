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

package org.neo4j.ogm.unit.mapper.cypher;

import org.neo4j.ogm.model.Property;
import org.junit.Test;
import org.neo4j.ogm.session.request.strategy.VariableDepthQuery;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class VariableDepthQueryTest {

    private final VariableDepthQuery query = new VariableDepthQuery();

    @Test
    public void testFindOne() throws Exception {
        assertEquals("MATCH p=(n)-[*0..2]-(m) WHERE id(n) = { id } RETURN collect(distinct p)", query.findOne(0L, 2).getStatement());
    }

    @Test
    public void testFindAllCollection() throws Exception {
        assertEquals("MATCH p=(n)-[*0..1]-(m) WHERE id(n) in { ids } RETURN collect(distinct p)", query.findAll(Arrays.asList(1L, 2L, 3L), 1).getStatement());
    }

    @Test
    public void testFindAll() throws Exception {
        assertEquals("MATCH p=()-->() RETURN p", query.findAll().getStatement());
    }

    @Test
    public void testFindByLabel() throws Exception {
        assertEquals("MATCH p=(n:Orbit)-[*0..3]-(m) RETURN collect(distinct p)", query.findByLabel("Orbit", 3).getStatement());
    }

    @Test
    public void testFindByProperty() throws Exception {
        assertEquals("MATCH p=(n:Asteroid)-[*0..4]-(m) WHERE n.diameter = { diameter } RETURN collect(distinct p)", query.findByProperty("Asteroid", new Property<String, Object>("diameter", 60.2), 4).getStatement());
    }

    @Test
    public void testFindOneZeroDepth() throws Exception {
        assertEquals("MATCH (n) WHERE id(n) = { id } RETURN n", query.findOne(0L, 0).getStatement());
    }

    @Test
    public void testFindAllCollectionZeroDepth() throws Exception {
        assertEquals("MATCH (n) WHERE id(n) in { ids } RETURN collect(n)", query.findAll(Arrays.asList(1L, 2L, 3L), 0).getStatement());
    }

    @Test
    public void testFindByLabelZeroDepth() throws Exception {
        assertEquals("MATCH (n:Orbit) RETURN collect(n)", query.findByLabel("Orbit", 0).getStatement());
    }

    @Test
    public void testFindByPropertyZeroDepth() throws Exception {
        assertEquals("MATCH (n:Asteroid) WHERE n.diameter = { diameter } RETURN collect(n)", query.findByProperty("Asteroid", new Property<String, Object>("diameter", 60.2), 0).getStatement());
    }


}
