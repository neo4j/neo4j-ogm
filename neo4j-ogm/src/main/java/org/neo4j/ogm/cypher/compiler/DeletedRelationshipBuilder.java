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

public class DeletedRelationshipBuilder implements CypherEmitter, Comparable<DeletedRelationshipBuilder> {

    private final String type;
    private final String src;
    private final String tgt;
    private final String rid;

    public DeletedRelationshipBuilder(String type, String src, String tgt, String rid) {
        this.type = type;
        this.src = src;
        this.tgt = tgt;
        this.rid = rid;
    }

    public boolean emit(StringBuilder queryBuilder, Map<String, Object> parameters, Set<String> varStack) {

        if (!varStack.isEmpty()) {
            queryBuilder.append(" WITH ").append(NodeBuilder.toCsv(varStack));
        }

        queryBuilder.append(" MATCH (");
        queryBuilder.append(src);
        queryBuilder.append(")-[");
        queryBuilder.append(rid);
        queryBuilder.append(":");
        queryBuilder.append(type);
        queryBuilder.append("]->(");
        queryBuilder.append(tgt);
        queryBuilder.append(")");

        boolean where = false;

        if (!varStack.contains(src)) {
            queryBuilder.append(" WHERE id(");
            queryBuilder.append(src);
            queryBuilder.append(")=");
            queryBuilder.append(src.substring(1)); // existing nodes have an id. we pass it in as $id
            varStack.add(src);
            where = true;
        }

        if (!varStack.contains(tgt)) {
            if (where) {
                queryBuilder.append(" AND id(");
            } else {
                queryBuilder.append(" WHERE id(");
            }
            queryBuilder.append(tgt);
            queryBuilder.append(")=");
            queryBuilder.append(tgt.substring(1)); // existing nodes have an id. we pass it in as $id
            varStack.add(tgt);
        }

        queryBuilder.append(" DELETE ");
        queryBuilder.append(rid);

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeletedRelationshipBuilder that = (DeletedRelationshipBuilder) o;

        if (!rid.equals(that.rid)) return false;
        if (!src.equals(that.src)) return false;
        if (!tgt.equals(that.tgt)) return false;
        if (!type.equals(that.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + src.hashCode();
        result = 31 * result + tgt.hashCode();
        result = 31 * result + rid.hashCode();
        return result;
    }

    @Override
    public int compareTo(DeletedRelationshipBuilder o) {
        return hashCode()-o.hashCode();
    }
}
