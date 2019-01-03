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
package org.neo4j.ogm.driver;

import org.neo4j.ogm.config.ObjectMapperFactory;

/**
 * Custom configuration mode for controlling parameter conversion in custom Cypher queries.
 *
 * @author Michael J. Simons
 */
public enum ParameterConversionMode {
    /**
     * Convert all parameters to custom queries via Jacksons Object Mapper. The Object Mapper can be customized by registering custom modules
     * to {@link ObjectMapperFactory#objectMapper()}.
     */
    CONVERT_ALL,

    /**
     * Convert only non-native parameters and use the Java-Driver only. Has only effect on the bolt-transport.
     */
    CONVERT_NON_NATIVE_ONLY;

    public static final String CONFIG_PARAMETER_CONVERSION_MODE = ParameterConversionMode.class.getName();
}
