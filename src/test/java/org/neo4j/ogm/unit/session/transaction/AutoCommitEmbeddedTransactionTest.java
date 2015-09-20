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

package org.neo4j.ogm.unit.session.transaction;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.cypher.compiler.CypherContext;
import org.neo4j.ogm.domain.education.Teacher;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.driver.embedded.EmbeddedDriver;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.session.transaction.TransactionException;
import org.neo4j.ogm.session.transaction.TransactionManager;

import static org.junit.Assert.*;

/**
 * @author Vince Bickers
 */

public class AutoCommitEmbeddedTransactionTest {

    private Transaction tx;
    private MappingContext mappingContext;
    private TransactionManager transactionManager;
    private static final Driver driver = new EmbeddedDriver();

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.education");

    @Before
    public void setUp() {
        mappingContext = new MappingContext(metaData);
        transactionManager = new TransactionManager(driver);
        tx = transactionManager.openTransientTransaction(mappingContext);
    }

    @Test public void assertNewTransactionIsOpen() {
        assertEquals(Transaction.Status.OPEN, tx.status());
    }

    @Test public void assertCommitOperation() {
        tx.append(new CypherContext());
        assertEquals(Transaction.Status.COMMITTED, tx.status());
    }

    @Test public void assertRollbackOperation() {
        tx.rollback();
        assertEquals(Transaction.Status.ROLLEDBACK, tx.status());

    }

    @Test(expected = TransactionException.class) public void failRollbackIfCommitted() {
        tx.append(new CypherContext());
        tx.rollback();
    }

    @Test(expected = TransactionException.class) public void failRollbackIfRolledBack() {
        tx.rollback();
        tx.rollback();
    }

    @Test(expected = TransactionException.class) public void failCommitIfCommitted() {
        tx.append(new CypherContext());
        tx.commit();
    }

    @Test(expected = TransactionException.class) public void failCommitIfRolledBack() {
        tx.rollback();
        tx.commit();
    }

    @Test(expected = TransactionException.class) public void failNewOperationIfRolledBack() {
        tx.rollback();
        tx.append(new CypherContext());
    }

    @Test(expected = TransactionException.class) public void failNewOperationIfCommitted() {
        tx.append(new CypherContext());
        tx.append(new CypherContext());
    }

    @Test public void assertNotDirtyAfterCommit() {
        // 'load' a teacher
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        mappingContext.remember(teacher);

        // change the teacher's properties
        teacher.setName("Richard Feynman");
        assertTrue(mappingContext.isDirty(teacher));

        // create a new cypher context, representing the response from saving the teacher
        CypherContext cypherContext = new CypherContext();
        cypherContext.log(teacher);

        tx.append(cypherContext);


        // the mapping context should now be in sync with the persistent state
        assertFalse(mappingContext.isDirty(teacher));

    }


}
