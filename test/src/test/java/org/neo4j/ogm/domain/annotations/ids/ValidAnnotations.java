/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

    public static class BasicChild extends Basic {
    }

    public static class WithCustomIdStrategy {

        public Long id;

        @Id @GeneratedValue(strategy = CustomIdStrategy.class)
        public String idetifier;
    }

    public static class WithCustomInstanceIdStrategy {

        public Long id;

        @Id @GeneratedValue(strategy = IdGenerationTest.CustomInstanceIdStrategy.class)
        public String idetifier;
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
