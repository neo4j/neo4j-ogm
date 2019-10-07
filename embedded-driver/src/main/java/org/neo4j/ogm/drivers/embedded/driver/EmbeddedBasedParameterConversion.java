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
package org.neo4j.ogm.drivers.embedded.driver;

import static java.util.stream.Collectors.*;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.neo4j.ogm.driver.AbstractConfigurableDriver;
import org.neo4j.ogm.driver.ParameterConversion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This conversion mode first tries to map all parameters to a {@link org.neo4j.values.storable.Value}. This method returns
 * null if a conversion is not possible. For those parameters, the default fallback is used.
 *
 * @author Michael J. Simons
 */
enum EmbeddedBasedParameterConversion implements ParameterConversion {

    INSTANCE;

    private final Logger logger = LoggerFactory.getLogger(EmbeddedBasedParameterConversion.class);
    private final ParameterConversion fallback = AbstractConfigurableDriver.CONVERT_ALL_PARAMETERS_CONVERSION;

    private Predicate<Object> canConvert;

    EmbeddedBasedParameterConversion() {

        // The infrastructure for the kernel based value utils is available since 3.3.x only.
        try {
            String fqnOfValues = "org.neo4j.values.storable.Values";
            Method unsafeOf = Class.forName(fqnOfValues)
                .getDeclaredMethod("unsafeOf", Object.class, boolean.class);

            this.canConvert = new WrappedValuesUnsafeOf(unsafeOf);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            logger.warn("Cannot use native type conversion prior to Neo4j 3.3.x");
            canConvert = anyObject -> false;
        }
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
            } else if (canConvert.test(unconvertedValue)) {
                convertedParameter.put(parameterKey, unconvertedValue);
            } else {
                System.out.println("putting "+ unconvertedValue);
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

        if (isSupportedNativeCollection.negate().test(value.getClass()) && canConvert.test(value)) {
            return value;
        } else {
            String fixedKey = "u";
            return convertParameters(Collections.singletonMap(fixedKey, value)).get(fixedKey);
        }
    }

    private static class WrappedValuesUnsafeOf implements Predicate<Object> {
        private final Method unsafeOf;

        WrappedValuesUnsafeOf(Method unsafeOf) {
            this.unsafeOf = unsafeOf;
        }

        @Override
        public boolean test(Object o) {
            try {
                return this.unsafeOf.invoke(null, o, true) != null;
            } catch (IllegalAccessException | InvocationTargetException e) {
                // InvocationTargetException is ignored on purpose, just try the default fallback then.
            }
            return false;
        }
    }
}
