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

/**
 * A default implementation of {@link Strategy} that should be suitable
 * for frameworks such as Play
 *
 * @author vince
 *
 */
class DefaultStrategy implements Strategy {

    public ClassLoader classLoader(final Class caller) {

        if (caller == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        // the three amigos.
        final ClassLoader callerClassLoader  = caller.getClassLoader();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final ClassLoader systemClassLoader  = ClassLoader.getSystemClassLoader();

        ClassLoader classLoader;

        if (inHierarchy(contextClassLoader, callerClassLoader)) {
            classLoader = callerClassLoader;
        }
        else {
            classLoader = contextClassLoader;
        }

        // finally check if class loader is in system class loader hierarchy
        if (inHierarchy(classLoader, systemClassLoader)) {
            classLoader = systemClassLoader;
        }

        return classLoader;
    }

    private static boolean inHierarchy(final ClassLoader loader1, ClassLoader loader2) {

        if (loader1 == null || loader2 == null || loader1 == loader2) {
            return true;
        }

        for (; loader2 != null; loader2 = loader2.getParent()) {
            if (loader2 == loader1) {
                return true;
            }
        }

        return false;
    }

}