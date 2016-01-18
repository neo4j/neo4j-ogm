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
 * Resolves the most appropriate class loader, based on the supplied Strategy implementation
 *
 * @author vince
 *
 */
public abstract class ClassLoaderResolver {

    private static final Strategy strategy;
    private static final CallerResolver CALLER_RESOLVER;

    static {
        try {
            CALLER_RESOLVER = new CallerResolver();
            strategy = new DefaultStrategy();
        } catch (SecurityException se) {
            throw new RuntimeException(se);
        }
    }

    public static synchronized ClassLoader resolve() {
        return strategy.classLoader(callingClass());
    }

    /**
     * A helper class to get the call context. It subclasses SecurityManager
     * to make getClassContext() accessible. An instance of CallerResolver
     * only needs to be created, not installed as an actual security
     * manager.
     */
    private static final class CallerResolver extends SecurityManager {
        protected Class[] getClassContext() {
            return super.getClassContext();
        }
    }

    // do not change the array value from 4 unless you know why you're doing it !
    private static Class callingClass() {
        return CALLER_RESOLVER.getClassContext()[4];
    }

}