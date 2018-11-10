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
package org.neo4j.ogm.drivers.bolt.driver;

import static org.neo4j.ogm.drivers.bolt.driver.BoltDriver.*;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.ogm.driver.AbstractConfigurableDriver;
import org.neo4j.ogm.driver.ParameterConversion;

/**
 * This conversion mode first tries to map all parameters to a {@link Value} and uses them directly. For all non supported
 * object types, it falls back to the default object mapper based conversion.
 *
 * @author Michael J. Simons
 */
enum JavaDriverBasedParameterConversion implements ParameterConversion {

    INSTANCE;

    private final ParameterConversion fallback = AbstractConfigurableDriver.CONVERT_ALL_PARAMETERS_CONVERSION;

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
            } else if (NATIVE_TYPES.supportsAsNativeType(unconvertedValue.getClass())) {
                Object convertedValue = NATIVE_TYPES.getMappedToNativeTypeAdapter(unconvertedParameter.getClass())
                    .apply(unconvertedParameter);
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
