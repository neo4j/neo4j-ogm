/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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
