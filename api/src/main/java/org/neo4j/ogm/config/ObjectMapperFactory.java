/*
 * Copyright (c) 2002-2020 "Neo4j,"
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

package org.neo4j.ogm.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Produces a singleton ObjectMapper
 *
 * @author Luanne Misquitta
 */
public class ObjectMapperFactory {

    private static final JsonFactory jsonFactory = new JsonFactory();
    private static final ObjectMapper mapper = new ObjectMapper(jsonFactory)
        .configure(DeserializationFeature.USE_LONG_FOR_INTS, true);

    private ObjectMapperFactory() {

    }

    public static ObjectMapper objectMapper() {
        return mapper;
    }

    public static JsonFactory jsonFactory() {
        return jsonFactory;
    }
}
