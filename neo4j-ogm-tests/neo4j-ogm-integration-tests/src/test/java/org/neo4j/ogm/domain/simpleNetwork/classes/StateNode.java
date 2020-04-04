/*
 * Copyright (c) 2002-2020 "Neo4j,"
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
package org.neo4j.ogm.domain.simpleNetwork.classes;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.domain.simpleNetwork.abstractions.AbstractStateNode;

/**
 * @author vince
 */
@NodeEntity(label = "StateNode")
public class StateNode extends AbstractStateNode<TimeRelation> {

    @Relationship(direction = Relationship.Direction.INCOMING, type = "IDENTITY_STATE")
    private TimeRelation identityRelation;

    @Relationship(direction = Relationship.Direction.OUTGOING, type = "PREV")
    private StateNode previous;

    private String name;
    private String description;

    @Override
    public TimeRelation getIdentityRelation() {
        return identityRelation;
    }

    @Override
    public void setIdentityRelation(TimeRelation identityRelation) {
        this.identityRelation = identityRelation;
    }

    public StateNode getPrevious() {
        return previous;
    }

    public void setPrevious(StateNode previous) {
        this.previous = previous;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
