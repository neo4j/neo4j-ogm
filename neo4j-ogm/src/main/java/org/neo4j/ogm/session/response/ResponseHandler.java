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

package org.neo4j.ogm.session.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.ogm.cypher.compiler.CypherContext;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.Property;

import java.util.Collection;
import java.util.Set;

public interface ResponseHandler {

    <T> T loadById(Class<T> type, Neo4jResponse<GraphModel> stream, Long id);
    <T> Collection<T> loadAll(Class<T> type, Neo4jResponse<GraphModel> stream);
    <T> Set<T> loadByProperty(Class<T> type, Neo4jResponse<GraphModel> stream, Property<String, Object> filter);

    void updateObjects(CypherContext context, Neo4jResponse<String> response, ObjectMapper mapper);
}
