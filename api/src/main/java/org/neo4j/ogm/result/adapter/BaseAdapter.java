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
package org.neo4j.ogm.result.adapter;

import static java.util.stream.Collectors.*;

import java.util.Map;

import org.neo4j.ogm.support.CollectionUtils;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class BaseAdapter {

    public Map<String, Object> convertArrayPropertiesToCollection(Map<String, Object> properties) {
        return properties.entrySet().stream()
            .filter(e -> e.getValue() != null)
            .collect(toMap(Map.Entry::getKey, BaseAdapter::convertOrReturnSelf));
    }

    private static Object convertOrReturnSelf(Map.Entry<String, Object> entry) {

        Object entryValue = entry.getValue();
        if (entryValue != null && entryValue.getClass().isArray()) {
            return CollectionUtils.materializeIterableIf(CollectionUtils.iterableOf(entryValue));
        } else {
            return entryValue;
        }
    }
}
