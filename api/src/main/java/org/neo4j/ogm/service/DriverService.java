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

import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * This class is responsible for loading a Driver using the Service Loader mechanism
 *
 * @author vince
 */
public class DriverService {

    private static final Logger logger = LoggerFactory.getLogger(DriverService.class);

    private static Driver load(String className) {

        for (Driver driver : ServiceLoader.load(Driver.class)) {
            try {
                if (driver.getClass().getName().equals(className)) {
                    logger.info("Using driver: {}", className);
                    return driver;
                }
            } catch (ServiceConfigurationError sce) {
                logger.warn("{}, reason: {}", sce.getLocalizedMessage(), sce.getCause());
            }
        }

        throw new ServiceNotFoundException("Driver: " + className);

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
