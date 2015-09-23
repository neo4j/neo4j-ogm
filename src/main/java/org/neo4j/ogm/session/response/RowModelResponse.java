/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.session.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.neo4j.ogm.session.response.model.RowModel;

/**
 * A Response containing RowModel objects
 *
 * These responses should be identical which driver is providing them
 *
 * @author Vince Bickers
 */
public class RowModelResponse implements Response<RowModel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RowModelResponse.class);

    private final ObjectMapper objectMapper;
    private final Response<String> response;

    public RowModelResponse(Response<String> response, ObjectMapper mapper) {
        this.response = response;
        this.objectMapper = mapper;
        expect(ResponseRecord.ROW);
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
    public void expect(ResponseRecord record) {
        response.expect(record);
    }

    @Override
    public String[] columns() {
        return response.columns();
    }

    @Override
    public int rowId() {
        return response.rowId();
    }

    /**
     * @author Vince Bickers
     */
    public static class RowModelResult {

        private Object[] row;

        public Object[] getRow() {
            return row;
        }

        @SuppressWarnings("UnusedDeclaration")
        public void setRow(Object[] rowModel) {
            this.row = rowModel;
        }

    }
}
