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
import org.neo4j.ogm.session.result.RowModel;
import org.neo4j.ogm.session.result.RowModelResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RowModelResponse implements Neo4jResponse<RowModel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RowModelResponse.class);

    private final ObjectMapper objectMapper;
    private final Neo4jResponse<String> response;

    public RowModelResponse(Neo4jResponse<String> response, ObjectMapper mapper) {
        this.response = response;
        this.objectMapper = mapper;
        initialiseScan("row");
    }

    @Override
    public RowModel next() {
        String json = response.next();
        if (json != null) {
            try {
                return new RowModel(objectMapper.readValue(json, RowModelResult.class).getRow());
            } catch (Exception e) {
                LOGGER.error("failed to parse: " + json);
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }

    }

    @Override
    public void close() {
        response.close();
    }

    @Override
    public void initialiseScan(String token) {
        response.initialiseScan(token);
    }

    @Override
    public String[] columns() {
        return response.columns();
    }

    @Override
    public int rowId() {
        return response.rowId();
    }
}
