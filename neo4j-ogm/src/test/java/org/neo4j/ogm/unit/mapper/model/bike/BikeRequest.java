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

package org.neo4j.ogm.unit.mapper.model.bike;

import org.neo4j.ogm.RequestProxy;

public class BikeRequest extends RequestProxy {

    public String[] getResponse() {
        return jsonModel;
    }

    private static String[] jsonModel = {
            "{\"graph\": { " +
                "\"nodes\" :[ " +
                    "{\"id\" : \"15\",\"labels\" : [ \"Bike\"], \"properties\" : { \"colours\" :[\"red\", \"black\"] } }, " +
                    "{\"id\" : \"16\",\"labels\" : [ \"Wheel\", \"FrontWheel\" ],\"properties\" : {\"spokes\" : 3 } }, " +
                    "{\"id\" : \"17\",\"labels\" : [ \"Wheel\", \"BackWheel\" ],\"properties\" : {\"spokes\" : 5 } }, " +
                    "{\"id\" : \"18\",\"labels\" : [ \"Frame\" ],\"properties\" : {\"size\" : 27 } }, " +
                    "{\"id\" : \"19\",\"labels\" : [ \"Saddle\" ],\"properties\" : {\"price\" : 42.99, \"material\" : \"plastic\" } } " +
                    "], " +
                "\"relationships\": [" +
                    "{\"id\":\"141\",\"type\":\"HAS_WHEEL\",\"startNode\":\"15\",\"endNode\":\"16\",\"properties\":{ \"purchased\" : 20130917 }}, " +
                    "{\"id\":\"142\",\"type\":\"HAS_WHEEL\",\"startNode\":\"15\",\"endNode\":\"17\",\"properties\":{ \"purchased\" : 20130917 }}," +
                    "{\"id\":\"143\",\"type\":\"HAS_FRAME\",\"startNode\":\"15\",\"endNode\":\"18\",\"properties\":{ \"purchased\" : 20130917 }}," +
                    "{\"id\":\"144\",\"type\":\"HAS_SADDLE\",\"startNode\":\"15\",\"endNode\":\"19\",\"properties\":{\"purchased\" : 20130922 }} " +
                    "] " +
            "} }"
    };
}
