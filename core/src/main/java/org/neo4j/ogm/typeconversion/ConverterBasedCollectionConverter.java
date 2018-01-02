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

package org.neo4j.ogm.typeconversion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.neo4j.ogm.exception.core.MappingException;

/**
 * @author Frantisek Hartman
 */
public class ConverterBasedCollectionConverter<T, F> implements AttributeConverter<Collection<T>, F[]> {

    private final Class<?> collectionClass;
    private final AttributeConverter<T, F> converter;

    public ConverterBasedCollectionConverter(Class<?> collectionClass, AttributeConverter<T, F> converter) {
        this.collectionClass = collectionClass;
        this.converter = converter;
    }

    @Override
    public F[] toGraphProperty(Collection<T> values) {
        if (values == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        F[] result = (F[]) new Object[values.size()];

        int i = 0;
        for (T value : values) {
            result[i++] = converter.toGraphProperty(value);
        }
        return result;
    }

    @Override
    public Collection<T> toEntityAttribute(F[] values) {
        if (values == null) {
            return null;
        }

        Collection<T> result;
        if (List.class.isAssignableFrom(collectionClass)) {
            result = new ArrayList<>(values.length);
        } else if (Vector.class.isAssignableFrom(collectionClass)) {
            result = new Vector<>(values.length);
        } else if (Set.class.isAssignableFrom(collectionClass)) {
            result = new HashSet<>(values.length);
        } else {
            return null;
        }
        try {
            for (F value : values) {
                result.add(converter.toEntityAttribute(value));
            }
        } catch (Exception e) {
            throw new MappingException("Could not map array of values " + Arrays.toString(values) +
                " to collection of type " + collectionClass, e);
        }
        return result;
    }
}
