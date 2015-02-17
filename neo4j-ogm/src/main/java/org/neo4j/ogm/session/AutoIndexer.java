/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.session;

/**
 * The job of the autoIndexer is to ensure that attributes annotated with @Index in
 * the domain have an appropriate schema index created to improve fetch performance.
 *
 * e.g. given:
 *
 * @NodeEntity
 * class Node {
 *     @Index
 *     String key
 * }
 *
 * we would ensure that the following cypher gets executed:
 *
 *      CREATE INDEX on :Node(key)
 *
 * or, if the index was additionally constrained:
 *
 * @NodeEntity
 * class Node {
 *     @Index(unique=true)
 *     String key
 * }
 *
 * the following Cypher is appropriate, which creates an index in the background.
 *
 *      CREATE CONSTRAINT ON (node:Node) ASSERT node.key IS UNIQUE
 *
 * However, because the existence and state of schema indexes is not available
 * via Cypher, we would presumably have to use the REST API first to get schema
 * index info in order to know what actions to take (if any).
 *
 * Additionally, we have to be aware of situations where an @Index annotation is changed.
 * For example if a non-constrained index is made constrained or a constraint is removed.
 * Neo4j doesn't yet handle constraint changes atomically. The recommended approach is to
 * drop the old index and recreate the new one via two distinct steps. During this time,
 * existing execution plans are evicted and performance may suffer as a consequence.
 *
 * So the question arises: should we even support @Index, or should we expect a DBA function
 * to handle indexing and other tuning options externally?
 *
 */
public class AutoIndexer {
}
