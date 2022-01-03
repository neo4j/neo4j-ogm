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
 * This strategy will use internally generated Long ids as entity ids
 *
 * @author Frantisek Hartman
 * @since 3.0
 */
public class InternalIdStrategy implements IdStrategy {

    /*
     * Internally this is just a placeholder class and is handled as a special case.
     * The id is assigned after creation of the node in the database.
     */

    @Override
    public Object generateId(Object entity) {
        return null;
    }

}
