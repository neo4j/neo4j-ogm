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

package org.neo4j.ogm.integration;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;
import org.neo4j.ogm.session.transaction.TransactionManager;
import org.neo4j.ogm.session.transaction.Transaction;

import static org.junit.Assert.assertEquals;

public class TransactionRequestHandlerTest extends IntegrationTest {

    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

    @Test
    public void testCreateLongTransaction() {

        TransactionManager txRequestHandler = new TransactionManager(httpClient, "http://localhost:" + neoPort);
        try (Transaction tx = txRequestHandler.openTransaction(null)) {
            assertEquals(Transaction.Status.OPEN, tx.status());
        }
    }

    @Test
    public void testCreateConcurrentTransactions() {

        TransactionManager txRequestHandler = new TransactionManager(httpClient, "http://localhost:" + neoPort);

        // note that the try-with-resources implies these transactions are nested, but they are in fact independent
        try (Transaction tx1 = txRequestHandler.openTransaction(null)) {
            try (Transaction tx2 = txRequestHandler.openTransaction(null)) {
                assertEquals(Transaction.Status.OPEN, tx1.status());
                assertEquals(Transaction.Status.OPEN, tx2.status());
            }
        }
    }

}
