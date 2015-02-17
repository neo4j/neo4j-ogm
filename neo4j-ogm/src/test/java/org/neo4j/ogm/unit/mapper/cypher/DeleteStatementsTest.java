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

import org.junit.Test;
import org.neo4j.ogm.session.request.strategy.DeleteStatements;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class DeleteStatementsTest {

    private final DeleteStatements statements = new DeleteStatements();

    @Test
    public void testDeleteOne() {
        assertEquals("MATCH (n) WHERE id(n) = { id } OPTIONAL MATCH (n)-[r]-() DELETE r, n", statements.delete(0L).getStatement());
    }

    @Test
    public void testDeleteAll() {
        assertEquals("MATCH (n) WHERE id(n) in { ids } OPTIONAL MATCH (n)-[r]-() DELETE r, n", statements.deleteAll(Arrays.asList(1L, 2L)).getStatement());
    }

    @Test
    public void testPurge() {
        assertEquals("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r, n", statements.purge().getStatement());
    }

    @Test
    public void testDeleteByLabel() {
        assertEquals("MATCH (n:TRAFFIC_WARDENS) OPTIONAL MATCH (n)-[r]-() DELETE r, n", statements.deleteByLabel("TRAFFIC_WARDENS").getStatement());
    }
}
