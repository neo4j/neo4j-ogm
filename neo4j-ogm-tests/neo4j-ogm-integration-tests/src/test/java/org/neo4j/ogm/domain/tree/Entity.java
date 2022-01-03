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
package org.neo4j.ogm.domain.tree;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Luanne Misquitta
 */
public class Entity implements Comparable {

    private Long id;
    private String name;

    @Relationship(type = "REL", direction = Relationship.Direction.OUTGOING)
    private Entity parent;

    @Relationship(type = "REL", direction = Relationship.Direction.INCOMING)
    private SortedSet<Entity> children = new TreeSet<>();

    public Entity() {
    }

    public Entity(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public Set<Entity> getChildren() {
        return children;
    }

    public void setChildren(SortedSet<Entity> children) {
        this.children = children;
    }

    public Entity getParent() {
        return parent;
    }

    public Entity setParent(Entity parent) {
        parent.children.add(this);
        this.parent = parent;
        return this;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(Object o) {
        Entity that = (Entity) o;
        if (this.name != null) {
            if (that.name != null) {
                return this.name.compareTo(that.name);
            } else {
                return 1;
            }
        }
        return -1;
    }
}
