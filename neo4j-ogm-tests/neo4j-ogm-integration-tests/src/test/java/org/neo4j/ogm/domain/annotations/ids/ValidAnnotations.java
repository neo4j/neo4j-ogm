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
package org.neo4j.ogm.domain.annotations.ids;

import java.util.UUID;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.id.UuidStrategy;
import org.neo4j.ogm.metadata.IdGenerationTest;
import org.neo4j.ogm.metadata.IdGenerationTest.CustomIdStrategy;
import org.neo4j.ogm.typeconversion.UuidStringConverter;

public class ValidAnnotations {

    public static class WithoutId {
        //@Id not implemented yet
        @Id
        public String identifier;
    }

    public static class InternalId {
        public Long id;
    }

    public static class InternalIdWithAnnotation {
        @Id @GeneratedValue public Long identifier;
    }

    public static class Basic {
        public Long id;
        @Id public String identifier;
    }

    public static class IdAndGenerationType {
        public Long id;
        @Id @GeneratedValue(strategy = UuidStrategy.class)
        public String identifier;
    }

    public static class UuidIdAndGenerationType {
        public Long id;
        @Id @GeneratedValue(strategy = UuidStrategy.class)
        @Convert(UuidStringConverter.class)
        public UUID identifier;
    }

    public static class UuidIdAndGenerationTypeWithoutIdAttribute {
        @Id @GeneratedValue(strategy = UuidStrategy.class)
        @Convert(UuidStringConverter.class)
        public UUID identifier;
    }

    public static class UuidAndGenerationType {
        @Id @GeneratedValue(strategy = UuidStrategy.class)
        @Convert(UuidStringConverter.class)
        public UUID identifier;
    }

    public static class BasicChild extends Basic {
    }

    public static class WithCustomIdStrategy {

        public Long id;

        @Id @GeneratedValue(strategy = CustomIdStrategy.class)
        public String identifier;
    }

    public static class WithCustomInstanceIdStrategy {

        public Long id;

        @Id @GeneratedValue(strategy = IdGenerationTest.CustomInstanceIdStrategy.class)
        public String identifier;
    }

    @RelationshipEntity(type = "REL")
    public static class RelationshipEntityWithId {

        Long id;

        @Id
        @GeneratedValue(strategy = UuidStrategy.class)
        public String uuid;

        @StartNode
        public IdAndGenerationType startNode;

        @EndNode
        public IdAndGenerationType endNode;

        public int value;

        public RelationshipEntityWithId() {
        }

        public RelationshipEntityWithId(IdAndGenerationType startNode, IdAndGenerationType endNode, int value) {
            this.startNode = startNode;
            this.endNode = endNode;
            this.value = value;
        }
    }
}
