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
package org.neo4j.ogm.domain.gh640;

import static org.neo4j.ogm.annotation.Relationship.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Michael J. Simons
 */
@NodeEntity
public class MyNodeWithAssignedId {

    @Id
    private String name;

    @Relationship(type = "REL_ONE", direction = Direction.INCOMING)
    private MyNodeWithAssignedId refOne;

    @Relationship(type = "REL_TWO", direction = Direction.UNDIRECTED)
    private List<MyNodeWithAssignedId> refTwo = new ArrayList<>();

    public MyNodeWithAssignedId(String name) {
        this.name = name;
    }

    public MyNodeWithAssignedId() {
    }

    public String getName() {
        return name;
    }

    public MyNodeWithAssignedId getRefOne() {
        return refOne;
    }

    public void setRefOne(MyNodeWithAssignedId refOne) {
        this.refOne = refOne;
    }

    public List<MyNodeWithAssignedId> getRefTwo() {
        return refTwo;
    }

    public void setRefTwo(List<MyNodeWithAssignedId> refTwo) {
        this.refTwo = refTwo;
    }

    public MyNodeWithAssignedId copy() {
        MyNodeWithAssignedId n = new MyNodeWithAssignedId();
        n.name = name;
        n.setRefOne(refOne);
        n.setRefTwo(refTwo);
        return n;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MyNodeWithAssignedId myNode = (MyNodeWithAssignedId) o;
        return Objects.equals(name, myNode.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override public String toString() {
        return "MyNodeWithAssignedId{" +
            "name='" + name + '\'' +
            '}';
    }
}
