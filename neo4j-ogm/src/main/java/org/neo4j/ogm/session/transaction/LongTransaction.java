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

import org.neo4j.ogm.mapper.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LongTransaction extends SimpleTransaction {

    private final Logger logger = LoggerFactory.getLogger(LongTransaction.class);

    private final TransactionManager transactionRequestHandler;

    public LongTransaction(MappingContext mappingContext, String url, TransactionManager transactionRequestHandler) {
        super(mappingContext, url);
        this.transactionRequestHandler = transactionRequestHandler;
    }

    public void commit() {
        transactionRequestHandler.commit(this);
        super.commit();
    }


    public void rollback() {
        transactionRequestHandler.rollback(this);
        super.rollback();
    }

    public void close() {
        if (this.status().equals(Status.OPEN) || this.status().equals(Status.PENDING)) {
            transactionRequestHandler.rollback(this);
        }
        super.close();
    }
}
