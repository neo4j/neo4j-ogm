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
