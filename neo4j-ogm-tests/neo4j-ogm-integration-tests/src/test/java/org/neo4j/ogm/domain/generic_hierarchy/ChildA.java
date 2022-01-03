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
package org.neo4j.ogm.domain.generic_hierarchy;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Jonathan D'Orleans
 * @author Michael J. Simons
 */
public class ChildA extends Entity {

    private String value;

    private Set<AnotherEntity> children = new HashSet<>();

    public ChildA() {
    }

    public ChildA(String uuid) {
        super(uuid);
    }

    public ChildA add(AnotherEntity childB) {
        children.add(childB);
        return this;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Set<AnotherEntity> getChildren() {
        return children;
    }

    public void setChildren(Set<AnotherEntity> children) {
        this.children = children;
    }
}
