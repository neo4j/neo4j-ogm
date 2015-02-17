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

package org.neo4j.ogm.metadata;

/**
 * Specialised {@link RuntimeException} thrown when an unrecoverable issue occurs when mapping between objects and graphs.
 */
public class MappingException extends RuntimeException {

    private static final long serialVersionUID = -9160906479092232033L;

    /**
     * Constructs a new {@link MappingException} with the given reason message and cause.
     *
     * @param reasonMessage A message explaining the reason for this exception
     * @param cause The underlying {@link Exception} that was the root cause of the problem
     */
    public MappingException(String reasonMessage, Exception cause) {
        super(reasonMessage, cause);
    }

    /**
     * Constructs a new {@link MappingException} with the given message.
     *
     * @param message A message describing the reason for this exception
     */
    public MappingException(String message) {
        super(message);
    }

}
