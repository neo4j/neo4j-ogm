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


import java.io.File;
import java.net.URL;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.neo4j.ogm.classloader.ResourceResolver;
import org.neo4j.ogm.exception.ServiceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vince
 */
public class ResourceService
{
    private static final Logger logger = LoggerFactory.getLogger( ResourceResolver.class );

    private static ServiceLoader< ResourceResolver > serviceLoader = ServiceLoader.load( ResourceResolver.class );

    public static File resolve( URL url ) throws Exception {

        for (ResourceResolver resourceResolver : serviceLoader) {
            try {
                File file = resourceResolver.resolve(url);
                if (file != null) {
                    return file;
                }
            } catch (ServiceConfigurationError sce) {
                logger.warn("{}, reason: {}", sce.getLocalizedMessage(), sce.getCause());
            }
        }

        throw new ServiceNotFoundException("Resource: " + url.toExternalForm());
    }
}
