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

import org.neo4j.ogm.config.DriverConfiguration;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.exception.ServiceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * This class is responsible for loading a Driver using the Service Loader mechanism
 *
 * @author Vince Bickers
 * @author Mark Angrish
 */
public class DriverService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriverService.class);

    private static Driver load(String className) {

        Iterator<Driver> iterator = ServiceLoader.load(Driver.class).iterator();

        while (iterator.hasNext()) {
            try {
                Driver driver = iterator.next();
                if (driver.getClass().getName().equals(className)) {
                    LOGGER.info("Using: [{}]", className);
                    return driver;
                }
            } catch (ServiceConfigurationError sce) {
                if (sce.getCause() != null) {
                    throw new ServiceNotFoundException("Could not load driver: " + className + ".", sce);
                }
                else {
                    LOGGER.debug("Error loading driver: {}", sce.getLocalizedMessage());
                }
            }
        }

        throw new ServiceNotFoundException("Could not load driver: " + className + ".");

    }

    /**
     * Loads and initialises a Driver using the specified DriverConfiguration
     *
     * @param configuration an instance of {@link DriverConfiguration} with which to configure the driver
     * @return the named {@link Driver} if found, otherwise throws a ServiceNotFoundException
     */
    public static Driver load(DriverConfiguration configuration) {
        String driverClassName = configuration.getDriverClassName();
        Driver driver = load(driverClassName);
        driver.configure(configuration);
        return driver;
    }
}
