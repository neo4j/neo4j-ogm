package org.neo4j.ogm.persistence.relationships.direct.withrelentity;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Gerrit Meier
 */
public class IgnoreRelationshipEntityTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetup() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.persistence.relationships.direct.withrelentity");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @After
    public void cleanup() throws IOException {
        session.purgeDatabase();
    }

    @Test
    public void ignoreRelationshipDefinitionIfDirectRelationshipIsPresentOutgoing() {
        C c1 = new C("c1");
        C c2 = new C("c2");
        Set<C> cs = new HashSet<>();
        cs.add(c1);
        cs.add(c2);
        B b = new B("b", cs);
        Set<B> bs = new HashSet<>();
        bs.add(b);
        A a = new A("a", bs);

        session.save(a);
        session = sessionFactory.openSession();

        A loaded = session.load(A.class, "a", -1);

        assertThat(loaded).isNotNull();
        Set<B> loadedBs = loaded.getB();
        assertThat(loadedBs).hasSize(1);
        B loadedB = loadedBs.iterator().next();
        assertThat(loadedB.id).isEqualTo("b");
        Set<C> loadedCs = loadedB.c;
        assertThat(loadedCs).hasSize(2);
        assertThat(loadedCs).containsExactlyInAnyOrder(c1, c2);

    }

    @Test
    public void ignoreRelationshipDefinitionIfDirectRelationshipIsPresentIncoming() {
        A a = new A();
        a.id = "target";
        D d = new D();
        d.a = a;
        d.id = "source";

        session.save(d);
        session = sessionFactory.openSession();

        D loaded = session.load(D.class, "source", -1);

        assertThat(loaded).isNotNull();
        A loadedA = loaded.a;
        assertThat(loadedA).isNotNull();
        assertThat(loadedA.id).isEqualTo("target");

    }

    // Scenario for OUTGOING relationship
    @NodeEntity("AA")
    public static class A {
        @Id String id;
        @Relationship(type = "HAS_B")
        Set<B> b;

        public A() {
        }

        public A(String id, Set<B> b) {
            this.id = id;
            this.b = b;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Set<B> getB() {
            return b;
        }

        public void setB(Set<B> b) {
            this.b = b;
        }

        @Override public String toString() {
            return "A{" +
                "id='" + id + '\'' +
                ", b=" + b +
                '}';
        }
    }

    @RelationshipEntity(type = "HAS_B")
    public static class HasB {

        @Id @GeneratedValue Long id;
        @StartNode A startNode;
        @EndNode B endNode;

        public HasB() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public A getStartNode() {
            return startNode;
        }

        public void setStartNode(A startNode) {
            this.startNode = startNode;
        }

        public B getEndNode() {
            return endNode;
        }

        public void setEndNode(B endNode) {
            this.endNode = endNode;
        }

        @Override public String toString() {
            return "HasB{" +
                "id=" + id +
                ", startNode=" + startNode +
                ", endNode=" + endNode +
                '}';
        }
    }

    @NodeEntity("BB")
    public static class B {
        @Id String id;
        @Relationship(type = "HAS_C")
        Set<C> c;

        public B() {
        }

        public B(String id, Set<C> c) {
            this.id = id;
            this.c = c;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Set<C> getC() {
            return c;
        }

        public void setC(Set<C> c) {
            this.c = c;
        }

        @Override public String toString() {
            return "B{" +
                "id='" + id + '\'' +
                ", c=" + c +
                '}';
        }
    }

    @NodeEntity("CC")
    public static class C {
        @Id String id;

        public C() {
        }

        public C(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            C c = (C) o;
            return Objects.equals(id, c.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override public String toString() {
            return "C{" +
                "id='" + id + '\'' +
                '}';
        }
    }

    // classes for INCOMING
    @RelationshipEntity("HAS_D")
    public static class HasD {
        @Id @GeneratedValue Long id;
        @StartNode A startNode;
        @EndNode D endNode;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public A getStartNode() {
            return startNode;
        }

        public void setStartNode(A startNode) {
            this.startNode = startNode;
        }

        public D getEndNode() {
            return endNode;
        }

        public void setEndNode(D endNode) {
            this.endNode = endNode;
        }
    }

    @NodeEntity("DD")
    public static class D {
        @Id String id;

        @Relationship(type = "HAS_D", direction = Relationship.INCOMING)
        public A a;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public A getA() {
            return a;
        }

        public void setA(A a) {
            this.a = a;
        }
    }

}
