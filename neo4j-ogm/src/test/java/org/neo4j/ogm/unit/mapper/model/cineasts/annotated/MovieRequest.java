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

package org.neo4j.ogm.unit.mapper.model.cineasts.annotated;

import org.neo4j.ogm.RequestProxy;

public class MovieRequest extends RequestProxy {

    public String[] getResponse() {
        return jsonModel;
    }

    private static String[] jsonModel = {
            "{\"graph\": { " +
                    "\"nodes\" :[ " +
                    "{\"id\" : \"15\",\"labels\" : [ \"Movie\"],    \"properties\" : {\"title\" : \"Pulp Fiction\"}}, " +
                    "{\"id\" : \"16\",\"labels\" : [ \"Movie\"],    \"properties\" : {\"title\" : \"Top Gun\"}}, " +
                    "{\"id\" : \"17\",\"labels\" : [ \"Movie\"],    \"properties\" : {\"title\" : \"Django Unchained\"}}, " +
                    "{\"id\" : \"18\",\"labels\" : [ \"User\"],     \"properties\" : {\"name\" : \"Michal\"}}, " +
                    "{\"id\" : \"19\",\"labels\" : [ \"User\"],     \"properties\" : {\"name\" : \"Vince\"}}, " +
                    "{\"id\" : \"20\",\"labels\" : [ \"User\"],     \"properties\" : {\"name\" : \"Daniela\"}} " +
                    "], " +
                    "\"relationships\": [" +
                    "{\"id\":\"141\",\"type\":\"RATED\",\"startNode\":\"18\",\"endNode\":\"15\",\"properties\":{ \"stars\" : 5, \"comment\" : \"Best Film Ever!\" }}, " +
                    "{\"id\":\"142\",\"type\":\"RATED\",\"startNode\":\"18\",\"endNode\":\"16\",\"properties\":{ \"stars\" : 3, \"comment\" : \"Overrated\" }}, " +
                    "{\"id\":\"143\",\"type\":\"RATED\",\"startNode\":\"19\",\"endNode\":\"16\",\"properties\":{ \"stars\" : 4 }} " +
                    "] " +
                    "} }"
    };
}
