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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ReturnClauseBuilder implements CypherEmitter {
    @Override
    public boolean emit(StringBuilder queryBuilder, Map<String, Object> parameters, Set<String> varStack) {

        if (!varStack.isEmpty()) {
            queryBuilder.append(" RETURN ");
            for (Iterator<String> it = varStack.iterator(); it.hasNext(); ) {
                String var = it.next();
                queryBuilder.append("id(");
                queryBuilder.append(var);
                queryBuilder.append(") AS ");
                queryBuilder.append(var);
                if (it.hasNext()) {
                    queryBuilder.append(", ");
                }
            }
        }
        return !varStack.isEmpty();
    }
}
