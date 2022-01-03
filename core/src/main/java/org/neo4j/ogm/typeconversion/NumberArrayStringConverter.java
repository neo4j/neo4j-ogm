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

import java.lang.reflect.Array;

/**
 * The NumberStringConverter can be used to convert any java object array containing values that extend
 * {@link java.lang.Number} to and from its String array representation.
 * By default, the OGM will automatically convert arrays of {@link java.math.BigInteger}
 * and {@link java.math.BigDecimal} entity attributes using this converter.
 *
 * @author Luanne Misquitta
 * @author Róbert Papp
 */
public class NumberArrayStringConverter implements AttributeConverter<Number[], String[]> {

    private final Class<? extends Number> numberClass;

    public NumberArrayStringConverter(Class<? extends Number> numberClass) {
        this.numberClass = numberClass;
    }

    @Override
    public String[] toGraphProperty(Number[] value) {
        if (value == null) {
            return null;
        }
        String[] values = new String[(value.length)];

        int i = 0;
        for (Number num : value) {
            values[i++] = num.toString();
        }
        return values;
    }

    @Override
    public Number[] toEntityAttribute(String[] stringValues) {
        if (stringValues == null) {
            return null;
        }
        Number[] values = (Number[]) Array.newInstance(numberClass, stringValues.length);

        int i = 0;
        try {
            for (String num : stringValues) {
                values[i++] = numberClass.getDeclaredConstructor(String.class).newInstance(num);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return values;
    }
}
