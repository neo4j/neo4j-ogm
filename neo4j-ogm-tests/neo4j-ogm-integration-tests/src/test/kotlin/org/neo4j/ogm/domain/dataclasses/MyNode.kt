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
package org.neo4j.ogm.domain.dataclasses

import org.neo4j.ogm.annotation.*

/**
 * @author Michael J. Simons
 */
@NodeEntity
data class MyNode (
    @Id @GeneratedValue var dbId: Long? = null,
    @Index(unique = true) val name: String,
    val description: String,
    @Relationship("IS_LINKED_TO", direction = Relationship.OUTGOING)
    val otherNodes: List<OtherNode> = emptyList()
)

@NodeEntity("AA")
data class A(
    @Id val id: String? = null,
    @Relationship(type = "HAS_B")
    var b: MutableSet<B> = mutableSetOf()
)

@RelationshipEntity(type = "HAS_B")
class HasB @JvmOverloads constructor(
    @Id @GeneratedValue val id: Long? = null,
    @StartNode val start: A = A(),
    @EndNode val end: B = B()


) {
    override fun toString(): String {
        return "HasB(id=$id, end=$end)"
    }
}

@NodeEntity("BB")
data class B(
    @Id val id: String? = null,
    @Relationship(type = "HAS_C")
    val c: MutableSet<C> = mutableSetOf()
)

@NodeEntity("CC")
data class C(
    @Id val id: String? = null
)
