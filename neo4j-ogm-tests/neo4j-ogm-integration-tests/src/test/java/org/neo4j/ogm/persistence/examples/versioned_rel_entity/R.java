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

import java.util.List;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.annotation.Version;

/**
 * @author Michael J. Simons
 */
@RelationshipEntity
public class R {

    @Id @GeneratedValue
    private Long id;

    private String someAttribute;

    private List<String> roles;

    @EndNode
    private A a;

    @StartNode
    private B b;

    @Version
    private long version;

    public R() {
    }

    public R(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public List<String> getRoles() {
        return roles;
    }

    public Long getId() {
        return id;
    }

    public String getSomeAttribute() {
        return someAttribute;
    }

    public void setSomeAttribute(String someAttribute) {
        this.someAttribute = someAttribute;
    }

    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
    }

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }

    public long getVersion() {
        return version;
    }

    @Override public String toString() {
        return "R{" +
            "id=" + id +
            ", someAttribute='" + someAttribute + '\'' +
            ", a=" + a +
            ", b=" + b +
            ", version=" + version +
            '}';
    }
}
