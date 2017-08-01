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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * {@link ConfigurationSource} reading configuration from files on filesystem
 *
 * @author Frantisek Hartman
 */
public class FileConfigurationSource implements ConfigurationSource {

    private final Properties properties = new Properties();

    /**
     * Create {@link FileConfigurationSource}
     *
     * @param propertiesFilePath relative or absolute path to the configuration file
     */
    public FileConfigurationSource(String propertiesFilePath) {
        try {
            try (InputStream is = new FileInputStream(createFile(propertiesFilePath))) {
                properties.load(is);
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Could not load properties file: " + propertiesFilePath, e);
        }
    }

    private File createFile(String propertiesFilePath) throws URISyntaxException {
        if (propertiesFilePath.toLowerCase().startsWith("file:")) {
            return new File(new URI(propertiesFilePath));
        } else {
            return new File(propertiesFilePath);
        }
    }

    @Override
    public Properties properties() {
        return properties;
    }

}
