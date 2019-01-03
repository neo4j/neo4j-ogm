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
package org.neo4j.ogm.result.adapter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Frantisek Hartman
 */
public class BaseAdapter {

    public Map<String, Object> convertArrayPropertiesToIterable(Map<String, Object> properties) {
        Map<String, Object> props = new HashMap<>();
        for (String k : properties.keySet()) {
            Object v = properties.get(k);
            if (v.getClass().isArray()) {
                props.put(k, AdapterUtils.convertToIterable(v));
            } else {
                props.put(k, v);
            }
        }
        return props;
    }
}
