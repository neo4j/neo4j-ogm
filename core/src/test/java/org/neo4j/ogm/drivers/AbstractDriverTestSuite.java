/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */
package org.neo4j.ogm.drivers;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.domain.social.User;
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.exception.TransactionException;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author vince
 *
 * Do not rename this class to end with *Test, or certain test packages might try to execute it.
 */
public abstract class AbstractDriverTestSuite {

    private SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.social");
    private Session session;

    public abstract void setUpTest();
    public abstract void tearDownTest();

	protected static void deleteExistingEmbeddedDatabase() {
		try {
			FileUtils.forceDelete(new File(new URI(Components.getConfiguration().driverConfiguration().getURI())));
		} catch (IOException ioe) {
			// ignore - nothing to delete
		} catch (URISyntaxException use) {
			throw new RuntimeException(use); // invalid file URI
		}
	}

    @Before
    public void init() {
        setUpTest();
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @After
    public void cleanUp() {
        tearDownTest();
    }

    // save test
    @Test
    public void shouldSaveObject() {
        User user = new User("Bilbo Baggins");
        assertNull(user.getId());
        session.save(user);
        assertNotNull(user.getId());
    }


    // load tests
    @Test
    public void shouldLoadByType() {
        session.save(new User());
        session.clear();
        assertEquals(1, session.loadAll(User.class).size());
    }

    @Test
    public void shouldLoadOne() {
        User user = new User();
        session.save(user);
        session.clear();
        User userByType = session.load(User.class, user.getId());
        assertNotNull(userByType);
    }

    @Test
    public void shouldLoadByProperty() {
        User user = new User("Bilbo Baggins");
        session.save(user);
        session.clear();
        User userByProperty = session.loadAll(User.class, new Filter("name", "Bilbo Baggins")).iterator().next();
        assertNotNull(userByProperty);
    }

    @Test
    public void shouldLoadByInstances() {
        User bilbo = new User("Bilbo Baggins");
        User frodo = new User("Frodo Baggins");
        List<User> users = new ArrayList<>();
        users.add(bilbo);
        users.add(frodo);
        session.save(users);
        session.clear();
        Collection<User> userByInstances = session.loadAll(users);
        assertNotNull(userByInstances);
        assertEquals(2, userByInstances.size());
    }

    @Test
    public void shouldLoadByIds() {
        User bilbo = new User("Bilbo Baggins");
        User frodo = new User("Frodo Baggins");
        session.save(bilbo);
        session.save(frodo);
        session.clear();
        Collection<User> userByInstances = session.loadAll(User.class, Arrays.asList(frodo.getId(), bilbo.getId()));
        assertNotNull(userByInstances);
        assertEquals(2, userByInstances.size());
    }

    // query tests
    @Test
    public void shouldQueryForObject() {
        session.save(new User("Bilbo Baggins"));
        session.clear();
        User bilbo = session.queryForObject(User.class, "MATCH(u:User) RETURN u", Utils.map("name", "Bilbo Baggins"));
        assertNotNull(bilbo);
    }

    @Test
    public void shouldQueryForDomainObjects() {
        session.save(new User("Bilbo Baggins"));
        session.save(new User("Frodo Baggins"));
        session.clear();
        Collection<User> users = (Collection) session.query(User.class, "MATCH(u:User) WHERE u.name =~ '.*Baggins' RETURN u", Utils.map());
        assertNotNull(users);
        assertEquals(2, users.size());
    }

    @Test
    public void shouldQueryForScalarValues() {
        session.save(new User("Bilbo Baggins"));
        session.save(new User("Frodo Baggins"));
        session.clear();
        Collection<String> userNames = (Collection) session.query(String.class, "MATCH(u:User) WHERE u.name =~ '.*Baggins' RETURN u.name", Utils.map());
        assertNotNull(userNames);
        assertEquals(2, userNames.size());
    }

    @Test
    public void shouldObtainEmptyQueryResultsWithStatistics() {
        session.save(new User("Bilbo Baggins"));
        session.save(new User("Frodo Baggins"));
        session.clear();
        Result result = session.query("MATCH (u:User) WHERE u.name =~ '.*Baggins' SET u.species = 'Hobbit'", Utils.map());
        assertEquals(2, result.queryStatistics().getPropertiesSet());
        assertFalse(result.queryResults().iterator().hasNext());
    }

    @Test
    public void shouldObtainQueryResultsWithStatistics() {
        session.save(new User("Bilbo Baggins"));
        session.save(new User("Frodo Baggins"));
        session.clear();
        Result result = session.query("MATCH (u:User) WHERE u.name =~ '.*Baggins' SET u.species = 'Hobbit' RETURN u.name", Utils.map());
        assertEquals(2, result.queryStatistics().getPropertiesSet());
        assertTrue(result.queryResults().iterator().hasNext());
    }

    //
    @Test
    public void shouldFindExplicitlyCommittedEntity() {

        Transaction tx = session.beginTransaction();
        session.save(new User());
        tx.commit();
        session.clear();
        assertEquals(1, session.loadAll(User.class).size());
    }


    @Test
    public void shouldNotFindExplicitlyRolledBackEntity() {

        Transaction tx = session.beginTransaction();
        session.save(new User());
        tx.rollback();
        session.clear();
        assertEquals(0, session.loadAll(User.class).size());
    }

    @Test
    public void shouldFailExtendedCommitRollbackCommit() {
        try {
            doExtendedCommitRollbackCommit();
            fail("Should have thrown exception");
        } catch (TransactionException txe) {
            assertEquals(0, session.loadAll(User.class).size());
        }
    }

    @Test
    public void shouldFailExtendedRollbackCommitCommit() {
        try {
            doExtendedRollbackCommitCommit();
            fail("Should have thrown exception");
        } catch (TransactionException txe) {
            assertEquals(0, session.loadAll(User.class).size());
        }
    }

    @Test
    @Ignore
    public void shouldFailExtendedRollbackRollbackCommit() {
        try {
            doExtendedRollbackRollbackCommit();
            fail("Should have thrown exception");
        } catch (TransactionException txe) {
            assertEquals(0, session.loadAll(User.class).size());
        }
    }

    @Test
    public void shouldSucceedExtendedCommitCommitCommit() {
        doExtendedCommitCommitCommit();
        assertEquals(2, session.loadAll(User.class).size());
    }

    @Test
    public void shouldSucceedExtendedCommitRollbackRollback() {
        doExtendedCommitRollbackRollback();
        assertEquals(0, session.loadAll(User.class).size());
    }

    @Test
    public void shouldSucceedExtendedRollbackCommitRollback() {
        try {
            doExtendedRollbackCommitRollback();
            fail("Should have caught exception"); // invalid transaction state after rollback, commit
        } catch (TransactionException txe) {
            assertEquals(0, session.loadAll(User.class).size());
        }
    }

    @Test
    public void shouldSucceedExtendedRollbackRollbackRollback() {
        doExtendedRollbackRollbackRollback();
        assertEquals(0, session.loadAll(User.class).size());
    }

    @Test
    public void shouldSucceedExtendedCommitCommitRollback() {
        doExtendedCommitCommitRollback();
        assertEquals(0, session.loadAll(User.class).size());
    }

	/**
     * @see issue 119
     */
    @Test
    public void shouldWrapUnderlyingException() {
        session.save(new User("Bilbo Baggins"));
        try {
           session.query(User.class, "MATCH(u:User) WHERE u.name ~ '.*Baggins' RETURN u", Utils.map());
           fail("Expected a CypherException but got none");
        }
        catch (CypherException ce) {
            assertTrue(ce.getCode().contains("Neo.ClientError.Statement"));
            assertTrue(ce.getDescription().contains("Invalid input"));
        }

    }

    private void doExtendedCommitRollbackCommit() throws TransactionException {
        try (Transaction tx = session.beginTransaction()) {
            m2(); // commit_deferred
            m3(); // rollback_deferred
            tx.commit(); // cannot commit outer transaction
        }
    }

    private void doExtendedRollbackCommitCommit() throws TransactionException {
        try (Transaction tx = session.beginTransaction()) {
            m3(); // rollback_deferred
            m2(); // commit_deferred
            tx.commit(); // cannot commit outer transaction
        }
    }

    private void doExtendedCommitCommitCommit() {
        try (Transaction tx = session.beginTransaction()) {
            m2(); // commit_deferred
            m2(); // commit_deferred
            tx.commit(); // should be able to commit outer transaction
        }
    }

    private void doExtendedRollbackRollbackCommit() throws TransactionException {
        try (Transaction tx = session.beginTransaction()) {
            m3(); // rollback_deferred
            m3(); // rollback_deferred
            tx.commit(); // cannot commit outer transaction
        }
    }


    private void doExtendedCommitRollbackRollback() {
        try (Transaction tx = session.beginTransaction()) {
            m2(); // commit_deferred
            m3(); // rollback_deferred
            tx.rollback(); // cannot commit outer transaction
        }
    }

    private void doExtendedRollbackCommitRollback() {
        try (Transaction tx = session.beginTransaction()) {
            m3(); // rollback_deferred
            m2(); // commit_deferred
            tx.rollback();
        }
    }

    private void doExtendedCommitCommitRollback() {
        try (Transaction tx = session.beginTransaction()) {
            m2(); // commit_deferred
            m2(); // commit_deferred
            tx.rollback();
        }
    }

    private void doExtendedRollbackRollbackRollback() {
        try (Transaction tx = session.beginTransaction()) {
            m3(); // rollback_deferred
            m3(); // rollback_deferred
            tx.rollback(); // won't commit outer transaction
        }
    }


    private void m2() { // inner transaction commits (defers commit)
        try (Transaction tx = session.beginTransaction()) {
            session.save(new User());
            tx.commit();
        }
    }

    private void m3() {  // inner transaction rolls back (defers rollback)
        try (Transaction tx = session.beginTransaction()) {
            session.save(new User());
            tx.rollback();
        }
    }


}
