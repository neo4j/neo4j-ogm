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

import org.neo4j.ogm.entityaccess.EntityAccessStrategy;
import org.neo4j.ogm.metadata.info.ClassInfo;

import java.util.*;

public abstract class NodeBuilder implements CypherEmitter, Comparable<NodeBuilder> {

    private final String cypherReference;

    final Map<String, Object> props = new HashMap<>();
    final List<String> labels = new ArrayList<>();

    /**
     * Constructs a new {@link NodeBuilder} identified by the named variable in the context of its enclosing Cypher
     * query.
     *
     * @param variableName The name of the variable to use
     */
    NodeBuilder(String variableName) {
        this.cypherReference = variableName;
    }

    NodeBuilder addLabel(String labelName) {
        this.labels.add(labelName);
        return this;
    }


    NodeBuilder addProperty(String propertyName, Object value) {
        this.props.put(propertyName, value);
        return this;
    }

    public NodeBuilder addLabels(Iterable<String> labelName) {
        for (String label : labelName) {
            addLabel(label);
        }
        return this;
    }

    public abstract NodeBuilder mapProperties(Object toPersist, ClassInfo classInfo, EntityAccessStrategy objectAccessStrategy);

    @Override
    public String toString() {
        return "(" + cypherReference + ":" + this.labels + " " + this.props + ")";
    }

    public static String toCsv(Iterable<String> elements) {
        StringBuilder sb = new StringBuilder();
        for (String element : elements) {
            sb.append(element).append(',');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public String reference() {
        return cypherReference;
    }

    @Override
    public int compareTo(NodeBuilder o) {
        return cypherReference.compareTo(o.cypherReference);
    }


}
