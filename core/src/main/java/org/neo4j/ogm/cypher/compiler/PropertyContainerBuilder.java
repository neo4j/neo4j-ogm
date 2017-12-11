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

package org.neo4j.ogm.cypher.compiler;

import java.util.Map;

import org.neo4j.ogm.cypher.compiler.RelationshipBuilder;

/**
 * Interface to abstract setting properties on nodes/relationship entities
 *
 * @author Frantisek Hartman
 */
public interface PropertyContainerBuilder<T> {

    T addProperty(String key, Object value);

    T addProperties(Map<String, ?> properties);

    T setVersionProperty(String name, Long version);
}
