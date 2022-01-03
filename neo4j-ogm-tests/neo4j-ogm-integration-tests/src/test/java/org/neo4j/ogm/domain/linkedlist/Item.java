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
package org.neo4j.ogm.domain.linkedlist;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Vince Bickers
 */
@NodeEntity
public class Item {

    @Relationship(type = "NEXT", direction = Relationship.Direction.OUTGOING)
    public Item next;
    @Relationship(type = "NEXT", direction = Relationship.Direction.INCOMING)
    public Item previous;

    @Relationship(type = "BELONGS_TO", direction = Relationship.Direction.OUTGOING)
    public Item belongsTo;

    private Long id;
    private String name;

    public Item() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override public String toString() {
        return "Item{" +
            "name='" + name + '\'' +
            '}';
    }
}
