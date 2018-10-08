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
