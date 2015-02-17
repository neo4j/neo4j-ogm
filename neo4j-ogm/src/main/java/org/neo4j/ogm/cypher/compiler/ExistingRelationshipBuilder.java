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

class ExistingRelationshipBuilder extends RelationshipBuilder {

    private final Long id;

    ExistingRelationshipBuilder(String variableName, Long relationshipId) {
        super(variableName);
        this.id = relationshipId;
    }

    @Override
    public void relate(String startNodeIdentifier, String endNodeIdentifier) {
        this.startNodeIdentifier = startNodeIdentifier;
        this.endNodeIdentifier = endNodeIdentifier;
        this.reference = reference;
    }

    @Override
    public boolean emit(StringBuilder queryBuilder, Map<String, Object> parameters, Set<String> varStack) {
        // admittedly, this isn't brilliant, as we'd ideally avoid creating the relationship in the first place
        // this doesn't make sense here because we don't even use the node identifiers for updating rels!
        if (this.startNodeIdentifier == null || this.endNodeIdentifier == null) {
            return false;
        }

        if (!varStack.isEmpty()) {
            queryBuilder.append(" WITH ").append(NodeBuilder.toCsv(varStack));
        }

        queryBuilder.append(" MATCH ()-[").append(this.reference).append("]->() WHERE id(")
                .append(this.reference).append(")=").append(this.id);

        if (!this.props.isEmpty()) {
            queryBuilder.append(" SET ").append(this.reference).append("+={").append(this.reference).append("_props} ");
            parameters.put(this.reference + "_props", this.props);
        }

        return true;
    }

}
