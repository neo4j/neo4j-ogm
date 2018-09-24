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
package org.neo4j.ogm.spi;

import java.util.function.Function;

import org.neo4j.ogm.config.Configuration;

/**
 * @author Michael J. Simons
 */
public interface CypherModificationProvider {
    /**
     * Get the order value of this object.
     * Higher values are interpreted as lower priority. As a consequence, the object with the lowest value has the highest priority.
     * Same order values will result in arbitrary sort positions for the affected objects.
     *
     * @return the order value
     */
    default int getOrder() {
        return Integer.MAX_VALUE;
    }

    Function<String, String> getCypherModification(Configuration configuration);
}
