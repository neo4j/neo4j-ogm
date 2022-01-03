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
package org.neo4j.ogm.domain.hierarchy.dualRelation;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Luanne Misquitta
 */
@NodeEntity(label = "DataView")
public class DataView {

    public Long id;
    String name;

    @Relationship(type = "OWNER", direction = Relationship.Direction.OUTGOING)
    public Thing owner;

    @Relationship(type = "SHARED_WITH", direction = Relationship.Direction.OUTGOING)
    public List<Thing> sharedWith = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Thing getOwner() {
        return owner;
    }

    public void setOwner(Thing owner) {
        this.owner = owner;
    }

    public List<Thing> getSharedWith() {
        return sharedWith;
    }

    public void setSharedWith(List<Thing> sharedWith) {
        this.sharedWith = sharedWith;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
