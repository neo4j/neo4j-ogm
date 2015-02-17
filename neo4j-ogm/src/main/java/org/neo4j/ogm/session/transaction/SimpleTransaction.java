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
import org.neo4j.ogm.mapper.MappedRelationship;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.mapper.TransientRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SimpleTransaction implements Transaction {

    private final Logger logger = LoggerFactory.getLogger(Transaction.class);
    private final MappingContext mappingContext;
    private final String url;
    private final boolean autocommit;

    private final List<CypherContext> contexts;

    private Status status = Status.OPEN;

    public SimpleTransaction(MappingContext mappingContext, String url) {
        this.mappingContext = mappingContext;
        this.url = url;
        this.autocommit = url.endsWith("/commit");
        this.contexts = new ArrayList<>();
    }

    public final void append(CypherContext context) {
        logger.debug("Appending transaction context " + context);
        if (status == Status.OPEN || status == Status.PENDING) {
            contexts.add(context);
            status = Status.PENDING;
            if (autocommit) {
                commit();
            }
        } else {
            throw new TransactionException("Transaction is no longer open. Cannot accept new operations");
        }
    }

    public final String url() {
        return url;
    }

    public void rollback() {
        logger.info("rollback invoked");
        if (status == Status.OPEN || status == Status.PENDING) {
            contexts.clear();
            status = Status.ROLLEDBACK;
        } else {
            throw new TransactionException("Transaction is no longer open. Cannot rollback");
        }
    }

    public void commit() {
        logger.info("commit invoked");
        if (status == Status.OPEN || status == Status.PENDING) {
            synchroniseSession();
            status = Status.COMMITTED;
        } else {
            throw new TransactionException("Transaction is no longer open. Cannot commit");
        }
    }

    public final Status status() {
        return status;
    }

    public void close() {
        status = Status.CLOSED;
    }

    private void synchroniseSession()  {

        for (CypherContext cypherContext : contexts) {

            logger.info("Synchronizing transaction context " + cypherContext + " with session context");

            for (Object o : cypherContext.log())  {
                logger.debug("checking cypher context object: " + o);
                if (o instanceof MappedRelationship) {
                    MappedRelationship mappedRelationship = (MappedRelationship) o;
                    if (mappedRelationship.isActive()) {
                        logger.debug("activating (${})-[:{}]->(${})", mappedRelationship.getStartNodeId(), mappedRelationship.getRelationshipType(), mappedRelationship.getEndNodeId());
                        mappingContext.registerRelationship((MappedRelationship) o);
                    } else {
                        logger.debug("de-activating (${})-[:{}]->(${})", mappedRelationship.getStartNodeId(), mappedRelationship.getRelationshipType(), mappedRelationship.getEndNodeId());
                        mappingContext.mappedRelationships().remove(mappedRelationship);
                    }
                } else if (!(o instanceof TransientRelationship)) {
                    mappingContext.remember(o);
                }
            }
            logger.debug("checked objects: " + cypherContext.log().size());
        }

        logger.debug("relationships registered active:");

        for (MappedRelationship mappedRelationship : mappingContext.mappedRelationships()) {
            logger.debug("(${})-[:{}]->(${})", mappedRelationship.getStartNodeId(), mappedRelationship.getRelationshipType(), mappedRelationship.getEndNodeId());
        }

        contexts.clear();
    }


}