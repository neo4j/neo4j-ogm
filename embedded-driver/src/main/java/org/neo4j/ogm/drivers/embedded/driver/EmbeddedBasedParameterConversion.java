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
package org.neo4j.ogm.drivers.embedded.driver;

import static java.util.stream.Collectors.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
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

    private Predicate<Entry<String, Object>> canConvert;

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

        Map<Boolean, Map<String, Object>> allParameters = originalParameter.entrySet().stream()
            .collect(partitioningBy(canConvert, Collectors.toMap(Entry::getKey, Entry::getValue)));

        Map<String, Object> convertedParameters = new HashMap<>(originalParameter.size());
        convertedParameters.putAll(allParameters.get(true));
        convertedParameters.putAll(fallback.convertParameters(allParameters.get(false)));

        return convertedParameters;
    }

    private static class WrappedValuesUnsafeOf implements Predicate<Entry<String, Object>> {
        private final Method unsafeOf;

        WrappedValuesUnsafeOf(Method unsafeOf) {
            this.unsafeOf = unsafeOf;
        }

        @Override
        public boolean test(Entry<String, Object> o) {
            try {
                return this.unsafeOf.invoke(null, o.getValue(), true) != null;
            } catch (IllegalAccessException | InvocationTargetException e) {
                // InvocationTargetException is ignored on purpose, just try the default fallback then.
            }
            return false;
        }
    }
}
