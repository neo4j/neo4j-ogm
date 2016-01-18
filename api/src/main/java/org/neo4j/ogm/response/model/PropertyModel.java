/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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
     * @param key The property key or name
     * @param value The property value
     * @return A new {@link PropertyModel} based on the given arguments
     */
    public static <K, V> PropertyModel<K, V> with(K key, V value) {
        return new PropertyModel<>(key, value);
    }

    public PropertyModel() {}

    public PropertyModel(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

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
