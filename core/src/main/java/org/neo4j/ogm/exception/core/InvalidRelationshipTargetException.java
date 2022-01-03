/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
package org.neo4j.ogm.exception.core;

/**
 * This exception is thrown when a one to many relationship collection contains the {@literal null} value.
 *
 * @author Michael J. Simons
 * @soundtrack Antilopen Gang - Abbruch Abbruch
 * @since 3.2.11
 */
public final class InvalidRelationshipTargetException extends MappingException {

    private final Class<?> startNodeType;

    private final String relationshipType;

    private final String attributeName;

    private final Class<?> endNodeType;

    public InvalidRelationshipTargetException(Class<?> startNodeType, String relationshipType, String attributeName,
        Class<?> endNodeType) {
        super("The relationship '" + relationshipType + "' from '" + startNodeType.getName()
            + "' to '" + endNodeType.getName() + "' stored on '#" + attributeName
            + "' contains 'null', which is an invalid target for this relationship.'");
        this.startNodeType = startNodeType;
        this.relationshipType = relationshipType;
        this.attributeName = attributeName;
        this.endNodeType = endNodeType;
    }

    public Class<?> getStartNodeType() {
        return startNodeType;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public Class<?> getEndNodeType() {
        return endNodeType;
    }
}
