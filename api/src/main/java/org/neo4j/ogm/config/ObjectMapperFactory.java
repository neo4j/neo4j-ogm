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
package org.neo4j.ogm.config;

import org.neo4j.ogm.response.model.DefaultGraphModel;
import org.neo4j.ogm.response.model.DefaultGraphRowModel;
import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.response.model.QueryStatisticsModel;
import org.neo4j.ogm.response.model.RelationshipModel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Produces a singleton ObjectMapper
 *
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public final class ObjectMapperFactory {

    private static final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new Neo4jOgmJacksonModule())
        .registerModule(new Jdk8Module())
        .registerModule(new JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.USE_LONG_FOR_INTS, true);

    public static ObjectMapper objectMapper() {
        return mapper;
    }

    /**
     * Mixin needed to specify the required constructor argument of the {@link NodeModel NodeModel} class.
     */
    abstract static class NodeModelMixin {
        @JsonCreator NodeModelMixin(@JsonProperty("id") Long id) {
        }
    }

    /**
     * The {@link DefaultGraphModel DefaultGraphModels} setter are actually methods that add the the list of existing
     * nodes- and relationshipmodels. They are used when the model is manually build. Without this mixin, they would
     * need to be called {@code setXXX}, which is totally misleading.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    abstract static class DefaultGraphModelMixin {
        @JsonSetter("nodes")
        public void addNodes(NodeModel[] additionalNodes) {
        }

        @JsonSetter("relationships")
        public void addRelationships(RelationshipModel[] additionalRelationships) {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    abstract static class DefaultGraphRowModelMixin {
        @JsonCreator DefaultGraphRowModelMixin(@JsonProperty("graph") DefaultGraphModel graph,
            @JsonProperty("row") Object[] row) {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    abstract static class IgnoreUnknownPropertiesMixin {
    }

    static class Neo4jOgmJacksonModule extends SimpleModule {

        Neo4jOgmJacksonModule() {
        }

        @Override
        public void setupModule(SetupContext context) {
            context.setMixInAnnotations(NodeModel.class, NodeModelMixin.class);
            context.setMixInAnnotations(DefaultGraphModel.class, DefaultGraphModelMixin.class);
            context.setMixInAnnotations(DefaultGraphRowModel.class, DefaultGraphRowModelMixin.class);
            context.setMixInAnnotations(QueryStatisticsModel.class, IgnoreUnknownPropertiesMixin.class);
        }
    }

    private ObjectMapperFactory() {
    }
}
