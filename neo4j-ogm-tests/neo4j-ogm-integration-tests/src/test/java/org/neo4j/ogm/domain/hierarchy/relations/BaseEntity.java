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
package org.neo4j.ogm.domain.hierarchy.relations;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Luanne Misquitta
 */
@NodeEntity(label = "BaseType")
public abstract class BaseEntity {

    @Id @GeneratedValue
    private Long graphId;
    public String name;

    @Relationship(type = "RELATED_TO", direction = Relationship.Direction.OUTGOING)
    private List<BaseEntity> outgoing = new ArrayList<>();
    @Relationship(type = "RELATED_TO", direction = Relationship.Direction.INCOMING)
    private List<BaseEntity> incoming = new ArrayList<>();

    public Long getGraphId() {
        return graphId;
    }

    public List<BaseEntity> getOutgoing() {
        return outgoing;
    }

    public void setOutgoing(List<BaseEntity> outgoing) {
        this.outgoing = outgoing;
    }

    public List<BaseEntity> getIncoming() {
        return incoming;
    }

    public void setIncoming(List<BaseEntity> incoming) {
        this.incoming = incoming;
    }

    public void addIncoming(BaseEntity related) {
        incoming.add(related);
        related.getOutgoing().add(this);
    }
}
