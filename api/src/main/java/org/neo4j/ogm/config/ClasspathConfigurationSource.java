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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * {@link ConfigurationSource} reading configuration from classpath files.
 *
 * @author Mark Angrish
 * @author Michael J. Simons
 */
public class ClasspathConfigurationSource implements ConfigurationSource {

    private final Properties properties = new Properties();

    public ClasspathConfigurationSource(String propertiesFileName) {

        try (InputStream is = ConfigurationUtils.getResourceAsStream(propertiesFileName)) {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Could not load properties file: " + propertiesFileName, e);
        }
    }

    @Override
    public Properties properties() {
        return this.properties;
    }
}
