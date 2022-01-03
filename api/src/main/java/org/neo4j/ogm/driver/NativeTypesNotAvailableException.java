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
