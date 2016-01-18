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

package org.neo4j.ogm.config;

import org.neo4j.ogm.classloader.ClassLoaderResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * A generic configuration class that can be set up programmatically
 * or via a properties file.
 *
 * @author vince
 */
public class Configuration {

    private final Logger logger = LoggerFactory.getLogger(Configuration.class);

    private final Map<String, Object> config = new HashMap<>();

    public Configuration() {}

    public Configuration(String propertiesFilename) {
        configure(propertiesFilename);
    }

    public void set(String key, Object value) {
        config.put(key, value);
    }

    public Object get(String key) {
        return config.get(key);
    }

    private void configure(String propertiesFileName) {

        try(InputStream is = ClassLoaderResolver.resolve().getResourceAsStream(propertiesFileName)) {

            Properties properties = new Properties();
            properties.load(is);
            Enumeration propertyNames = properties.propertyNames();

            while (propertyNames.hasMoreElements()) {
                String propertyName = (String) propertyNames.nextElement();
                config.put(propertyName, properties.getProperty(propertyName));
            }
        } catch (Exception e) {
            logger.warn("Could not load {}",propertiesFileName);
        }
    }

    public DriverConfiguration driverConfiguration() {
        return new DriverConfiguration(this);
    }

    public CompilerConfiguration compilerConfiguration() {
        return new CompilerConfiguration(this);
    }
}
