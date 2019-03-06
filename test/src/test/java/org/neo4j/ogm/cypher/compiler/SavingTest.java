package org.neo4j.ogm.cypher.compiler;

import static org.assertj.core.api.Assertions.*;

import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.domain.gh613.Label;
import org.neo4j.ogm.domain.gh613.Node;
import org.neo4j.ogm.domain.gh613.NodeType;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Andreas Berger
 */
public class SavingTest extends MultiDriverTestClass {

    private Session session;

    @BeforeClass
    public static void initSesssionFactory() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.gh613");
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();

        NodeType nodeType = new NodeType("nt");
        Node l1 = new Node("l1").setNodeType(nodeType);
        Node l2 = new Node("l2")
            .setNodeType(nodeType)
            .setLabels(Collections.singleton(new Label().setKey("label1")))
            .setChildOf(l1);

        l1.setChildNodes(Collections.singleton(l2));

        session.save(l1);
        session.save(l2);
        session.clear();
    }

    /**
     * GH-613
     */
    @Test
    public void testSaveParentAfterChild() {

        Node l2 = queryNode("l2");
        assertThat(l2.getNodeType()).isNotNull();
        l2.setLabels(null);
        session.save(l2);

        Node l1 = queryNode("l1");
        assertThat(l1.getChildNodes()).hasSize(1);
        assertThat(l1.getNodeType()).isNotNull();
        session.save(l1);

        l2 = queryNode("l2");
        assertThat(l2.getNodeType()).isNotNull();
    }

    private Node queryNode(String nodeId) {
        Collection<Node> nodeTypes = session
            .loadAll(Node.class, new Filters(new Filter("nodeId", ComparisonOperator.EQUALS, nodeId)));
        assertThat(nodeTypes).hasSize(1);
        return nodeTypes.iterator().next();
    }
}
