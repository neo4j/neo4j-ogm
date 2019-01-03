/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */
package org.neo4j.ogm.driver;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.config.ObjectMapperFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Abstraction over the parameter conversion.
 *
 * @author Michael J. Simons
 */
public interface ParameterConversion {

    Map<String, Object> convertParameters(Map<String, Object> originalParameter);

    /**
     * The "old" way of converting things. Based on Jackson's Object Mapper.
     */
    enum DefaultParameterConversion implements ParameterConversion {

        INSTANCE;

        private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.objectMapper();
        private static final TypeReference<HashMap<String, Object>> MAP_TYPE_REF = new TypeReference<HashMap<String, Object>>() {
        };

        @Override
        public Map<String, Object> convertParameters(final Map<String, Object> originalParameter) {
            return OBJECT_MAPPER.convertValue(originalParameter, MAP_TYPE_REF);
        }
    }

}
