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
