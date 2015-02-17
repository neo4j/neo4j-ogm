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

package org.neo4j.ogm.cypher.compiler;

import java.util.Map;
import java.util.Set;

public interface CypherEmitter {

    /**
     * Emits one or more Cypher clauses.
     *
     * @param queryBuilder The {@code StringBuilder} to which the Cypher should be appended
     * @param parameters A {@link Map} to which Cypher parameter values may optionally be added as the query is built up
     * @param varStack The variable stack carried through the query, to which this emitter's variable name may be added
     */

    boolean emit(StringBuilder queryBuilder, Map<String, Object> parameters, Set<String> varStack);
}
