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

package org.neo4j.ogm.mapper;

import org.neo4j.ogm.model.GraphModel;

import java.util.Collection;

/**
 * Specification for an object-graph mapper, which can map {@link org.neo4j.ogm.model.GraphModel}s onto arbitrary Java objects.
 *
 * @param <G> The Graph implementation
 */
public interface GraphToEntityMapper<G extends GraphModel> {

    /**
     * Maps the data representation in the given {@link org.neo4j.ogm.model.GraphModel} onto an instance of <code>T</code>.
     *
     * @param graphModel The {@link org.neo4j.ogm.model.GraphModel} model containing the data to map onto the object
     * @return An object of type <code>T</code> containing relevant data extracted from the given graph model
     */
    <T> Collection<T> map(Class<T> type, G graphModel);

}
