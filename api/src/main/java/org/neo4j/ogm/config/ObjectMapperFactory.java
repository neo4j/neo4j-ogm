/*
 * Copyright (c) 2002-2019 "Neo4j,"
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

import org.neo4j.ogm.response.model.NodeModel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Produces a singleton ObjectMapper
 *
 * @author Luanne Misquitta
 */
public class ObjectMapperFactory {

    private static final JsonFactory jsonFactory = new JsonFactory();
    private static final ObjectMapper mapper = new ObjectMapper(jsonFactory)
        .registerModule(new Neo4jOgmJacksonModule())
        .configure(DeserializationFeature.USE_LONG_FOR_INTS, true);

    public static ObjectMapper objectMapper() {
        return mapper;
    }

    public static JsonFactory jsonFactory() {
        return jsonFactory;
    }

    abstract static class NodeModelMixin {
        @JsonCreator
        public NodeModelMixin(@JsonProperty("id") Long id) {
        }
    }

    static class Neo4jOgmJacksonModule extends SimpleModule {

        Neo4jOgmJacksonModule() {
        }

        @Override
        public void setupModule(SetupContext context) {
            context.setMixInAnnotations(NodeModel.class, NodeModelMixin.class);
        }
    }

    private ObjectMapperFactory() {
    }
}
