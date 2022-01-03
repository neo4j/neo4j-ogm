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

import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.cypher.Filters
import org.neo4j.ogm.cypher.query.Pagination
import org.neo4j.ogm.cypher.query.SortOrder
import java.io.Serializable

/**
 * Extension for [Session.loadAll] leveraging reified type parameters.
 */
inline fun <reified T : Any> Session.loadAll(
        ids: Collection<Serializable>,
        sortOrder: SortOrder = SortOrder(),
        pagination: Pagination? = null,
        depth: Int = 1
): Collection<T> = loadAll(T::class.java, ids, sortOrder, pagination, depth)

/**
 * Extension for [Session.loadAll] leveraging reified type parameters.
 */
inline fun <reified T : Any> Session.loadAll(
        sortOrder: SortOrder = SortOrder(),
        pagination: Pagination? = null,
        depth: Int = 1
): Collection<T> = loadAll(T::class.java, sortOrder, pagination, depth)

/**
 * Extension for [Session.loadAll] leveraging reified type parameters.
 */
inline fun <reified T : Any> Session.loadAll(
        filter: Filter,
        sortOrder: SortOrder = SortOrder(),
        pagination: Pagination? = null,
        depth: Int = 1
): Collection<T> = loadAll(T::class.java, filter, sortOrder, pagination, depth)

/**
 * Extension for [Session.loadAll] leveraging reified type parameters.
 */
inline fun <reified T : Any> Session.loadAll(
        filters: Filters,
        sortOrder: SortOrder = SortOrder(),
        pagination: Pagination? = null,
        depth: Int = 1
): Collection<T> = loadAll(T::class.java, filters, sortOrder, pagination, depth)

/**
 * Extension for [Session.load] leveraging reified type parameters.
 */
inline fun <reified T : Any> Session.load(id: Serializable, depth: Int = 1): T? =
        load(T::class.java, id, depth)

/**
 * Extension for [Session.deleteAll] leveraging reified type parameters.
 */
inline fun <reified T : Any> Session.deleteAll(): Unit =
        deleteAll(T::class.java)

/**
 * Extension for [Session.delete] leveraging reified type parameters.
 */
inline fun <reified T : Any> Session.delete(filters : Iterable<Filter>, listResults: Boolean): Any =
        delete(T::class.java, filters, listResults)

/**
 * Extension for [Session.queryForObject] leveraging reified type parameters.
 */
inline fun <reified T : Any> Session.queryForObject(
        cypher: String,
        parameters: Map<String, Any> = emptyMap()
): T? = queryForObject(T::class.java, cypher, parameters)

/**
 * Extension for [Session.query] leveraging reified type parameters.
 */
inline fun <reified T : Any> Session.query(
        cypher: String,
        parameters: Map<String, Any> = emptyMap()
): Iterable<T> = query(T::class.java, cypher, parameters)

/**
 * Extension for [Session.countEntitiesOfType] leveraging reified type parameters.
 */
inline fun <reified T : Any> Session.countEntitiesOfType(): Long = countEntitiesOfType(T::class.java)

/**
 * Extension for [Session.countEntitiesOfType] leveraging reified type parameters.
 */
inline fun <reified T : Any> Session.count(filters: Iterable<Filter>): Long
        = count(T::class.java, filters)
