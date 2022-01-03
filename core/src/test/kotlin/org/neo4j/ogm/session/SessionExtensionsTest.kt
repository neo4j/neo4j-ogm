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
package org.neo4j.ogm.session

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertNull
import org.junit.Test
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.cypher.Filters
import org.neo4j.ogm.cypher.query.SortOrder

/**
 * @author Michael J. Simons
 */
class SessionExtensionsTest {

    val session = mockk<Session>(relaxed = true)
    val ids = listOf(1L)

    class SomeEntity

    @Test
    fun `loadAll(ids) extension should call its Java counterpart`() {

        session.loadAll<SomeEntity>(listOf(1L))

        verify(exactly = 1) { session.loadAll(SomeEntity::class.java, eq(ids), ofType(SortOrder::class), isNull(), 1) }
    }

    @Test
    fun `loadAll extension should call its Java counterpart`() {

        session.loadAll<SomeEntity>()

        verify(exactly = 1) { session.loadAll(SomeEntity::class.java, ofType(SortOrder::class), isNull(), 1) }
    }

    @Test
    fun `loadAll(filter) extension should call its Java counterpart`() {

        val filter = mockk<Filter>()
        session.loadAll<SomeEntity>(filter)

        verify(exactly = 1) { session.loadAll(SomeEntity::class.java, filter, ofType(SortOrder::class), isNull(), 1) }
    }

    @Test
    fun `loadAll(filters) extension should call its Java counterpart`() {

        val filters = Filters()
        session.loadAll<SomeEntity>(filters)

        verify(exactly = 1) { session.loadAll(SomeEntity::class.java, filters, ofType(SortOrder::class), isNull(), 1) }
    }

    @Test
    fun `load(id) extension should call its Java counterpart`() {

        session.load<SomeEntity>(23L)

        verify(exactly = 1) { session.load(SomeEntity::class.java, 23L, 1) }
    }

    @Test
    fun `load(id) extension should call its Java counterpart and allow null to be returned`() {
        every { session.load(any<Class<*>>(), any<Long>(), any()) } returns(null)

        val result = session.load<SomeEntity>(23L)

        verify(exactly = 1) { session.load(SomeEntity::class.java, 23L, 1) }
        assertNull(result)
    }

    @Test
    fun `deleteAll extension should call its Java counterpart`() {

        session.deleteAll<SomeEntity>()

        verify(exactly = 1) { session.deleteAll(SomeEntity::class.java) }
    }

    @Test
    fun `delete(filters, boolean) extension should call its Java counterpart`() {

        val filters = emptyList<Filter>()
        session.delete<SomeEntity>(filters, true)

        verify(exactly = 1) { session.delete(SomeEntity::class.java, filters, true) }
    }

    @Test
    fun `queryForObject extension should call its Java counterpart`() {

        val cypher = "MATCH (n:SomeEntity) RETURN n LIMIT 1"
        session.queryForObject<SomeEntity>(cypher)

        verify(exactly = 1) { session.queryForObject(SomeEntity::class.java, cypher, emptyMap<String, Any>()) }
    }

    @Test
    fun `queryForObject extension should call its Java counterpart and allow null to be returned`() {
        every { session.queryForObject(any<Class<*>>(), any(), any()) } returns(null)

        val cypher = "MATCH (n:SomeEntity) RETURN n LIMIT 1"
        val result = session.queryForObject<SomeEntity>(cypher)

        verify(exactly = 1) { session.queryForObject(SomeEntity::class.java, cypher, emptyMap<String, Any>()) }
        assertNull(result)
    }

    @Test
    fun `query extension should call its Java counterpart`() {

        val cypher = "MATCH (n:SomeEntity) RETURN n"
        session.query<SomeEntity>(cypher)

        verify(exactly = 1) { session.query(SomeEntity::class.java, cypher, emptyMap<String, Any>()) }
    }

    @Test
    fun `countEntitiesOfType extension should call its Java counterpart`() {

        session.countEntitiesOfType<SomeEntity>()

        verify(exactly = 1) { session.countEntitiesOfType(SomeEntity::class.java) }
    }

    @Test
    fun `count extension should call its Java counterpart`() {

        session.count<SomeEntity>(emptyList())

        verify(exactly = 1) { session.count(SomeEntity::class.java, emptyList()) }
    }
}
