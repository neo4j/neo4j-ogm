/*
 * Copyright (c) 2002-2019 "Neo4j,"
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

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Michael J. Simons
 */
public class SimpleRelations {

    @NodeEntity("P")
    public static class P {
        @Id @GeneratedValue
        private Long id;

        private String name;

        @Relationship("HAS")
        private Collection<C> c;

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Collection<C> getC() {
            return c;
        }
    }

    @NodeEntity("C")
    public static class C {
        @Id @GeneratedValue
        private Long id;

        private String name;
    }

    private SimpleRelations() {
    }
}
