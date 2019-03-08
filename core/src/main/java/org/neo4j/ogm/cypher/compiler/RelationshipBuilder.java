/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.cypher.compiler;

import org.neo4j.ogm.model.Edge;

/**
 * Builds a relationship to be persisted in the database
 *
 * @author Luanne Misquitta
 */
public interface RelationshipBuilder extends PropertyContainerBuilder<RelationshipBuilder> {

    Long reference();

    void setReference(Long reference);

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

    Edge edge();

    void setPrimaryIdName(String primaryIdName);

}
