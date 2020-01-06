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
package org.neo4j.ogm.domain.gh385;

import java.util.Collection;
import java.util.List;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * @author Michael J. Simons
 */
public class RichRelations {

    @NodeEntity("S")
    public static class S {
        @Id @GeneratedValue
        private Long id;

        private String name;

        @Relationship("R")
        Collection<R> r;

        public S() {
        }

        public S(String name) {
            this.name = name;
        }

        public Collection<R> getR() {
            return r;
        }

        public void setR(List<R> r) {
            this.r = r;
        }

        public Long getId() {
            return id;
        }
    }

    @RelationshipEntity("R")
    public static class R {
        @Id @GeneratedValue
        private Long id;

        @StartNode
        private S s;

        @EndNode
        private E e;

        private String name;

        public R() {
        }

        public R(S s, E e, String name) {
            this.s = s;
            this.e = e;
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public E getE() {
            return e;
        }
    }

    @NodeEntity("E")
    public static class E {
        @Id @GeneratedValue
        private Long id;

        private String name;

        public E() {
        }

        public E(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private RichRelations() {
    }
}
