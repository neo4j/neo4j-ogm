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

package org.neo4j.ogm.session.request;

import org.neo4j.ogm.cypher.query.GraphModelQuery;
import org.neo4j.ogm.cypher.query.RowModelQuery;
import org.neo4j.ogm.cypher.statement.ParameterisedStatement;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.session.response.Neo4jResponse;
import org.neo4j.ogm.session.result.RowModel;

import java.util.List;

public interface RequestHandler {

    Neo4jResponse<GraphModel> execute(GraphModelQuery query, String url);
    Neo4jResponse<RowModel> execute(RowModelQuery query, String url);
    Neo4jResponse<String> execute(ParameterisedStatement statement, String url);
    Neo4jResponse<String> execute(List<ParameterisedStatement> statementList, String url);

}
