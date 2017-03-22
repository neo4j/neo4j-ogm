package org.neo4j.ogm.persistence.model;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.*;
import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.persistence.examples.stage.edges.LastDrama;
import org.neo4j.ogm.persistence.examples.stage.edges.PlayedInDrama;
import org.neo4j.ogm.persistence.examples.stage.nodes.Drama;
import org.neo4j.ogm.persistence.examples.stage.nodes.StageActor;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * Issue #54, ##66, #186 & #298 (Zendesk ticket XXX)
 *
 * @author Mihai Raulea
 * @author Mark Angrish
 * @author Vince Bickers
 */

public class AbstractWithGenericPropertyRelationshipTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(baseConfiguration.build(), "org.neo4j.ogm.persistence.model", "org.neo4j.ogm.persistence.examples.stage");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void shouldReloadEntitiesJustSaved() {
        StageActor stageActor = new StageActor("first actor");
        Drama firstDrama = new Drama("malade imaginaire");
        Drama secondDrama = new Drama("Le cid");

        stageActor.dramas.add(new PlayedInDrama(stageActor, firstDrama, "rel1"));
        stageActor.dramas.add(new PlayedInDrama(stageActor, secondDrama, "rel2"));
        stageActor.lastDrama = new LastDrama(stageActor, secondDrama, "last");

        session.save(stageActor);

        session.clear();

        StageActor reloadedActor = session.load(StageActor.class, stageActor.id);

        Assert.assertEquals(2, reloadedActor.dramas.size());
        Assert.assertNotNull(reloadedActor.lastDrama);
    }

    /**
     * TODO:
     * This test is unclear to me in what it is expecting from the domain class setup. However, it should be possible
     * in theory to query by a generic type. Requires further investigation.
     */
    @Ignore
    @Test
    public void testQueryByGenericRelationshipType() {
        SomeNode<Integer> someNode = new SomeNode<>();
        session.save(someNode);

        SomeOtherNode someOtherNode = new SomeOtherNode();
        session.save(someOtherNode);

        RelationshipImplementation<Integer> relationshipImplementation = new RelationshipImplementation<>();
        relationshipImplementation.a = someNode;
        relationshipImplementation.b = someOtherNode;
        session.save(relationshipImplementation);

        AnotherRelationshipImplementation<Integer> anotherRelationshipImplementation = new AnotherRelationshipImplementation<>();
        anotherRelationshipImplementation.a = someNode;
        anotherRelationshipImplementation.b = someOtherNode;
        session.save(anotherRelationshipImplementation);

        // we can not query by abstract class
        Collection<L> relationship = session.loadAll(L.class);
        Assert.assertEquals(1, relationship.size());
    }

    @Test
    public void testGenericTypeInConcreteRelationship() {
        SomeNode<Integer> someNode = new SomeNode<>();
        session.save(someNode);

        SomeOtherNode someOtherNode = new SomeOtherNode();
        session.save(someOtherNode);

        RelationshipImplementation<Integer> relationshipImplementation = new RelationshipImplementation<>();
        relationshipImplementation.a = someNode;
        relationshipImplementation.b = someOtherNode;
        session.save(relationshipImplementation);

        AnotherRelationshipImplementation<String> anotherRelationshipImplementation = new AnotherRelationshipImplementation<>();
        anotherRelationshipImplementation.a = someNode;
        anotherRelationshipImplementation.b = someOtherNode;
        session.save(anotherRelationshipImplementation);

        AnotherRelationshipImplementation<String> retrieved = session.load(AnotherRelationshipImplementation.class, anotherRelationshipImplementation.id);
        Assert.assertEquals(retrieved.a.id, someNode.id);
    }

    @RelationshipEntity(type = "otherRelDynamicProperty")
    public class AnotherRelationshipImplementation<T> extends L<T> {

    }


    @RelationshipEntity(type = "relDynamicProperty")
    public class RelationshipImplementation<T> extends L<T> {

    }

    @RelationshipEntity(type = "abstractDynamicProperty")
    public abstract class L<T> {

        Long id;

        private T property;

        @StartNode
        public SomeNode a;

        @EndNode
        public SomeOtherNode b;
    }

    @NodeEntity
    public final class SomeNode<T> {

        Long id;

        @Relationship
        private List<L<T>> listOfLs;
    }

    @NodeEntity
    public final class SomeOtherNode {

        Long id;
    }
}
