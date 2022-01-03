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

import java.util.Properties;

/**
 * Provides access to a configuration store and exposes configuration values in bulk {@link Properties} format.
 * Allows users to define and load custom property formats (e.g. YAML, HOGAN etc.) that are not supported in the core OGM API.
 *
 * @author Mark Angrish
 */
public interface ConfigurationSource {

    /**
     * Get configuration set for this source in a form of {@link Properties}.
     *
     * @return configuration set .
     */
    Properties properties();
}
