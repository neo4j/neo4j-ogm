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
package org.neo4j.ogm.config;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

enum Drivers {

    BOLT("org.neo4j.ogm.drivers.bolt.driver.BoltDriver", "bolt", "bolt+routing"),
    EMBEDDED("org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver", "file"),
    HTTP("org.neo4j.ogm.drivers.http.driver.HttpDriver", "http", "https");

    private static final String SUPPORTED_SCHEMES = Stream.of(Drivers.values())
        .flatMap(driver -> Arrays.stream(driver.schemes))
        .collect(Collectors.joining(", "));

    private static final String UNSUPPORTED_SCHEME_MESSAGE = "A URI Scheme must be one of: " + SUPPORTED_SCHEMES + ".";
    private final String[] schemes;
    private final String driverClassName;

    Drivers(String driverClassName, String... schemes) {
        this.schemes = schemes;
        this.driverClassName = driverClassName;
    }

    static Drivers getDriverFor(String scheme) {
        for (Drivers driver : Drivers.values()) {
            for (String supportedScheme : driver.schemes) {
                if (supportedScheme.equalsIgnoreCase(scheme)) {
                    return driver;
                }
            }
        }
        throw new RuntimeException(UNSUPPORTED_SCHEME_MESSAGE);
    }

    String driverClassName() {
        return driverClassName;
    }
}
