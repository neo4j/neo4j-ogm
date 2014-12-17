package org.neo4j.ogm.unit.session.transaction;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.cypher.compiler.CypherContext;
import org.neo4j.ogm.domain.education.Teacher;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.session.transaction.Transaction;

import static org.junit.Assert.*;

public class TransactionTest {

    private Transaction tx;
    private MappingContext mappingContext;

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.education");

    @Before
    public void setUp() {
        mappingContext = new MappingContext(metaData);
        tx = new Transaction(mappingContext, "");
    }

    @Test public void createTransaction() {
        assertEquals(Transaction.OPEN, tx.status());
    }

    @Test public void testCommit() {
        tx.append(new CypherContext());
        tx.commit();
        assertEquals(Transaction.COMMITTED, tx.status());
    }

    @Test public void testRollback() {
        tx.append(new CypherContext());
        tx.rollback();
        assertEquals(Transaction.ROLLEDBACK, tx.status());

    }

    @Test(expected = RuntimeException.class) public void testCannotRollbackIfNothingToDo() {
        tx.rollback();
    }

    @Test(expected = RuntimeException.class) public void testCannotCommitIfNothingToDo() {
        tx.commit();
    }

    @Test(expected = RuntimeException.class) public void testCannotRollbackIfCommitted() {
        tx.append(new CypherContext());
        tx.commit();
        tx.rollback();
    }

    @Test(expected = RuntimeException.class) public void testCannotRollbackIfRolledBack() {
        tx.append(new CypherContext());
        tx.rollback();
        tx.rollback();
    }

    @Test(expected = RuntimeException.class) public void testCannotCommitIfCommitted() {
        tx.append(new CypherContext());
        tx.commit();
        tx.commit();
    }

    @Test(expected = RuntimeException.class) public void testCannotCommitIfRolledBack() {
        tx.append(new CypherContext());
        tx.rollback();
        tx.commit();
    }

    @Test(expected = RuntimeException.class) public void testAddNewOperationIfRolledBack() {
        tx.append(new CypherContext());
        tx.rollback();
        tx.append(new CypherContext());
    }

    @Test(expected = RuntimeException.class) public void testAddNewOperationIfCommitted() {
        tx.append(new CypherContext());
        tx.commit();
        tx.append(new CypherContext());
    }

    @Test public void testDirtyObjectIsNotDirtyAfterCommit() {
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
        tx.commit();

        // the mapping context should now be in sync with the persistent state
        assertFalse(mappingContext.isDirty(teacher));

    }

}
