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
package org.neo4j.ogm.persistence.examples.versioned_rel_entity;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Michael J. Simons
 */
@NodeEntity
public class A {

    @Id
    private String id;

    @Relationship(value = "R", direction = Relationship.Direction.INCOMING)
    private List<R> bs = new ArrayList<>();

    public A() {
    }

    public A(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public R add(B b) {
        R r = new R(this, b);
        this.bs.add(r);
        return r;
    }

    public List<R> getBs() {
        return bs;
    }

    @Override
    public String toString() {
        return "A{" +
            "id='" + id + '\'' +
            '}';
    }
}
