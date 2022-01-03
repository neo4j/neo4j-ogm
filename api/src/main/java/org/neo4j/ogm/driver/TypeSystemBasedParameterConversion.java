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
package org.neo4j.ogm.driver;

import static java.util.stream.Collectors.*;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * This conversion mode first tries to map all parameters to a FOOBAR  and uses them directly. For all non supported
 * object types, it falls back to the default object mapper based conversion.
 *
 * @author Michael J. Simons
 * @author Gerrit Meier
 */
class TypeSystemBasedParameterConversion implements ParameterConversion {

    private final ParameterConversion fallback = DefaultParameterConversion.INSTANCE;

    private final TypeSystem typeSystem;

    TypeSystemBasedParameterConversion(TypeSystem typeSystem) {
        this.typeSystem = typeSystem;
    }

    @Override
    public Map<String, Object> convertParameters(Map<String, Object> originalParameter) {

        final Map<String, Object> convertedParameter = new HashMap<>(originalParameter.size());
        final Map<String, Object> unconvertedParameter = new HashMap<>(originalParameter.size());

        originalParameter.forEach((parameterKey, unconvertedValue) -> {

            if (unconvertedValue == null) {
                convertedParameter.put(parameterKey, null);
            } else if (unconvertedValue instanceof List) {
                convertedParameter.put(parameterKey, convertListItems((List) unconvertedValue));
            } else if (unconvertedValue.getClass().isArray()) {
                convertedParameter.put(parameterKey, convertArrayItems(unconvertedValue));
            } else if (unconvertedValue instanceof Map) {
                convertedParameter.put(parameterKey, convertParameters((Map<String, Object>) unconvertedValue));
            } else if (typeSystem.supportsAsNativeType(unconvertedValue.getClass())) {
                Object convertedValue = typeSystem.getMappedToNativeTypeAdapter(unconvertedValue.getClass())
                    .apply(unconvertedValue);
                convertedParameter.put(parameterKey, convertedValue);
            } else {
                unconvertedParameter.put(parameterKey, unconvertedValue);
            }
        });

        convertedParameter.putAll(fallback.convertParameters(unconvertedParameter));
        return convertedParameter;
    }

    private List<?> convertListItems(List<?> unconvertedValues) {

        return unconvertedValues.stream().map(this::convertSingle).collect(toList());
    }

    private Object[] convertArrayItems(Object unconvertedValues) {

        int length = Array.getLength(unconvertedValues);
        Object[] convertedValues = new Object[length];

        for (int i = 0; i < length; ++i) {
            convertedValues[i] = convertSingle(Array.get(unconvertedValues, i));
        }

        return convertedValues;
    }

    private Object convertSingle(Object value) {
        Predicate<Class> isSupportedNativeCollection = c -> List.class.isAssignableFrom(c) || Map.class.isAssignableFrom(c);

        if (isSupportedNativeCollection.negate().and(typeSystem::supportsAsNativeType).test(value.getClass())) {
            return typeSystem.getMappedToNativeTypeAdapter(value.getClass()).apply(value);
        } else {
            String fixedKey = "u";
            return convertParameters(Collections.singletonMap(fixedKey, value)).get(fixedKey);
        }
    }
}
