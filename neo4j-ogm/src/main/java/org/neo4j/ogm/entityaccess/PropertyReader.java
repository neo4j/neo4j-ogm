/*
 * Copyright (c) 2014-2015 "GraphAware"
 *
 * GraphAware Ltd
 *
 * This file is part of Neo4j-OGM.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.neo4j.ogm.entityaccess;

/**
 * Simple interface through which a particular property of a given object can be read.
 */
public interface PropertyReader {

    /**
     * Retrieves the property name as it would be written to the node or relationship in the graph database.
     *
     * @return The name of the property to write to the graph database property container
     */
    String propertyName();

    /**
     * Reads the value corresponding to this property from the given object.
     *
     * @param instance The instance from which to read the property value
     * @return The value of the property, which may be <code>null</code>
     * @throws RuntimeException if there's an error reading the property or if it's not found on the given object
     */
    Object read(Object instance);

}
