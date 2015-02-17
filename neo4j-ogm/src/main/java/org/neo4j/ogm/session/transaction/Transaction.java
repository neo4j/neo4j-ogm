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

package org.neo4j.ogm.session.transaction;

import org.neo4j.ogm.cypher.compiler.CypherContext;

public interface Transaction extends AutoCloseable {


    /**
     * Adds a new cypher context to this transaction
     * @param context The CypherContext that forms part of this transaction when committed
     */
    void append(CypherContext context);

    /**
     * The endpoint for this transaction
     * @return
     */
    String url();

    /*
     * rollback a transaction that has pending writes
     * calling rollback on a transaction with no pending read/writes is an error
     */
    void rollback();

    /*
     * commit a transaction that has pending writes
     * calling commit on a transaction with no pending read/writes is an error
     */
    void commit();

    /**
     * return the status of the current transaction
     * @return the Status value associated with the current transaction
     */
    Status status();

    public enum Status {
        OPEN, PENDING, ROLLEDBACK, COMMITTED, CLOSED
    }

    void close();
}
