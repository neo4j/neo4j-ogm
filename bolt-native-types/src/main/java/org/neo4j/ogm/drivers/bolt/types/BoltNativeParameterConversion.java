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
package org.neo4j.ogm.drivers.bolt.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.ogm.driver.ParameterConversion;
import org.neo4j.ogm.driver.TypeSystem;

/**
 * This conversion mode first tries to map all parameters to a {@link Value} and uses them directly. For all non supported
 * object types, it falls back to the default object mapper based conversion.
 *
 * @author Michael J. Simons
 * @author Gerrit Meier
 */
class BoltNativeParameterConversion implements ParameterConversion {

    private final ParameterConversion fallback = DefaultParameterConversion.INSTANCE;

    private final TypeSystem typeSystem;

    BoltNativeParameterConversion(TypeSystem typeSystem) {
        this.typeSystem = typeSystem;
    }

    @Override
    public Map<String, Object> convertParameters(Map<String, Object> originalParameter) {
        return convertParametersImpl(originalParameter);
    }

    private Map<String, Object> convertParametersImpl(Map<String, Object> originalParameter) {
        final Map<String, Object> convertedParameter = new HashMap<>(originalParameter.size());
        final Map<String, Object> unconvertedParameter = new HashMap<>(originalParameter.size());

        originalParameter.forEach((parameterKey, unconvertedValue) -> {

            if (unconvertedValue == null) {
                convertedParameter.put(parameterKey, null);
            } else if (unconvertedValue instanceof List) {
                for (Object value : (List<Object>) unconvertedValue) {
                    if (value instanceof Map) {
                        convertedParameter.put(parameterKey, convertParametersImpl((Map<String, Object>) value));
                    } else {
                        Value convertedValue = Values.value(unconvertedValue);
                        convertedParameter.put(parameterKey, convertedValue);
                    }
                }
            } else if (unconvertedValue instanceof Map) {
                convertedParameter.put(parameterKey, convertParametersImpl((Map<String, Object>) unconvertedValue));
            } else if (typeSystem.supportsAsNativeType(unconvertedValue.getClass())) {
                Object convertedValue = typeSystem.getMappedToNativeTypeAdapter(unconvertedValue.getClass())
                    .apply(unconvertedValue);
                convertedParameter.put(parameterKey, convertedValue);
            } else {
                try {
                    Value convertedValue = Values.value(unconvertedValue);
                    convertedParameter.put(parameterKey, convertedValue);
                } catch (ClientException e) {
                    unconvertedParameter.put(parameterKey, unconvertedValue);
                }
            }
        });

        convertedParameter.putAll(fallback.convertParameters(unconvertedParameter));
        return convertedParameter;
    }
}
