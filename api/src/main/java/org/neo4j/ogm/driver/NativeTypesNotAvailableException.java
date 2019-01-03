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

/**
 * Exception indicating that the selected driver does not support native types.
 *
 * @author Michael J. Simons
 * @since 3.2
 */
public class NativeTypesNotAvailableException extends NativeTypesException {

    /**
     * The name of the module containing the native types for the driver.
     */
    private final String requiredModule;

    NativeTypesNotAvailableException(String driverClassName) {

        super(driverClassName, "Cannot use native types. Make sure you have the native module for your driver on the classpath.");

        switch (this.driverClassName) {
            case "org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver":
                this.requiredModule = "org.neo4j:neo4j-ogm-embedded-native-types";
                break;
            case "org.neo4j.ogm.drivers.bolt.driver.BoltDriver":
                this.requiredModule = "org.neo4j:neo4j-ogm-bolt-native-types";
                break;
            default:
                this.requiredModule = "n/a";
        }
    }

    public String getRequiredModule() {
        return requiredModule;
    }
}
