/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.session.transaction;

import org.neo4j.ogm.cypher.compiler.CypherContext;
import org.neo4j.ogm.session.Driver;
import org.neo4j.ogm.mapper.MappedRelationship;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.mapper.TransientRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class TransactionManager {

    private final Driver driver;
    private final Logger logger = LoggerFactory.getLogger(TransactionManager.class);

    private static final ThreadLocal<Transaction> transaction = new ThreadLocal<>();

    private final MappingContext mappingContext;

    public TransactionManager(Driver driver) {
        this(driver, null);
    }

    public TransactionManager(Driver driver, MappingContext context) {
        this.driver = driver;
        this.mappingContext = context;
        this.driver.setTransactionManager(this);
        transaction.remove();
    }

    /**
     * Opens a new transaction against a database instance.
     *
     * Instantiation of the transaction is left to the driver
     *
     * @return
     */// half-way house: we want drivers to be unaware of mapping contexts.
    public Transaction openTransaction() {
        if (transaction.get() == null) {
            transaction.set(driver.newTransaction());
            return transaction.get();
        } else {
            throw new TransactionException("Nested transactions not supported");
        }
    }


    /**
     * Rolls back the specified transaction.
     *
     * The actual job of rolling back the transaction is left to the relevant driver. if
     * this is successful, the transaction is detached from this thread.
     *
     * If the specified transaction is not the correct one for this thread, throws an exception
     *
     * @param tx the transaction to rollback
     */
    public void rollback(Transaction tx) {
        if (tx != transaction.get()) {
            throw new TransactionException("Transaction is not current for this thread");
        }
        transaction.remove();
    }

    /**
     * Commits the specified transaction.
     *
     * The actual job of committing the transaction is left to the relevant driver. if
     * this is successful, the transaction is detached from this thread.
     *
     * If the specified transaction is not the correct one for this thread, throws an exception
     *
     * @param tx the transaction to commit
     */
    public void commit(Transaction tx) {
        if (tx != transaction.get()) {
            throw new TransactionException("Transaction is not current for this thread");
        }

        transaction.remove();
        synchroniseSession(((AbstractTransaction) tx).contexts);
    }

    /**
     * Returns the current transaction for this thread, or null if none exists
     *
     * @return this thread's transaction
     */
    public Transaction getCurrentTransaction() {
        return transaction.get();
    }

    void synchroniseSession(Collection<CypherContext> contexts)  {

        for (CypherContext cypherContext : contexts) {

            logger.debug("Synchronizing transaction context " + cypherContext + " with session context");

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
                    logger.debug("remembering " + o);
                    mappingContext.remember(o);
                }
            }
            logger.debug("number of objects: " + cypherContext.log().size());
        }

        contexts.clear();
    }

}
