/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.drivers;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.context.WriteProtectionTarget;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.domain.social.Immortal;
import org.neo4j.ogm.domain.social.Person;
import org.neo4j.ogm.domain.social.User;
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.exception.TransactionException;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.session.WriteProtectionStrategy;
import org.neo4j.ogm.testutil.TestContainersTestBase;
import org.neo4j.ogm.transaction.Transaction;

/**
 * This test class is converted from the AbstractDriverTestSuite to use the test harness in use by other tests.
 * <em>Do not rename this class to end with *Test, or certain test packages might try to execute it.</em>
 *
 * @author Vince Bickers
 * @author Michael J. Simons
 * @author Jared Hancock
 * @author Gerrit Meier
 */
public class BasicDriverTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.social");
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
        User frodo = new User("Frodo Beutlin");
        bilbo.befriend(frodo);

        // Get an Iterable which is not a Collection
        Iterable<User> iterable = Stream.of(bilbo, frodo)::iterator;
        assertThat(iterable).isNotInstanceOf(Collection.class);

        session.save(iterable);

        session.clear();
        Collection<User> users = session.loadAll(User.class);
        assertThat(users)
            .hasSize(2)
            .extracting(User::getName)
            .containsExactlyInAnyOrder("Bilbo Baggins", "Frodo Beutlin");
    }

    @Test
    public void shouldSaveMultipleObjectsWithWriteProtection() throws Exception {
        User bilbo = new User("Bilbo Baggins");
        session.save(bilbo);
        session.clear();

        try {
            ((Neo4jSession) session).addWriteProtection(
                WriteProtectionTarget.PROPERTIES,
                object -> (object instanceof User) && bilbo.getId().equals(((User) object).getId()));
            User frodo = new User("Frodo Beutlin");
            bilbo.befriend(frodo);
            bilbo.setName("The wrong name");

            // Get an Iterable which is not a Collection
            Iterable<User> iterable = Stream.of(bilbo, frodo)::iterator;
            assertThat(iterable).isNotInstanceOf(Collection.class);

            session.save(iterable);

            session.clear();
            Collection<User> users = session.loadAll(User.class);
            assertThat(users)
                .hasSize(2)
                .extracting(User::getName)
                .containsExactlyInAnyOrder("Bilbo Baggins", "Frodo Beutlin");
        } finally {
            ((Neo4jSession) session).removeWriteProtection(WriteProtectionTarget.PROPERTIES);
        }
    }

    @Test
    public void shouldSaveMultipleObjectsWithWriteProtectionFromRoot() throws Exception {
        User avon = new User("Avon Barksdale");
        session.save(avon);

        User stringer = new User("Stringer Bell");
        session.save(stringer);

        session.clear();

        try {
            // save only Avon's properties, protect neighboring nodes from writes
            ((Neo4jSession) session).addWriteProtection(
                WriteProtectionTarget.PROPERTIES,
                object -> (object instanceof User) && !avon.getId().equals(((User) object).getId()));
            stringer.setName("Marlo");
            avon.befriend(stringer);

            session.save(avon);
            session.clear();
            Collection<User> users = session.loadAll(User.class);
            assertThat(users)
                .hasSize(2)
                .extracting(User::getName)
                .containsExactlyInAnyOrder("Avon Barksdale", "Stringer Bell");
        } finally {
            ((Neo4jSession) session).removeWriteProtection(WriteProtectionTarget.PROPERTIES);
        }
    }

    @Test
    public void customWriteProtectionStrategyShouldBeApplied() {
        Predicate<Object> alwaysWriteProtect = o -> true;
        Predicate<Object> protectAvon = o -> ((User) o).getName().startsWith("Avon");

        WriteProtectionStrategy customStrategy = () -> (target, clazz) -> {
            // Mode ignored

            // If you need only protection for some Classes, this is the way to go.
            // You than can omit the class check  in the predicate
            if (clazz == Immortal.class) {
                return alwaysWriteProtect;
            } else if (clazz == User.class) {
                return protectAvon;
            } else { // if no predicate is return, we assume no write protection
                // Person.class in the example
                return null;
            }
        };

        User avon = new User("Avon Barksdale");
        session.save(avon);

        User stringer = new User("Stringer Bell");
        session.save(stringer);

        Immortal connor = new Immortal("Connor", "MacLeod");
        session.save(connor);

        Person person = new Person("A person");
        session.save(person);

        session.clear();

        try {
            ((Neo4jSession) session).setWriteProtectionStrategy(customStrategy);

            stringer.setName("Avon Something");
            avon.befriend(stringer);
            session.save(avon);

            connor.setFirstName("Duncan");
            session.save(connor);

            person.setName("John Reese");
            session.save(person);

            session.clear();

            Collection<User> users = session.loadAll(User.class);
            assertThat(users)
                .hasSize(2)
                .extracting(User::getName)
                .containsExactlyInAnyOrder("Avon Barksdale", "Stringer Bell");

            Collection<Immortal> immortals = session.loadAll(Immortal.class);
            assertThat(immortals)
                .hasSize(1)
                .extracting(Immortal::getFirstName)
                .containsExactlyInAnyOrder("Connor");

            Collection<Person> personsOfInterest = session.loadAll(Person.class);
            assertThat(personsOfInterest)
                .hasSize(1)
                .extracting(Person::getName)
                .containsExactlyInAnyOrder("John Reese");
        } finally {
            ((Neo4jSession) session).setWriteProtectionStrategy(null);
        }
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
        User bilbo = session.queryForObject(User.class, "MATCH(u:User) RETURN u", Collections
            .singletonMap("name", "Bilbo Baggins"));
        assertThat(bilbo).isNotNull();
    }

    @Test
    public void shouldQueryForDomainObjects() {
        session.save(new User("Bilbo Baggins"));
        session.save(new User("Frodo Baggins"));
        session.clear();
        Collection<User> users = (Collection) session
            .query(User.class, "MATCH(u:User) WHERE u.name =~ '.*Baggins' RETURN u", Collections.emptyMap());
        assertThat(users).isNotNull();
        assertThat(users).hasSize(2);
    }

    @Test
    public void shouldQueryForScalarValues() {
        session.save(new User("Bilbo Baggins"));
        session.save(new User("Frodo Baggins"));
        session.clear();
        Collection<String> userNames = (Collection) session
            .query(String.class, "MATCH(u:User) WHERE u.name =~ '.*Baggins' RETURN u.name", Collections.emptyMap());
        assertThat(userNames).isNotNull();
        assertThat(userNames).hasSize(2);
    }

    @Test
    public void shouldObtainEmptyQueryResultsWithStatistics() {
        session.save(new User("Bilbo Baggins"));
        session.save(new User("Frodo Baggins"));
        session.clear();
        Result result = session
            .query("MATCH (u:User) WHERE u.name =~ '.*Baggins' SET u.species = 'Hobbit'", Collections.emptyMap());
        assertThat(result.queryStatistics().getPropertiesSet()).isEqualTo(2);
        assertThat(result.iterator().hasNext()).isFalse();
    }

    @Test
    public void shouldObtainQueryResultsWithStatistics() {
        session.save(new User("Bilbo Baggins"));
        session.save(new User("Frodo Baggins"));
        session.clear();
        Result result = session
            .query("MATCH (u:User) WHERE u.name =~ '.*Baggins' SET u.species = 'Hobbit' RETURN u.name", Collections.emptyMap());
        assertThat(result.queryStatistics().getPropertiesSet()).isEqualTo(2);
        assertThat(result.iterator().hasNext()).isTrue();
    }

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

    @Test // GH-119
    public void shouldWrapUnderlyingException() {
        session.save(new User("Bilbo Baggins"));
        try {
            session.query(User.class, "MATCH(u:User) WHERE u.name ~ '.*Baggins' RETURN u", Collections.emptyMap());
            fail("Expected a CypherException but got none");
        } catch (CypherException ce) {
            assertThat(ce.getCode().contains("Neo.ClientError.Statement")).isTrue();
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
