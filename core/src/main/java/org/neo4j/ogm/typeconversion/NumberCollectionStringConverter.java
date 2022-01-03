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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * The NumberStringConverter can be used to convert any java object collection containing values that extend
 * {@link java.lang.Number} to and from its String array representation.
 * By default, the OGM will automatically convert Collections of {@link java.math.BigInteger}
 * and {@link java.math.BigDecimal} entity attributes using this converter.
 *
 * @author Luanne Misquitta
 * @author Róbert Papp
 */
public class NumberCollectionStringConverter implements AttributeConverter<Collection<Number>, String[]> {

    private final Class<? extends Number> numberClass;
    private final Class<? extends Collection> collectionClass;

    public NumberCollectionStringConverter(Class<? extends Number> numberClass,
        Class<? extends Collection> collectionClass) {
        this.numberClass = numberClass;
        this.collectionClass = collectionClass;
    }

    @Override
    public String[] toGraphProperty(Collection<Number> value) {
        if (value == null) {
            return null;
        }
        String[] values = new String[(value.size())];

        int i = 0;
        for (Number num : value) {
            values[i++] = num.toString();
        }
        return values;
    }

    @Override
    public Collection<Number> toEntityAttribute(String[] stringValues) {
        if (stringValues == null) {
            return null;
        }
        Collection<Number> values;

        if (List.class.isAssignableFrom(collectionClass)) {
            values = new ArrayList<>(stringValues.length);
        } else if (Vector.class.isAssignableFrom(collectionClass)) {
            values = new Vector<>(stringValues.length);
        } else if (Set.class.isAssignableFrom(collectionClass)) {
            values = new HashSet<>(stringValues.length);
        } else {
            return null;
        }
        try {
            for (String value : stringValues) {
                values.add(numberClass.getDeclaredConstructor(String.class).newInstance(value));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return values;
    }
}
