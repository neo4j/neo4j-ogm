package org.neo4j.ogm.typeconversion;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;

/**
 * Created by Mihai Raulea on 4/24/2017.
 */
@Ignore
public class AbstractClassTest {

    @NodeEntity(label = "BaseType")
    public abstract class BaseEntity {

        @GraphId
        private Long graphId;
        @Relationship(type = "RELATED_TO", direction = "OUTGOING")
        private List<BaseEntity> outgoing = new ArrayList<>();
        @Relationship(type = "RELATED_TO", direction = "INCOMING")
        private List<BaseEntity> incoming = new ArrayList<>();

        public Long getGraphId() {
            return graphId;
        }

        public List<BaseEntity> getOutgoing() {
            return outgoing;
        }

        public void setOutgoing(List<BaseEntity> outgoing) {
            this.outgoing = outgoing;
        }

        public List<BaseEntity> getIncoming() {
            return incoming;
        }

        public void setIncoming(List<BaseEntity> incoming) {
            this.incoming = incoming;
        }

        public void addIncoming(BaseEntity related) {
            incoming.add(related);
            related.getOutgoing().add(this);
        }
    }

    @NodeEntity(label = "Type1")
    public class Type1 extends BaseEntity {

    }

    @NodeEntity(label = "Type2")
    public class Type2 extends BaseEntity {

    }

    private SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.typeconversion");

    @Test
    public void saveMultipleRelationships() throws Exception {
        Type1 node1 = new Type1();
        Type2 node2 = new Type2();
        node1.addIncoming(node2);
        node2.addIncoming(node1);

        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.save(node1);
        transaction.commit();
        transaction.close();

        session = sessionFactory.openSession();
        transaction = session.beginTransaction();
        BaseEntity entity = session.load(BaseEntity.class, node1.getGraphId());
        transaction.close();
        Assert.assertFalse(entity.getIncoming().isEmpty());
        Assert.assertFalse(entity.getOutgoing().isEmpty());
    }
}

