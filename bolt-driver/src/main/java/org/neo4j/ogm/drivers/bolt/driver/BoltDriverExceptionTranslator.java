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
package org.neo4j.ogm.drivers.bolt.driver;

import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;
import org.neo4j.ogm.driver.ExceptionTranslator;
import org.neo4j.ogm.exception.ConnectionException;

/**
 * Translates exceptions from the Java driver into unchecked exceptions.
 *
 * @author Michael J. Simons
 * @soundtrack Die Toten Hosen - Ein kleines bisschen Horrorschau
 * @since 3.2
 */
class BoltDriverExceptionTranslator implements ExceptionTranslator {

    @Override
    public RuntimeException translateExceptionIfPossible(Throwable e) {

        if (e instanceof ServiceUnavailableException) {
            return new ConnectionException(e.getMessage(), e);
        }

        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }

        return new RuntimeException(e);
    }
}
