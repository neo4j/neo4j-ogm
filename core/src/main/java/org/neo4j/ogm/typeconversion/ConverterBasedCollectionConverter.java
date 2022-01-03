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
