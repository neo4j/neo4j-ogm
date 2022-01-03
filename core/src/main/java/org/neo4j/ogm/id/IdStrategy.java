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
package org.neo4j.ogm.id;

/**
 * Id generation strategy that allows custom implementations of id generation.
 * For simple use cases, implementing classes should provide a no-argument constructor and OGM will instantiate the
 * strategy.
 * For cases where OGM can't instantiate the strategy (e.g. because it has other dependencies) it must be registered
 * with the SessionFactory.
 *
 * @author Frantisek Hartman
 * @since 3.0
 */
public interface IdStrategy {

    /**
     * Generates new id for given entity
     *
     * @param entity saved entity
     * @return identifier of the entity
     */
    Object generateId(Object entity);
}
