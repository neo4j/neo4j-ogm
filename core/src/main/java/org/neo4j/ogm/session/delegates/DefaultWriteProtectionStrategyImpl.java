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
package org.neo4j.ogm.session.delegates;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.neo4j.ogm.context.WriteProtectionTarget;
import org.neo4j.ogm.session.WriteProtectionStrategy;

/**
 * Map based write protection strategy. Does only consider the mode for retrieving predicates to protect entities.
 *
 * @author Michael J. Simons
 */
class DefaultWriteProtectionStrategyImpl implements WriteProtectionStrategy {
    private final Map<WriteProtectionTarget, Predicate<Object>> writeProtectionPredicates = new HashMap<>();

    @Override
    public BiFunction<WriteProtectionTarget, Class<?>, Predicate<Object>> get() {
        return (mode, targetEntity) -> this.writeProtectionPredicates.getOrDefault(mode, t -> false);
    }

    void addProtection(WriteProtectionTarget key, Predicate<Object> value) {
        writeProtectionPredicates.put(key, value);
    }

    void removeProtection(WriteProtectionTarget key) {
        writeProtectionPredicates.remove(key);
    }

    boolean isEmpty() {
        return writeProtectionPredicates.isEmpty();
    }
}
