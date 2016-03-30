/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

package org.neo4j.ogm.service;

import org.neo4j.ogm.classloader.ClassLoaderResolver;
import org.neo4j.ogm.compiler.Compiler;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * This class is responsible for ensuring that the various pluggable components
 * required by the OGM can be loaded.
 *
 * The Components class can be explicitly configured via an {@link Configuration} instance.
 *
 * If no explicit configuration is supplied, the class will attempt to auto-configure.
 *
 * Auto-configuration is accomplished using a properties file. By default, this file
 * is called "ogm.properties" and it must be available on the class path.
 *
 * You can supply a different configuration properties file, by specifying a system property
 * "ogm.properties" that refers to the configuration file you want to use. Your alternative
 * configuration file must be on the class path.
 *
 * The properties file should contain the desired configuration values for each of the
 * various components - Driver, Compiler, etc. Please refer to the relevant configuration
 * for each of these.
 *
 * @author vince
 */
public class Components {

    private Components() {}

    private static final Logger logger = LoggerFactory.getLogger(Components.class);

    private static Configuration configuration = new Configuration();
    private static Driver driver;

    /**
     * Configure the OGM from a pre-built Configuration class
     * @param configuration The configuration to use
     */
    public static void configure(Configuration configuration) {
        driver = null;
        Components.configuration = configuration;
    }

    /**
     * Configure the OGM from the specified config file
     * @param configurationFileName The config file to use
     */
    public static void configure(String configurationFileName) {
        try (InputStream is = classPathResource(configurationFileName)) {
            configure(is);
        } catch (Exception e) {
            logger.warn("Could not configure OGM from {}", configurationFileName);
        }

    }

    // only one instance of the driver exists for the lifetime of the application
    // please note you cannot use this method to find out if a driver is initialised
    // because it will attempt to initialise the driver if it is not.
    public synchronized static Driver driver() {
        if (driver == null) {
            loadDriver();
        }
        return driver;
    }

    // new instance of the compiler is returned every time
    public synchronized static Compiler compiler() {
        return loadCompiler();
    }

    /**
     * The OGM Components can be auto-configured from a properties file, "ogm.properties", or
     * a similar configuration file, specified by a system property or environment variable called "ogm.properties".
     *
     * If an auto-configure properties file is not available by any of these means, the Components class should be configured
     * by passing in a Configuration object to the configure method, or an explicit configuration file name
     */
    public synchronized static void autoConfigure() {
        try(InputStream is = configurationFile()) {
            configure(is);
        } catch (Exception e) {
            logger.warn("Could not autoconfigure the OGM");
        }

    }

    private static void configure(InputStream is) throws Exception {
        configuration.clear();
        driver = null;
        Properties properties = new Properties();
        properties.load(is);
        Enumeration propertyNames = properties.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = (String) propertyNames.nextElement();
            configuration.set(propertyName, properties.getProperty(propertyName));
        }
    }

    private static void loadDriver() {
        if (configuration.driverConfiguration().getDriverClassName() == null) {
            autoConfigure();
        }
        setDriver (DriverService.load(configuration.driverConfiguration()));
    }

    private static Compiler loadCompiler() {
        if (configuration.compilerConfiguration().getCompilerClassName() == null) {
            autoConfigure();
        }
        return CompilerService.load(configuration.compilerConfiguration());
    }

    private static InputStream configurationFile() throws Exception {
        String configFileName;
        configFileName = System.getenv("ogm.properties");

        if (configFileName == null) {
            configFileName = System.getProperty("ogm.properties");
            if (configFileName == null) {
                return classPathResource("ogm.properties");
            }
        }
        // load the config from the user-specified file
        return classPathResource(configFileName);
    }

    private static InputStream classPathResource(String name) {
        logger.debug("Trying to configure from {} ", name);
        return ClassLoaderResolver.resolve().getResourceAsStream(name);
    }

    public static void setDriver(Driver driver) {
        logger.debug("Setting driver to: {}", driver.getClass().getName());
        Components.driver = driver;
    }

    public static double neo4jVersion() {
        String neo4jVersion = (String) configuration.get("neo4j.version");
        if (neo4jVersion != null) {
            try {
                return new Double(neo4jVersion);
            } catch (NumberFormatException nfe) {
                logger.warn("Configuration property 'neo4j.version' is not in the correct form: expected something like '2.3'");
            }
        }
        return 9.9; // unknown version
    }

    public static Configuration configuration() {
        return configuration;
    }
}
