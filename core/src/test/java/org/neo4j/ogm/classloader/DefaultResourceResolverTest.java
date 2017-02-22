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

package org.neo4j.ogm.classloader;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;

/**
 * @author vince
 */
public class DefaultResourceResolverTest {

    @Test
    public void shouldHandleURLEncodedClassFileEntry() throws Exception {

        DefaultResourceResolver resourceResolver = new DefaultResourceResolver();
        File f = resourceResolver.resolve(new URL("file:///foo%23bar/WEB-INF/classes/Dependency.class"));
        Assert.assertEquals("/foo#bar/WEB-INF/classes/Dependency.class", f.getPath());

    }

    @Test
    public void shouldHandleURLEncodedJarFileEntry() throws Exception {

        DefaultResourceResolver resourceResolver = new DefaultResourceResolver();
        File f = resourceResolver.resolve(new URL("jar:file:///foo%23bar/WEB-INF/lib/dependencies.jar!/Dependency.class"));
        Assert.assertEquals("/foo#bar/WEB-INF/lib/dependencies.jar", f.getPath());

    }

}