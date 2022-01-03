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
public class MyNode {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @Relationship(type = "REL_ONE", direction = Direction.INCOMING)
    private MyNode refOne;

    @Relationship(type = "REL_TWO", direction = Direction.UNDIRECTED)
    private List<MyNode> refTwo = new ArrayList<>();

    public MyNode(String name) {
        this.name = name;
    }

    public MyNode() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public MyNode getRefOne() {
        return refOne;
    }

    public void setRefOne(MyNode refOne) {
        this.refOne = refOne;
    }

    public List<MyNode> getRefTwo() {
        return refTwo;
    }

    public void setRefTwo(List<MyNode> refTwo) {
        this.refTwo = refTwo;
    }

    public MyNode copy() {
        MyNode n = new MyNode();
        n.id = id;
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
        MyNode myNode = (MyNode) o;
        return Objects.equals(id, myNode.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override public String toString() {
        return "MyNode{" +
            "id=" + id +
            ", name='" + name + '\'' +
            '}';
    }
}
