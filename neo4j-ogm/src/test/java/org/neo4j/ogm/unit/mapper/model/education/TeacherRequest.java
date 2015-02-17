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

package org.neo4j.ogm.unit.mapper.model.education;

import org.neo4j.ogm.RequestProxy;

/**
 * MATCH p=(t:TEACHER)--(c) return p
 */
public class TeacherRequest extends RequestProxy {

    public String[] getResponse() {
        return jsonModel;
    }

    private static String[] jsonModel = {

            // mr thomas results
            "{\"graph\": { " +
                "\"nodes\" :[ " +
                    "{\"id\" : \"20\",\"labels\" : [ \"Teacher\"], \"properties\" : { \"name\" :\"Mr Thomas\" } }, " +
                    "{\"id\" : \"2\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"English\" } } " +
                    "], " +
                "\"relationships\": [" +
                "{\"id\":\"202\",\"type\":\"TEACHES\",\"startNode\":\"20\",\"endNode\":\"2\",\"properties\":{}}" +
                "] " +
            "} }"
             ,
            "{\"graph\": { " +
                    "\"nodes\" :[ " +
                    "{\"id\" : \"20\",\"labels\" : [ \"Teacher\"], \"properties\" : { \"name\" :\"Mr Thomas\" } }, " +
                    "{\"id\" : \"3\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"Maths\" } }" +
                    "], " +
                    "\"relationships\": [" +
                    "{\"id\":\"203\",\"type\":\"TEACHES\",\"startNode\":\"20\",\"endNode\":\"3\",\"properties\":{}}" +
                    "] " +
                    "} }"
             ,
            "{\"graph\": { " +
                    "\"nodes\" :[ " +
                    "{\"id\" : \"20\",\"labels\" : [ \"Teacher\"], \"properties\" : { \"name\" :\"Mr Thomas\" } }, " +
                    "{\"id\" : \"4\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"Physics\" } }" +
                    "], " +
                    "\"relationships\": [" +
                    "{\"id\":\"204\",\"type\":\"TEACHES\",\"startNode\":\"20\",\"endNode\":\"4\",\"properties\":{}} " +
                    "] " +
                    "} }"

            ,
            // mrs roberts results

            "{\"graph\": { " +
                "\"nodes\" :[ " +
                    "{\"id\" : \"21\",\"labels\" : [ \"Teacher\"], \"properties\" : { \"name\" :\"Mrs Roberts\" } }, " +
                    "{\"id\" : \"6\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"PE\" } }" +
                    "], " +
                "\"relationships\": [" +
                    "{\"id\":\"212\",\"type\":\"TEACHES\",\"startNode\":\"21\",\"endNode\":\"2\",\"properties\":{}}" +
                    "] " +
            "} }",
            "{\"graph\": { " +
                    "\"nodes\" :[ " +
                    "{\"id\" : \"21\",\"labels\" : [ \"Teacher\"], \"properties\" : { \"name\" :\"Mrs Roberts\" } }, " +
                    "{\"id\" : \"2\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"English\" } }" +
                    "], " +
                    "\"relationships\": [" +
                    "{\"id\":\"216\",\"type\":\"TEACHES\",\"startNode\":\"21\",\"endNode\":\"6\",\"properties\":{}}" +
                    "] " +
                    "} }",
            "{\"graph\": { " +
                    "\"nodes\" :[ " +
                    "{\"id\" : \"21\",\"labels\" : [ \"Teacher\"], \"properties\" : { \"name\" :\"Mrs Roberts\" } }, " +
                    "{\"id\" : \"7\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"History\" } } " +
                    "], " +
                    "\"relationships\": [" +
                    "{\"id\":\"217\",\"type\":\"TEACHES\",\"startNode\":\"21\",\"endNode\":\"7\",\"properties\":{}} " +
                    "] " +
                    "} }",

            // miss young results
            "{\"graph\": { " +
                "\"nodes\" :[ " +
                    "{\"id\" : \"22\",\"labels\" : [ \"Teacher\"], \"properties\" : { \"name\" :\"Miss Young\" } }, " +
                    "{\"id\" : \"5\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"Philosophy and Ethics\" } }" +
                    "], " +
                "\"relationships\": [" +
                    "{\"id\":\"225\",\"type\":\"TEACHES\",\"startNode\":\"22\",\"endNode\":\"5\",\"properties\":{}}" +
                    "] " +
                    "} }",
            "{\"graph\": { " +
                    "\"nodes\" :[ " +
                    "{\"id\" : \"22\",\"labels\" : [ \"Teacher\"], \"properties\" : { \"name\" :\"Miss Young\" } }, " +
                    "{\"id\" : \"7\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"History\" } }" +
                    "], " +
                    "\"relationships\": [" +
                    "{\"id\":\"227\",\"type\":\"TEACHES\",\"startNode\":\"22\",\"endNode\":\"7\",\"properties\":{}}" +
                    "] " +
                    "} }",
            "{\"graph\": { " +
                    "\"nodes\" :[ " +
                    "{\"id\" : \"22\",\"labels\" : [ \"Teacher\"], \"properties\" : { \"name\" :\"Miss Young\" } }, " +
                    "{\"id\" : \"8\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"Geography\" } }" +
                    "], " +
                    "\"relationships\": [" +
                    "{\"id\":\"228\",\"type\":\"TEACHES\",\"startNode\":\"22\",\"endNode\":\"8\",\"properties\":{}}" +
                    "] " +
                    "} }"

    };


}
