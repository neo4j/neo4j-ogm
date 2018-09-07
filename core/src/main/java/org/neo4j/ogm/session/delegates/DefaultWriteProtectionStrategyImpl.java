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

package org.neo4j.ogm.session.delegates;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.neo4j.ogm.context.WriteProtectionMode;

/**
 * Map based write protection strategy. Does only consider the mode for retrieving predicates to protect entities.
 *
 * @author Michael J. Simons
 */
class DefaultWriteProtectionStrategyImpl implements WriteProtectionStrategy {
    private final Map<WriteProtectionMode, Predicate<Object>> writeProtectionPredicates = new HashMap<>();

    @Override
    public BiFunction<WriteProtectionMode, Object, Predicate<Object>> get() {
        return (mode, targetEntity) -> this.writeProtectionPredicates.getOrDefault(mode, t -> false);
    }

    void addProtection(WriteProtectionMode key, Predicate<Object> value) {
        writeProtectionPredicates.put(key, value);
    }

    void removeProtection(WriteProtectionMode key) {
        writeProtectionPredicates.remove(key);
    }
}
