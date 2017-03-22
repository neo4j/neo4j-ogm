/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for ensuring that the various pluggable components
 * required by the OGM can be loaded.
 * The Components class can be explicitly configured via an {@link Configuration} instance.
 * If no explicit configuration is supplied, the class will attempt to auto-register.
 * Auto-configuration is accomplished using a properties file. By default, this file
 * is called "ogm.properties" and it must be available on the class path.
 * You can supply a different configuration properties file, by specifying a system property
 * "ogm.properties" that refers to the configuration file you want to use. Your alternative
 * configuration file must be on the class path.
 * The properties file should contain the desired configuration values for each of the
 * various components - Driver, Compiler, etc. Please refer to the relevant configuration
 * for each of these.
 *
 * @author vince
 */
public class Components {

    private Components() {
    }

    private static final Logger logger = LoggerFactory.getLogger(Components.class);

    private static Configuration configuration = new Configuration();

    /**
     * Configure the OGM from a pre-built Configuration class
     *
     * @param configuration The configuration to use
     */
    public static void configure(Configuration configuration) {
        // new configuration object, or update of current one?
        if (Components.configuration != configuration) {
            Components.configuration = configuration;
        }
    }

    /**
     * Configure the OGM from the specified config file
     *
     * @param configurationFileName The config file to use
     */
    public static void configure(String configurationFileName) {
        destroy();
        configuration = new Configuration(new ClasspathConfigurationSource(configurationFileName));
    }

    /**
     * Releases any current driver resources and clears the current configuration
     */
    public synchronized static void destroy() {

        configuration.clear();
    }

    /**
     * There is a single configuration object, which should never be null, associated with the Components class
     * You can update this configuration in-situ, or you can replace the configuration with another.
     *
     * @return the current Configuration object
     */
    public static Configuration getConfiguration() {
        return configuration;
    }
}
