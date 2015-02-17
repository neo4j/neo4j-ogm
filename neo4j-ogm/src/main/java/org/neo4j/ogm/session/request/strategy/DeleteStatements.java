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

package org.neo4j.ogm.session.request.strategy;

import org.neo4j.ogm.cypher.statement.ParameterisedStatement;
import org.neo4j.ogm.session.Utils;

import java.util.Collection;

public class DeleteStatements {

    public ParameterisedStatement delete(Long id) {
        return new ParameterisedStatement("MATCH (n) WHERE id(n) = { id } OPTIONAL MATCH (n)-[r]-() DELETE r, n", Utils.map("id",id));
    }

    public ParameterisedStatement deleteAll(Collection<Long> ids) {
        return new ParameterisedStatement("MATCH (n) WHERE id(n) in { ids } OPTIONAL MATCH (n)-[r]-() DELETE r, n", Utils.map("ids", ids));
    }

    public ParameterisedStatement purge() {
        return new ParameterisedStatement("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r, n", Utils.map());
    }

    public ParameterisedStatement deleteByLabel(String label) {
        return new ParameterisedStatement(String.format("MATCH (n:%s) OPTIONAL MATCH (n)-[r]-() DELETE r, n", label), Utils.map());
    }
}
