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

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.neo4j.ogm.classloader.ClassLoaderResolver;
import org.neo4j.ogm.compiler.Compiler;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.autoindex.AutoIndexMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     *
     * @param configuration The configuration to use
     */
    public static void configure(Configuration configuration) {
        // new configuration object, or update of current one?
        if (Components.configuration != configuration) {
            destroy();
            Components.configuration = configuration;
        }
        else {
            // same config - but have we switched drivers?
            if (driver != null && !driver.getClass().getCanonicalName().equals(configuration.driverConfiguration().getDriverClassName())) {
                driver.close();
                driver = null;
            }
        }
    }

    /**
     * Configure the OGM from the specified config file
     *
     * @param configurationFileName The config file to use
     */
    public static void configure(String configurationFileName) {
        try (InputStream is = toInputStream(configurationFileName)) {
            configure(is);
        } catch (Exception e) {
            logger.warn("Could not configure OGM from {}", configurationFileName);
        }

    }

    /**
     * Returns the current OGM {@link Driver}
     *
     * Normally only one instance of the driver exists for the lifetime of the application
     *
     * You cannot use this method to find out if a driver is initialised because it will attempt to
     * initialise the driver if it is not.
     *
     * @return an instance of the {@link Driver} to be used by the OGM
     */
    public synchronized static Driver driver() {
        if (driver == null) {
            loadDriver();
        }
        return driver;
    }

    /**
     *
     * Returns a new instance of the compiler
     *
     * @return an instance of the {@link Compiler} to be used by the OGM
     */
    public synchronized static Compiler compiler() {
        return getCompiler();
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

    /**
     * Creates a {@link Configuration} from an InputStream
     *
     * @param is an InputStream
     * @throws Exception
     */
    private static void configure(InputStream is) throws Exception {
        destroy();
        Properties properties = new Properties();
        properties.load(is);
        Enumeration propertyNames = properties.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = (String) propertyNames.nextElement();
            configuration.set(propertyName, properties.getProperty(propertyName));
        }
    }

    /**
     * Loads the configured Neo4j {@link Driver} and stores it on this class
     */
    private static void loadDriver() {
        if (configuration.driverConfiguration().getDriverClassName() == null) {
            autoConfigure();
        }
        setDriver (DriverService.load(configuration.driverConfiguration()));
    }

    /**
     * Obtains the Cypher compiler to be used by the OGM
     *
     * @return an instance of {@link Compiler}
     */
    private static Compiler getCompiler() {
        if (configuration.compilerConfiguration().getCompilerClassName() == null) {
            autoConfigure();
        }
        return CompilerService.load(configuration.compilerConfiguration());
    }

    /**
     * Tries to locate the default configuration file resource and return it as an InputStream
     *
     * @return An InputStream resource corresponding to the default configuration file, if it exists.
     */
    private static InputStream configurationFile() {
        String configFileName;
        configFileName = System.getenv("ogm.properties");

        if (configFileName == null) {
            configFileName = System.getProperty("ogm.properties");
            if (configFileName == null) {
                return toInputStream("ogm.properties");
            }
        }

        return toInputStream(configFileName);
    }

    /**
     * Fetches a configuration file resource as an InputStream
     *
     * @param name the configuration file resource name
     * @return the file resource as an InputStream
     */
    private static InputStream toInputStream(String name) {
        logger.debug("Trying to configure from {} ", name);
        return ClassLoaderResolver.resolve().getResourceAsStream(name);
    }

    /**
     * Sets a new {@link Driver} to be used by the OGM.
     *
     * If a different driver is in use, it will be closed first. In addition, the {@link Configuration} is updated
     * to reflect the correct classname for the new driver.
     *
     * @param driver an instance of {@link Driver} to be used by the OGM.
     */
    public static void setDriver(Driver driver) {

        logger.debug("Setting driver to: {}", driver.getClass().getName());

        if (Components.driver != null && Components.driver != driver) {
            Components.driver.close();
            Components.getConfiguration().driverConfiguration().setDriverClassName(driver.getClass().getCanonicalName());
        }

        Components.driver = driver;
    }

    /**
     * Gets the neo4j.version from the current configuration
     *
     * @return the major.minor part of the neo4.version property string, as a double, or 9.9 if not configured
     */
    public static double neo4jVersion() {
        String neo4jVersion = (String) configuration.get("neo4j.version");
        if (neo4jVersion != null) {
            try {
                String[] versionElements = neo4jVersion.split("\\.");
                if (versionElements.length < 2) {
                    throw new NumberFormatException();
                }
                return new Double(versionElements[0] + "." + versionElements[1]);
            } catch (NumberFormatException nfe) {
                logger.warn("Configuration property 'neo4j.version' is not in the correct form: expected something like '2.3', but got '{}' instead", neo4jVersion);
            }
        }
        return 9.9; // unknown version
    }

    /**
     * Releases any current driver resources and clears the current configuration
     */
    public synchronized static void destroy() {

        if (driver != null) {
            driver.close();
            driver = null;
        }
        configuration.clear();
    }

	/**
	 * Return the {@link AutoIndexMode} from the AutoIndexConfiguration
	 * @return the configured autoIndexMode or AutoIndexMode.NONE if not configured
	 */
	public static AutoIndexMode autoIndexMode() {
		return configuration.autoIndexConfiguration().getAutoIndex();
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
