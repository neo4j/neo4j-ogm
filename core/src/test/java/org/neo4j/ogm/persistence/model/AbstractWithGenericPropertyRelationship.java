package org.neo4j.ogm.persistence.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Issue #54
 * @author Mihai Raulea
 */
@Ignore
public class AbstractWithGenericPropertyRelationship {

    private static final SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.persistence.model");

    private Session session;


    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void testSave() {
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
        Assert.assertEquals(relationship.size(),1);
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
        Assert.assertEquals( retrieved.a.id, someNode.id);
    }

    @RelationshipEntity(type="otherRelDynamicProperty")
    public class AnotherRelationshipImplementation<T> extends L<T> {

    }


    @RelationshipEntity(type="relDynamicProperty")
    public class RelationshipImplementation<T> extends L<T> {

    }

    @RelationshipEntity(type="abstractDynamicProperty")
    public abstract class L<T> {

        Long id;

        private T property;

        @StartNode
        public SomeNode a;

        @EndNode
        public SomeOtherNode b;
    }

    @NodeEntity
    public final class SomeNode <T> {
        Long id;

        @Relationship
        private List<L<T>> listOfLs;

    }

    @NodeEntity
    public final class SomeOtherNode {
        Long id;
    }

}
