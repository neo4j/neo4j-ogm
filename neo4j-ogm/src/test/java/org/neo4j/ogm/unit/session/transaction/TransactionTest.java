package org.neo4j.ogm.unit.session.transaction;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.cypher.compiler.CypherContext;
import org.neo4j.ogm.domain.education.Teacher;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.session.transaction.Transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TransactionTest {

    private Transaction tx;
    private MappingContext mappingContext;

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.education");

    @Before
    public void setUp() {
        mappingContext = new MappingContext(metaData);
        tx = new Transaction(mappingContext, "", null);
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
        ClassInfo classInfo = metaData.classInfo(Teacher.class.getName());
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        mappingContext.remember(teacher);
        teacher.setName("St Ursula's");
        assertTrue(mappingContext.isDirty(teacher));

        CypherContext cypherContext = new CypherContext();
        cypherContext.log(teacher);

        tx.append(cypherContext);
        tx.commit();
        assertFalse(mappingContext.isDirty(teacher));

    }

}
