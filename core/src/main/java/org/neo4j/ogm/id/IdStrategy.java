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

package org.neo4j.ogm.id;

/**
 * Id generation strategy that allows custom implementations of id generation.
 * For simple use cases, implementing classes should provide a no-argument constructor and OGM will instantiate the
 * strategy.
 * For cases where OGM can't instantiate the strategy (e.g. because it has other dependencies) it must be registered
 * with the SessionFactory.
 *
 * @author Frantisek Hartman
 * @since 3.0
 */
public interface IdStrategy {

    /**
     * Generates new id for given entity
     *
     * @param entity saved entity
     * @return identifier of the entity
     */
    Object generateId(Object entity);
}
