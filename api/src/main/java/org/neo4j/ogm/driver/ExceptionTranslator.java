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
 * This is a helper class modelled roughly after {@code org.springframework.dao.support.PersistenceExceptionTranslator}.
 * <br>
 * There are several situations where exceptions that happen inside a specific driver bubbles up into the core and cannot
 * be caught inside the driver. This is especially true for the bolt driver: The {@code org.neo4j.ogm.drivers.bolt.response.BoltResponse}
 * keeps an open result set which might throw a {@code org.neo4j.driver.v1.exceptions.ServiceUnavailableException} at
 * a very late stage.
 * <br>
 * This should be handled gracefully inside the {@code org.neo4j.ogm.session.Neo4jSession}. It uses this translator to
 * translate driver specific exceptions into exception declared in the Neo4j-OGM api.
 * <br>
 * The translator doesn't come as an interface, but as concrete implementation so that it can be used "as is" by our drivers.
 *
 * @author Michael J. Simons
 * @soundtrack Die Toten Hosen - Crash Landing
 * @since 3.2
 */
@FunctionalInterface
public interface ExceptionTranslator {

    /**
     * Translates exceptions thrown by any of the drivers into exceptions defined in the Neo4j-OGM api.
     * <br>
     * Currently it deals with generic exceptions and throwables. This is due to the fact that
     * <ol>
     * <li>Neo4jSession is build that way</li>
     * <li>We don't have yet a clear defined exception hierarchy</li>
     * </ol>
     * The later is bound to change in 4.0. Until than, that we introduce this method as deprecated, so that it's short
     * lifetime is clear.
     *
     * @param e Exception to translate
     * @return The translated exception or {@code e} if a translation is not possible.
     */
    @Deprecated
    RuntimeException translateExceptionIfPossible(Throwable e);
}

