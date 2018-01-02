/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

import static com.google.common.collect.Lists.*;
import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.domain.social.User;
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.exception.TransactionException;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.transaction.Transaction;

import com.google.common.collect.Iterables;

/**
 * This test class is converted from the AbstractDriverTestSuite to use the test harness in use by toher tests
 *
 * @author vince
 * Do not rename this class to end with *Test, or certain test packages might try to execute it.
 */
public class BasicDriverTest extends MultiDriverTestClass {

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.social");
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    // save test
    @Test
    public void shouldSaveObject() {
        User user = new User("Bilbo Baggins");
        assertThat(user.getId()).isNull();
        session.save(user);
        assertThat(user.getId()).isNotNull();
    }

    @Test
    public void shouldSaveMultipleObjects() throws Exception {
        User bilbo = new User("Bilbo Baggins");
        User frodo = new User("Bilbo Baggins");
        bilbo.befriend(frodo);

        // Get an Iterable which is not a Collection
        Iterable<User> iterable = Iterables.concat(newArrayList(bilbo), newArrayList(frodo));
        assertThat(iterable).isNotInstanceOf(Collection.class);

        session.save(iterable);

        session.clear();
        Collection<User> users = session.loadAll(User.class);
        assertThat(users).hasSize(2);
    }

    // load tests
    @Test
    public void shouldLoadByType() {
        session.save(new User());
        session.clear();
        assertThat(session.loadAll(User.class)).hasSize(1);
    }

    @Test
    public void shouldLoadOne() {
        User user = new User();
        session.save(user);
        session.clear();
        User userByType = session.load(User.class, user.getId());
        assertThat(userByType).isNotNull();
    }

    @Test
    public void shouldLoadByProperty() {
        User user = new User("Bilbo Baggins");
        session.save(user);
        session.clear();
        User userByProperty = session
            .loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "Bilbo Baggins")).iterator().next();
        assertThat(userByProperty).isNotNull();
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
        assertThat(userByInstances).isNotNull();
        assertThat(userByInstances).hasSize(2);
    }

    @Test
    public void shouldLoadByIds() {
        User bilbo = new User("Bilbo Baggins");
        User frodo = new User("Frodo Baggins");
        session.save(bilbo);
        session.save(frodo);
        session.clear();
        Collection<User> userByInstances = session.loadAll(User.class, Arrays.asList(frodo.getId(), bilbo.getId()));
        assertThat(userByInstances).isNotNull();
        assertThat(userByInstances).hasSize(2);
    }

    // query tests
    @Test
    public void shouldQueryForObject() {
        session.save(new User("Bilbo Baggins"));
        session.clear();
        User bilbo = session.queryForObject(User.class, "MATCH(u:User) RETURN u", Utils.map("name", "Bilbo Baggins"));
        assertThat(bilbo).isNotNull();
    }

    @Test
    public void shouldQueryForDomainObjects() {
        session.save(new User("Bilbo Baggins"));
        session.save(new User("Frodo Baggins"));
        session.clear();
        Collection<User> users = (Collection) session
            .query(User.class, "MATCH(u:User) WHERE u.name =~ '.*Baggins' RETURN u", Utils.map());
        assertThat(users).isNotNull();
        assertThat(users).hasSize(2);
    }

    @Test
    public void shouldQueryForScalarValues() {
        session.save(new User("Bilbo Baggins"));
        session.save(new User("Frodo Baggins"));
        session.clear();
        Collection<String> userNames = (Collection) session
            .query(String.class, "MATCH(u:User) WHERE u.name =~ '.*Baggins' RETURN u.name", Utils.map());
        assertThat(userNames).isNotNull();
        assertThat(userNames).hasSize(2);
    }

    @Test
    public void shouldObtainEmptyQueryResultsWithStatistics() {
        session.save(new User("Bilbo Baggins"));
        session.save(new User("Frodo Baggins"));
        session.clear();
        Result result = session
            .query("MATCH (u:User) WHERE u.name =~ '.*Baggins' SET u.species = 'Hobbit'", Utils.map());
        assertThat(result.queryStatistics().getPropertiesSet()).isEqualTo(2);
        assertThat(result.queryResults().iterator().hasNext()).isFalse();
    }

    @Test
    public void shouldObtainQueryResultsWithStatistics() {
        session.save(new User("Bilbo Baggins"));
        session.save(new User("Frodo Baggins"));
        session.clear();
        Result result = session
            .query("MATCH (u:User) WHERE u.name =~ '.*Baggins' SET u.species = 'Hobbit' RETURN u.name", Utils.map());
        assertThat(result.queryStatistics().getPropertiesSet()).isEqualTo(2);
        assertThat(result.queryResults().iterator().hasNext()).isTrue();
    }

    //
    @Test
    public void shouldFindExplicitlyCommittedEntity() {

        Transaction tx = session.beginTransaction();
        session.save(new User());
        tx.commit();
        session.clear();
        assertThat(session.loadAll(User.class)).hasSize(1);
    }

    @Test
    public void shouldNotFindExplicitlyRolledBackEntity() {

        Transaction tx = session.beginTransaction();
        session.save(new User());
        tx.rollback();
        session.clear();
        assertThat(session.loadAll(User.class)).isEmpty();
    }

    @Test
    public void shouldFailExtendedCommitRollbackCommit() {
        try {
            doExtendedCommitRollbackCommit();
            fail("Should have thrown exception");
        } catch (TransactionException txe) {
            assertThat(session.loadAll(User.class)).isEmpty();
        }
    }

    @Test
    public void shouldFailExtendedRollbackCommitCommit() {
        try {
            doExtendedRollbackCommitCommit();
            fail("Should have thrown exception");
        } catch (TransactionException txe) {
            assertThat(session.loadAll(User.class)).isEmpty();
        }
    }

    @Test
    @Ignore
    public void shouldFailExtendedRollbackRollbackCommit() {
        try {
            doExtendedRollbackRollbackCommit();
            fail("Should have thrown exception");
        } catch (TransactionException txe) {
            assertThat(session.loadAll(User.class)).isEmpty();
        }
    }

    @Test
    public void shouldSucceedExtendedCommitCommitCommit() {
        doExtendedCommitCommitCommit();
        assertThat(session.loadAll(User.class)).hasSize(2);
    }

    @Test
    public void shouldSucceedExtendedCommitRollbackRollback() {
        doExtendedCommitRollbackRollback();
        assertThat(session.loadAll(User.class)).isEmpty();
    }

    @Test
    public void shouldSucceedExtendedRollbackCommitRollback() {
        try {
            doExtendedRollbackCommitRollback();
            fail("Should have caught exception"); // invalid transaction state after rollback, commit
        } catch (TransactionException txe) {
            assertThat(session.loadAll(User.class)).isEmpty();
        }
    }

    @Test
    public void shouldSucceedExtendedRollbackRollbackRollback() {
        doExtendedRollbackRollbackRollback();
        assertThat(session.loadAll(User.class)).isEmpty();
    }

    @Test
    public void shouldSucceedExtendedCommitCommitRollback() {
        doExtendedCommitCommitRollback();
        assertThat(session.loadAll(User.class)).isEmpty();
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
        } catch (CypherException ce) {
            assertThat(ce.getCode().contains("Neo.ClientError.Statement")).isTrue();
            assertThat(ce.getDescription().contains("Invalid input")).isTrue();
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
