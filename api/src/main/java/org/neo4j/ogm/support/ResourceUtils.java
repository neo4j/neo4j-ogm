/*
 * Copyright (c) 2002-2020 "Neo4j,"
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
package org.neo4j.ogm.support;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * @author Michael J. Simons
 */
public final class ResourceUtils {

    /**
     * Pseudo URL prefix for loading from the class path: "classpath:".
     */
    private static final String CLASSPATH_URL_PREFIX = "classpath:";

    /**
     * Extracts an URL from the name of a resource. The idea is similar to Springs
     * {@code org.springframework.core.io.DefaultResourceLoader} respectively
     * {@code org.springframework.util.ResourceUtils}.
     *
     * @param resourceLocation The location of the resource, must not be null.
     * @return URL of the given resource location
     * @throws FileNotFoundException In all cases where an URL cannot be determined
     */
    public static URL getResourceUrl(String resourceLocation) throws FileNotFoundException {

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
        ClassLoader cl = ClassUtils.getDefaultClassLoader();
        URL url = (cl != null ? cl.getResource(path) : ClassLoader.getSystemResource(path));
        if (url == null) {
            String description = "class path resource [" + path + "]";
            throw new FileNotFoundException(description +
                " cannot be resolved to URL because it does not exist");
        }
        return url;
    }

    private ResourceUtils() {
    }
}
