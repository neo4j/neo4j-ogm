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
package org.neo4j.ogm.driver;

/**
 * Exception indicating problems with the native type system.
 *
 * @author Michael J. Simons
 * @since 3.2
 */
public abstract class NativeTypesException extends IllegalStateException {

    /**
     * The name of the driver that caused the exception.
     */
    protected final String driverClassName;

    NativeTypesException(String driverClassName, String s) {
        super(s);
        this.driverClassName = driverClassName;
    }

    public String getDriverClassName() {
        return driverClassName;
    }
}
