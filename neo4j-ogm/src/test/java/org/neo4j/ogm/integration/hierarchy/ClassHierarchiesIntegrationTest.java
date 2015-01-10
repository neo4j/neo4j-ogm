package org.neo4j.ogm.integration.hierarchy;

import com.graphaware.test.integration.WrappingServerIntegrationTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;
import org.neo4j.ogm.integration.hierarchy.domain.annotated.*;
import org.neo4j.ogm.integration.hierarchy.domain.plain.*;
import org.neo4j.ogm.integration.hierarchy.domain.trans.*;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.tooling.GlobalGraphOperations;

import java.io.IOException;

import static com.graphaware.test.unit.GraphUnit.assertSameGraph;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Integration test for label-based mapping of class hierarchies.
 *
 * The rules should be as follows:
 *
 * - any plain concrete class in the hierarchy generates a label by default
 * - plain abstract class does not generate a label by default
 * - any class annotated with @NodeEntity or @NodeEntity(label="something") generates a label
 * - empty or null labels must not be allowed
 * - classes / hierarchies that are not to be persisted must be annotated with @Transient
 */
@Ignore //todo many failures
public class ClassHierarchiesIntegrationTest extends WrappingServerIntegrationTest {

    private Session session;

    @Override
    protected int neoServerPort() {
        return 7896;
    }

    @Before
    public void init() throws IOException {
        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.integration.hierarchy.domain");
        session = sessionFactory.openSession("http://localhost:" + 7896);
    }

    @Test
    public void annotatedChildWithAnnotatedAbstractNamedParent() {
        session.save(new AnnotatedChildWithAnnotatedAbstractNamedParent());

        assertSameGraph(getDatabase(), "CREATE (:AnnotatedChildWithAnnotatedAbstractNamedParent:Parent)");

        assertNotNull(session.load(AnnotatedChildWithAnnotatedAbstractNamedParent.class, 0L));
    }

    @Test
    public void annotatedChildWithAnnotatedAbstractParent() {
        session.save(new AnnotatedChildWithAnnotatedAbstractParent());

        assertSameGraph(getDatabase(), "CREATE (:AnnotatedChildWithAnnotatedAbstractParent:AnnotatedAbstractParent)");

        assertNotNull(session.load(AnnotatedChildWithAnnotatedAbstractParent.class, 0L));
    }

    @Test
    public void annotatedChildWithAnnotatedConcreteNamedParent() {
        session.save(new AnnotatedChildWithAnnotatedConcreteNamedParent());

        assertSameGraph(getDatabase(), "CREATE (:AnnotatedChildWithAnnotatedConcreteNamedParent:Parent)");

        assertNotNull(session.load(AnnotatedChildWithAnnotatedConcreteNamedParent.class, 0L));
    }

    @Test
    public void annotatedChildWithAnnotatedConcreteParent() {
        session.save(new AnnotatedChildWithAnnotatedConcreteParent());

        assertSameGraph(getDatabase(), "CREATE (:AnnotatedChildWithAnnotatedConcreteParent:AnnotatedConcreteParent)");

        assertNotNull(session.load(AnnotatedChildWithAnnotatedConcreteParent.class, 0L));
    }

    @Test
    public void annotatedChildWithPlainAbstractParent() {
        session.save(new AnnotatedChildWithPlainAbstractParent());

        assertSameGraph(getDatabase(), "CREATE (:AnnotatedChildWithPlainAbstractParent)");

        assertNotNull(session.load(AnnotatedChildWithPlainAbstractParent.class, 0L));
    }

    @Test
    public void annotatedChildWithPlainConcreteParent() {
        session.save(new AnnotatedChildWithPlainConcreteParent());

        assertSameGraph(getDatabase(), "CREATE (:AnnotatedChildWithPlainConcreteParent:PlainConcreteParent)");

        assertNotNull(session.load(AnnotatedChildWithPlainConcreteParent.class, 0L));
    }

    @Test
    public void annotatedNamedChildWithAnnotatedAbstractNamedParent() {
        session.save(new AnnotatedNamedChildWithAnnotatedAbstractNamedParent());

        assertSameGraph(getDatabase(), "CREATE (:Child:Parent)");

        assertNotNull(session.load(AnnotatedNamedChildWithAnnotatedAbstractNamedParent.class, 0L));
    }

    @Test
    public void annotatedNamedChildWithAnnotatedAbstractParent() {
        session.save(new AnnotatedNamedChildWithAnnotatedAbstractParent());

        assertSameGraph(getDatabase(), "CREATE (:Child:AnnotatedAbstractParent)");

        assertNotNull(session.load(AnnotatedNamedChildWithAnnotatedAbstractParent.class, 0L));
    }

    @Test
    public void annotatedNamedChildWithAnnotatedConcreteNamedParent() {
        session.save(new AnnotatedNamedChildWithAnnotatedConcreteNamedParent());

        assertSameGraph(getDatabase(), "CREATE (:Child:Parent)");

        assertNotNull(session.load(AnnotatedNamedChildWithAnnotatedConcreteNamedParent.class, 0L));
    }

    @Test
    public void annotatedNamedChildWithAnnotatedConcreteParent() {
        session.save(new AnnotatedNamedChildWithAnnotatedConcreteParent());

        assertSameGraph(getDatabase(), "CREATE (:Child:AnnotatedConcreteParent)");

        assertNotNull(session.load(AnnotatedNamedChildWithAnnotatedConcreteParent.class, 0L));
    }

    @Test
    public void annotatedNamedChildWithPlainAbstractParent() {
        session.save(new AnnotatedNamedChildWithPlainAbstractParent());

        assertSameGraph(getDatabase(), "CREATE (:Child)");

        assertNotNull(session.load(AnnotatedNamedChildWithPlainAbstractParent.class, 0L));
    }

    @Test
    public void annotatedNamedChildWithPlainConcreteParent() {
        session.save(new AnnotatedChildWithPlainConcreteParent());

        assertSameGraph(getDatabase(), "CREATE (:Child:PlainConcreteParent)");

        assertNotNull(session.load(AnnotatedChildWithPlainConcreteParent.class, 0L));
    }

    @Test
    public void annotatedNamedSingleClass() {
        session.save(new AnnotatedSingleClass());

        assertSameGraph(getDatabase(), "CREATE (:Single)");

        assertNotNull(session.load(AnnotatedNamedSingleClass.class, 0L));
    }

    @Test
    public void annotatedSingleClass() {
        session.save(new AnnotatedSingleClass());

        assertSameGraph(getDatabase(), "CREATE (:AnnotatedSingleClass)");

        assertNotNull(session.load(AnnotatedSingleClass.class, 0L));
    }

    @Test
    public void plainChildWithAnnotatedAbstractNamedParent() {
        session.save(new PlainChildWithAnnotatedAbstractNamedParent());

        assertSameGraph(getDatabase(), "CREATE (:PlainChildWithAnnotatedAbstractNamedParent:Parent)");

        assertNotNull(session.load(PlainChildWithAnnotatedAbstractNamedParent.class, 0L));
    }

    @Test
    public void plainChildWithAnnotatedAbstractParent() {
        session.save(new PlainChildWithAnnotatedAbstractParent());

        assertSameGraph(getDatabase(), "CREATE (:PlainChildWithAnnotatedAbstractParent:AnnotatedAbstractParent)");

        assertNotNull(session.load(PlainChildWithAnnotatedAbstractParent.class, 0L));
    }

    @Test
    public void plainChildWithAnnotatedConcreteNamedParent() {
        session.save(new PlainChildWithAnnotatedConcreteNamedParent());

        assertSameGraph(getDatabase(), "CREATE (:PlainChildWithAnnotatedConcreteNamedParent:Parent)");

        assertNotNull(session.load(PlainChildWithAnnotatedConcreteNamedParent.class, 0L));
    }

    @Test
    public void plainChildWithAnnotatedConcreteParent() {
        session.save(new PlainChildWithAnnotatedConcreteParent());

        assertSameGraph(getDatabase(), "CREATE (:PlainChildWithAnnotatedConcreteParent:AnnotatedConcreteParent)");

        assertNotNull(session.load(PlainChildWithAnnotatedConcreteParent.class, 0L));
    }

    @Test
    public void plainChildWithPlainAbstractParent() {
        session.save(new PlainChildWithPlainAbstractParent());

        assertSameGraph(getDatabase(), "CREATE (:PlainChildWithPlainAbstractParent)");

        assertNotNull(session.load(PlainChildWithPlainAbstractParent.class, 0L));
    }

    @Test
    public void plainChildWithPlainConcreteParent() {
        session.save(new PlainChildWithPlainConcreteParent());

        assertSameGraph(getDatabase(), "CREATE (:PlainChildWithPlainConcreteParent:PlainConcreteParent)");

        assertNotNull(session.load(PlainChildWithPlainConcreteParent.class, 0L));
    }

    @Test
    public void plainSingleClass() {
        session.save(new PlainSingleClass());

        assertSameGraph(getDatabase(), "CREATE (:PlainSingleClass)");

        assertNotNull(session.load(PlainSingleClass.class, 0L));
    }

    @Test
    public void plainChildOfTransientParent() {
        session.save(new PlainChildOfTransientParent());

        try (Transaction tx = getDatabase().beginTx()) {
            assertFalse(GlobalGraphOperations.at(getDatabase()).getAllNodes().iterator().hasNext());
            tx.success();
        }
    }

    @Test
    public void transientChildWithPlainConcreteParent() {
        session.save(new TransientChildWithPlainConcreteParent());

        try (Transaction tx = getDatabase().beginTx()) {
            assertFalse(GlobalGraphOperations.at(getDatabase()).getAllNodes().iterator().hasNext());
            tx.success();
        }
    }

    @Test
    public void transientSingleClass() {
        session.save(new TransientSingleClass());

        try (Transaction tx = getDatabase().beginTx()) {
            assertFalse(GlobalGraphOperations.at(getDatabase()).getAllNodes().iterator().hasNext());
            tx.success();
        }
    }

    @Test
    public void transientSingleClassWithId() {
        session.save(new TransientSingleClassWithId());

        try (Transaction tx = getDatabase().beginTx()) {
            assertFalse(GlobalGraphOperations.at(getDatabase()).getAllNodes().iterator().hasNext());
            tx.success();
        }
    }

    @Test
    public void plainClassWithTransientFields() {
        PlainClassWithTransientFields toSave = new PlainClassWithTransientFields();
        toSave.setAnotherTransientField(new PlainSingleClass());
        toSave.setTransientField(new PlainChildOfTransientParent());
        toSave.setYetAnotherTransientField(new PlainSingleClass());

        session.save(toSave);

        assertSameGraph(getDatabase(), "CREATE (:PlainClassWithTransientFields)");

        assertNotNull(session.load(PlainClassWithTransientFields.class, 0L));
    }

    @Test(expected = RuntimeException.class)
    //todo fix, happily loads a completely different class
    public void shouldNotBeAbleToLoadClassOfWrongType() {
        session.save(new AnnotatedNamedSingleClass());
        session.load(PlainSingleClass.class, 0L);
    }
}
