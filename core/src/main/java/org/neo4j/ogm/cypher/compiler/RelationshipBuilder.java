/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

import org.neo4j.ogm.model.Edge;

/**
 * Builds a relationship to be persisted in the database
 *
 * @author Luanne Misquitta
 */
public interface RelationshipBuilder {

    Long reference();

    void setReference(Long reference);

    void addProperty(String key, Object value);

    void addProperties(Map<String, ?> properties);

    String type();

    void setType(String type);

    void relate(Long startNodeId, Long endNodeId);

    RelationshipBuilder direction(String direction);

    boolean hasDirection(String direction);

    boolean isBidirectional();

    boolean isSingleton();

    void setSingleton(boolean singleton);

    boolean isRelationshipEntity();

    void setRelationshipEntity(boolean relationshipEntity);

    boolean isNew();

    Edge edge();

    void setPrimaryIdName(String primaryIdName);
}
