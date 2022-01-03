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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * @author Michael J. Simons
 */
final class ConfigurationUtils {

    /**
     * Pseudo URL prefix for loading from the class path: "classpath:".
     */
    private static final String CLASSPATH_URL_PREFIX = "classpath:";

    /**
     * Strips of <pre>classpath:</pre> and treats the rest as an absolute resource path before trying to open an input stream.
     *
     * @param name
     * @return
     */
    static InputStream getResourceAsStream(String name) {

        String path = name;
        if (path.startsWith(CLASSPATH_URL_PREFIX)) {
            path = path.substring(CLASSPATH_URL_PREFIX.length());
        }

        return Configuration.getDefaultClassLoader().getResourceAsStream(createRelativePathIfNecessary(path));
    }

    /**
     * Extracts an URL from the name of a resource. The idea is similar to Springs
     * {@code org.springframework.core.io.DefaultResourceLoader} respectively
     * {@code org.springframework.util.ResourceUtils}.
     *
     * @param resourceLocation The location of the resource, must not be null.
     * @return URL of the given resource location
     * @throws FileNotFoundException In all cases where an URL cannot be determined
     */
    static URL getResourceUrl(String resourceLocation) throws FileNotFoundException {

        Objects.requireNonNull(resourceLocation, "Resource location must not be null");

        URL resourceUrl;
        if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
            resourceUrl = getClasspathResourceUrl(resourceLocation.substring(CLASSPATH_URL_PREFIX.length()));
        } else {
            try {
                resourceUrl = new URL(resourceLocation);
            } catch (MalformedURLException e) {
                // Not a protocol? Try classpath resource URL, again
                resourceUrl = getClasspathResourceUrl(resourceLocation);
            }
        }
        return resourceUrl;
    }

    private static URL getClasspathResourceUrl(String path) throws FileNotFoundException {

        String pathToUse = createRelativePathIfNecessary(path);

        URL url = Configuration.getDefaultClassLoader().getResource(pathToUse);
        if (url == null) {
            String description = "class path resource [" + path + "]";
            throw new FileNotFoundException(description +
                " cannot be resolved to URL because it does not exist");
        }
        return url;
    }

    private static String createRelativePathIfNecessary(String path) {

        return path.startsWith("/") ? path.substring(1) : path;
    }

    private ConfigurationUtils() {
    }
}
