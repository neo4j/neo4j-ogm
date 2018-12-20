package org.neo4j.ogm.persistence.examples.nodes;

import static org.assertj.core.api.Assertions.*;
import static org.slf4j.LoggerFactory.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.domain.nodes.DataItem;
import org.neo4j.ogm.domain.nodes.FormulaItem;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.testutil.TestUtils;
import org.slf4j.Logger;

/**
 * Test class reproducing issue #576
 *
 * @author Andreas Berger
 */
@RunWith(Parameterized.class)
public class NodeTest extends MultiDriverTestClass {
    private static final Logger LOGGER = getLogger(NodeTest.class);

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.nodes");
    }

    @Parameterized.Parameters
    public static List<Integer> data() {
        return IntStream
            .range(0, 10) // change the number of test additionallyData here
            .boxed()
            .collect(Collectors.toList());
    }

    public NodeTest(@SuppressWarnings("unused") Integer iterations) {
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
        session.clear();
        String testData = TestUtils.readCQLFile("org/neo4j/ogm/cql/nodes.cql").toString();
        session.query(testData , Collections.emptyMap());
    }

    @After
    public void tearDown() {
        session.purgeDatabase();
    }

    @Test
    public void test() {
        Collection<DataItem> dataItems;
        FormulaItem formulaItem;

        Filter filter = new Filter("nodeId", ComparisonOperator.EQUALS, "m1");

        LOGGER.info("load data");
        dataItems = session.loadAll(DataItem.class, filter);
        assertThat(dataItems.size()).isEqualTo(1);
        formulaItem = (FormulaItem) dataItems.iterator().next();
        assertThat(formulaItem.getVariables().size()).isEqualTo(3);

        formulaItem.getVariables()
            .removeIf(
                variable -> variable.getVariable().equals("A") && variable.getDataItem().getNodeId().equals("m2"));
        LOGGER.info("change data");
        session.save(formulaItem);

        LOGGER.info("load data again");
        dataItems = session.loadAll(DataItem.class, filter);
        assertThat(dataItems.size()).isEqualTo(1);
        formulaItem = (FormulaItem) dataItems.iterator().next();
        assertThat(formulaItem.getVariables().size()).isEqualTo(2);
    }

}
