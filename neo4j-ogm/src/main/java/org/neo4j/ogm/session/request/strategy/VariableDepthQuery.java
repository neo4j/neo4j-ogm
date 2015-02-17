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

import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.cypher.query.GraphModelQuery;
import org.neo4j.ogm.session.Utils;

import java.util.Collection;

public class VariableDepthQuery implements QueryStatements {

    @Override
    public GraphModelQuery findOne(Long id, int depth) {
        int max = max(depth);
        int min = min(max);
        if (max > 0) {
            String qry = String.format("MATCH p=(n)-[*%d..%d]-(m) WHERE id(n) = { id } RETURN collect(distinct p)", min, max);
            return new GraphModelQuery(qry, Utils.map("id", id));
        } else {
            return DepthZeroReadStrategy.findOne(id);
        }
    }
    @Override
    public GraphModelQuery findAll(Collection<Long> ids, int depth) {
        int max = max(depth);
        int min = min(max);
        if (max > 0) {
            String qry=String.format("MATCH p=(n)-[*%d..%d]-(m) WHERE id(n) in { ids } RETURN collect(distinct p)", min, max);
            return new GraphModelQuery(qry, Utils.map("ids", ids));
        } else {
            return DepthZeroReadStrategy.findAll(ids);
        }
    }

    @Override
    public GraphModelQuery findAll() {
        return new GraphModelQuery("MATCH p=()-->() RETURN p", Utils.map());
    }

    @Override
    public GraphModelQuery findByLabel(String label, int depth) {
        int max = max(depth);
        int min = min(max);
        if (max > 0) {
            String qry = String.format("MATCH p=(n:%s)-[*%d..%d]-(m) RETURN collect(distinct p)", label, min, max);
            return new GraphModelQuery(qry, Utils.map());
        } else {
            return DepthZeroReadStrategy.findByLabel(label);
        }
    }

    @Override
    public GraphModelQuery findByProperty(String label, Property<String, Object> property, int depth) {
        int max = max(depth);
        int min = min(max);
        if (max > 0) {
            String qry = String.format("MATCH p=(n:%s)-[*%d..%d]-(m) WHERE n.%s = { %s } RETURN collect(distinct p)", label, min, max, property.getKey(), property.getKey());
            return new GraphModelQuery(qry, Utils.map(property.getKey(), property.asParameter()));
        } else {
            return DepthZeroReadStrategy.findByProperty(label, property);
        }
    }

    private int min(int depth) {
        return Math.min(0, depth);
    }

    private int max(int depth) {
        return Math.max(0, depth);
    }

    private static class DepthZeroReadStrategy {

        public static GraphModelQuery findOne(Long id) {
            return new GraphModelQuery("MATCH (n) WHERE id(n) = { id } RETURN n", Utils.map("id", id));
        }

        public static GraphModelQuery findAll(Collection<Long> ids) {
            return new GraphModelQuery("MATCH (n) WHERE id(n) in { ids } RETURN collect(n)", Utils.map("ids", ids));
        }

        public static GraphModelQuery findByLabel(String label) {
            return new GraphModelQuery(String.format("MATCH (n:%s) RETURN collect(n)", label), Utils.map());
        }

        public static GraphModelQuery findByProperty(String label, Property<String, Object> property) {
            return new GraphModelQuery(String.format("MATCH (n:%s) WHERE n.%s = { %s } RETURN collect(n)", label, property.getKey(), property.getKey()), Utils.map(property.getKey(), property.asParameter()));
        }

    }
}
