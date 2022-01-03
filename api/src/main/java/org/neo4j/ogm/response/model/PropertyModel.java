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
package org.neo4j.ogm.response.model;

import org.neo4j.ogm.model.Property;

/**
 * @author Michal Bachman
 */
public class PropertyModel<K, V> implements Property<K, V> {

    private K key;
    private V value;

    /**
     * Constructs a new {@link PropertyModel} inferring the generic type arguments of the key and the value.
     *
     * @param key   The property key or name
     * @param value The property value
     * @return A new {@link PropertyModel} based on the given arguments
     */
    public static <K, V> PropertyModel<K, V> with(K key, V value) {
        return new PropertyModel<>(key, value);
    }

    public PropertyModel(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public String toString() {
        return String.format("%s : %s", this.key, asParameter());
    }

    public Object asParameter() {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return value;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e1) {
            try {
                return Double.parseDouble(value.toString());
            } catch (Exception e2) {
                return value.toString();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PropertyModel property = (PropertyModel) o;

        return key.equals(property.key) && value.equals(property.value);
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}
