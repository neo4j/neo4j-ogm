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
package org.neo4j.ogm.drivers.embedded.types;

import static java.util.stream.Collectors.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.neo4j.ogm.driver.ParameterConversion;
import org.neo4j.ogm.driver.TypeSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This conversion mode first tries to map all parameters to a {@link org.neo4j.values.storable.Value}. This method returns
 * null if a conversion is not possible. For those parameters, the default fallback is used.
 *
 * @author Michael J. Simons
 * @author Gerrit Meier
 */
class EmbeddedBasedParameterConversion implements ParameterConversion {

    private final static Logger LOGGER = LoggerFactory.getLogger(EmbeddedBasedParameterConversion.class);

    private final ParameterConversion fallback = DefaultParameterConversion.INSTANCE;

    private final TypeSystem typeSystem;

    private Predicate<Entry<String, Object>> canStore;

    EmbeddedBasedParameterConversion(TypeSystem typeSystem) {

        // The infrastructure for the kernel based value utils is available since 3.3.x only.
        try {
            String fqnOfValues = "org.neo4j.values.storable.Values";
            Method unsafeOf = Class.forName(fqnOfValues)
                .getDeclaredMethod("unsafeOf", Object.class, boolean.class);

            this.canStore = new WrappedValuesUnsafeOf(unsafeOf);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            LOGGER.warn("Cannot use native type conversion prior to Neo4j 3.3.x");
            canStore = anyObject -> false;
        }

        this.typeSystem = typeSystem;
    }

    @Override
    public Map<String, Object> convertParameters(Map<String, Object> originalParameter) {
        Map<Boolean, Map<String, Object>> allParameters = originalParameter.entrySet().stream()
            // Convert to native first if possible
            .collect(toMap(
                Entry::getKey,
                e -> {
                    Object v = e.getValue();
                    if (v == null) {
                        return v;
                    }
                    return typeSystem.getMappedToNativeTypeAdapter(v.getClass()).apply(v);
                }))
            .entrySet().stream()
            // Then partition by whether be able to store or not
            .collect(partitioningBy(canStore, Collectors.toMap(Entry::getKey, Entry::getValue)));

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
