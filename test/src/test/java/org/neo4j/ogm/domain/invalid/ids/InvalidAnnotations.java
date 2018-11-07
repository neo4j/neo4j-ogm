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

package org.neo4j.ogm.domain.invalid.ids;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.domain.annotations.ids.ValidAnnotations;
import org.neo4j.ogm.id.UuidStrategy;

public class InvalidAnnotations {

    public static class TwoIdsOnSameClass {
        Long id;

        @Id public String identifier;
        @Id public String identifier2;
    }

    public static class NeitherGraphIdOrId {
        public String property;
    }

    public static class BothIdAndPrimaryIndexOnDifferentProperty {
        @Id public String identifier;
        @Index(primary = true, unique = true) public String other;
    }

    public static class ChildHasPrimaryIndexExtendsAndParentHasId extends ValidAnnotations.Basic {
        @Index(primary = true, unique = true) public String other;
    }

    public static class GeneratedValueWithoutID {
        Long id;

        @GeneratedValue public String identifier;
    }

    public static class UuidGenerationStrategyWithIdTypeNotUuid {
        @Id
        @GeneratedValue(strategy = UuidStrategy.class)
        public int identifier;
    }
}
