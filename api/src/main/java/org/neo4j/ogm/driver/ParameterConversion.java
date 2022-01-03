/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
