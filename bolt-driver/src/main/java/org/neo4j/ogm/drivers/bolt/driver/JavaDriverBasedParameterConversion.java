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
package org.neo4j.ogm.drivers.bolt.driver;

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

        final Map<String, Object> convertedParameter = new HashMap<>(originalParameter.size());
        final Map<String, Object> unconvertedParameter = new HashMap<>(originalParameter.size());

        originalParameter.forEach((parameterKey, unconvertedValue) -> {
            try {
                Value convertedValue = Values.value(unconvertedValue);
                convertedParameter.put(parameterKey, convertedValue);
            } catch (ClientException e) {
                unconvertedParameter.put(parameterKey, unconvertedValue);
            }
        });

        convertedParameter.putAll(fallback.convertParameters(unconvertedParameter));
        return convertedParameter;
    }
}
