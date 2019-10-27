package org.neo4j.ogm.session;

import static org.assertj.core.api.Assertions.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.domain.lazyloading.BaseNodeEntity;
import org.neo4j.ogm.domain.lazyloading.FieldDefinition;
import org.neo4j.ogm.domain.lazyloading.Label;
import org.neo4j.ogm.domain.lazyloading.LabelGroup;
import org.neo4j.ogm.domain.lazyloading.Node;
import org.neo4j.ogm.domain.lazyloading.NodeData;
import org.neo4j.ogm.domain.lazyloading.NodeType;
import org.neo4j.ogm.lazyloading.LazyInitializationException;
import org.neo4j.ogm.session.event.EventListenerAdapter;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.testutil.TestUtils;

/**
 * @author Andreas Berger
 */
public class LazyLoadingTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void createSessionFactory() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.lazyloading");
        sessionFactory.setLoadStrategy(LoadStrategy.LAZY_LOAD_STRATEGY);
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();
        session.purgeDatabase();
        session.query(TestUtils.readCQLFile("org/neo4j/ogm/cql/lazy_nodes.cql").toString(), Utils.map());
    }

    @Test
    public void testTraverse() {
        Node loc1_1_1_1 = queryNode("loc1_1_1_1");
        Node loc1 = loc1_1_1_1.getParentNode().getParentNode().getParentNode();
        assertThat(loc1.getName()).isNull();
        loc1.setName("location 1");
        session.save(loc1_1_1_1);
        session.clear();

        loc1 = queryNode("loc1");
        assertThat(loc1.getName()).isEqualTo("location 1");
    }

    @Test
    public void testReloadEntityWithoutSaving() {
        Node loc1 = queryNode("loc1");

        Node newParent = new Node();
        loc1.setParentNode(newParent);
        assertThat(loc1.getParentNode()).isSameAs(newParent);

        loc1 = queryNode("loc1");
        assertThat(loc1.getParentNode()).isNotNull();
        assertThat(loc1.getParentNode()).isNotSameAs(newParent);
    }

    @Test
    public void testSave() {
        Node loc1 = queryNode("loc1");
        loc1.setName("location 1");
        session.save(loc1);

        session.clear();

        loc1 = queryNode("loc1");
        assertThat(loc1.getParentNode()).isNotNull();
        assertThat(loc1.getChildNodes()).hasSize(3);

        Node loc1_1 = queryNode("loc1_1");
        assertThat(loc1.getChildNodes()).contains(loc1_1);
        assertThat(loc1_1.getParentNode()).isSameAs(loc1);
        assertThat(loc1_1.getParentNode().getName()).isEqualTo("location 1");
    }

    @Test
    public void testResetParent() {
        Node loc1 = queryNode("loc1");
        loc1.setParentNode(null);
        session.save(loc1);

        session.clear();

        loc1 = queryNode("loc1");
        assertThat(loc1.getParentNode()).isNull();

        Node comp1 = queryNode("company1");
        assertThat(comp1.getChildNodes()).hasSize(1);
    }

    @Test
    public void testRelationship() {
        Node loc1 = queryNode("loc1");
        loc1.setNodeData(null);
        session.save(loc1);
        session.clear();

        loc1 = queryNode("loc1");
        assertThat(loc1.getNodeData()).isNullOrEmpty();
    }

    @Test
    public void testRelationShip2() {
        Collection<NodeData> nodes = session.loadAll(NodeData.class);
        assertThat(nodes).hasSize(2);
        NodeData data = nodes.iterator().next();
        assertThat(data.getBaseNode().getParentNode().getParentNode().getNodeId()).isEqualTo("root");
    }

    @Test
    public void testMultipleSaves() {
        Node root = queryNode("root");

        NodeType nodeTypeCompany = queryNodeType("company");

        Node comp2 = (Node) new Node()
            .setNodeId("comp2")
            .setNodeType(nodeTypeCompany)
            .setParentNode(root);
        saveAndCheck(comp2);

        NodeType nodeTypeLocation = queryNodeType("location");
        Node loc2_1 = (Node) new Node()
            .setNodeId("loc2_1")
            .setParentNode(comp2)
            .setNodeType(nodeTypeLocation);
        saveAndCheck(loc2_1);

        Node loc2_2 = (Node) new Node()
            .setNodeId("loc2_2")
            .setParentNode(comp2)
            .setNodeType(nodeTypeLocation);
        saveAndCheck(loc2_2);
    }

    @Test(expected = LazyInitializationException.class)
    public void testMultipleSessions() {
        Node root = queryNode("root");

        NodeType nodeTypeCompany = queryNodeType("company");

        Node comp2 = (Node) new Node()
            .setNodeId("comp2")
            .setNodeType(nodeTypeCompany)
            .setParentNode(root);
        session = sessionFactory.openSession();
        saveAndCheck(comp2);
    }

    @Test
    public void testSaveDelegate() {
        sessionFactory.register(new EventListenerAdapter());
        session = sessionFactory.openSession();

        Node node = new Node();
        node.setNodeId("root2");
        node.setNodeType(queryNodeType("root"));

        session.save(node);

        Collection<LabelGroup> labelGroups = session
            .loadAll(LabelGroup.class, new Filters(new Filter("key", ComparisonOperator.EQUALS, "lg1")));
        assertThat(labelGroups).hasSize(1);
        LabelGroup labelGroup = labelGroups.iterator().next();
        assertThat(labelGroup.getLabels()).hasSize(1);

    }

    @Test
    public void testSaveLoadSave() {
        Node comp2 = new Node();
        comp2.setNodeId("comp2");
        comp2.setNodeType(queryNodeType("company"));
        comp2.setLabels(Collections.singleton(getLabel("l1")));

        session.save(comp2);

        comp2 = queryNode("comp2");
        session.save(comp2);

        comp2 = queryNode("comp2");
        assertThat(comp2.getLabels()).hasSize(1);
    }

    @Test
    public void testLoadByQuery() {

        Node unit = new Node();
        unit.setNodeId("unit");

        FieldDefinition fd2 = StreamSupport.stream(queryFieldsWithGroup("unit")
            .spliterator(), false)
            .filter(fieldDefinition -> fieldDefinition.getFieldKey().equals("fd2"))
            .findFirst()
            .orElse(null);

        NodeData nodeData = new NodeData()
            .setBaseNode(unit)
            .setFieldDefinition(fd2)
            .setValue("foo");
        session.save(nodeData);

        assertThat(queryFieldDefinition("fd3").getFieldGroup()).isNotNull();
        assertThat(queryFieldDefinition("fd2").getFieldGroup()).isNotNull();
    }

    @Test
    public void testChangeParent() {
        Node loc1_1_2 = queryNode("loc1_1_2");
        Node loc1 = queryNode("loc1");
        loc1_1_2.setParentNodeBidirectional(loc1);
        session.save(loc1_1_2);

        session.clear();
        assertThat(queryNode("loc1").getChildNodes()).hasSize(4);
        assertThat(queryNode("loc1_1").getChildNodes()).hasSize(1);
    }

    private Iterable<FieldDefinition> queryFieldsWithGroup(String nodeTypeId) {
        return session.query(FieldDefinition.class,
            "MATCH (f:FieldDefinition)<-[:HAS_FIELD]-(t:NodeType) "
                + "WHERE t.nodeTypeId = {nodeTypeId} "
                + "OPTIONAL MATCH (g:FieldGroup)-[:GROUPED_BY]->(f) "
                + "WITH f, g "
                + "RETURN f, [ "
                + "[(f)<-[r_g1:GROUPED_BY]-(g:FieldGroup) | [r_g1, g]],"
                + "[(f)-[r:HAS_OPTION]->(o:FieldOption) | [r, o]]"
                + "]"
                + "ORDER BY g.sort, f.sort",
            Collections.singletonMap("nodeTypeId", nodeTypeId));
    }

    private Label getLabel(String key) {
        Collection<Label> labels = session
            .loadAll(Label.class, new Filters(new Filter("key", ComparisonOperator.EQUALS, key)));
        assertThat(labels).hasSize(1);
        return labels.iterator().next();
    }

    private void saveAndCheck(BaseNodeEntity node) {
        Node parentNode = node.getParentNode();
        NodeType parentNodeType = queryNodeType(parentNode.getNodeType().getNodeTypeId());
        Set<NodeType> subs = parentNodeType.getSubordinateNodeTypes();
        if (subs.isEmpty() || !subs.contains(node.getNodeType())) {
            Assert.fail("node-type not allowed");
        }
        session.save(node);
    }

    private Node queryNode(String nodeId) {
        Collection<Node> nodeTypes = session
            .loadAll(Node.class, new Filters(new Filter("nodeId", ComparisonOperator.EQUALS, nodeId)));
        assertThat(nodeTypes).hasSize(1);
        return nodeTypes.iterator().next();
    }

    private NodeType queryNodeType(String nodeTypeId) {
        Collection<NodeType> nodes = session
            .loadAll(NodeType.class, new Filters(new Filter("nodeTypeId", ComparisonOperator.EQUALS, nodeTypeId)));
        assertThat(nodes).hasSize(1);
        return nodes.iterator().next();
    }

    private FieldDefinition queryFieldDefinition(String fieldKey) {
        Collection<FieldDefinition> fieldDefinitions = session
            .loadAll(FieldDefinition.class, new Filters(new Filter("fieldKey", ComparisonOperator.EQUALS, fieldKey)));
        assertThat(fieldDefinitions).hasSize(1);
        return fieldDefinitions.iterator().next();
    }
}
