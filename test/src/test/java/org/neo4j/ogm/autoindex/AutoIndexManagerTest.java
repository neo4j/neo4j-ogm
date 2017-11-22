package org.neo4j.ogm.autoindex;

import static com.google.common.collect.Lists.*;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Make sure this tests works across all drivers supporting Neo4j 3.x and above.
 *
 * @author Mark Angrish
 * @author Eric Spiegelberg
 */
@Ignore
public class AutoIndexManagerTest extends MultiDriverTestClass {

    private static final Logger logger = LoggerFactory.getLogger(AutoIndexManagerTest.class);

    private GraphDatabaseService service;

    private MetaData metaData = new MetaData("org.neo4j.ogm.domain.forum");

    private static final String LOGIN_NICKNAME_INDEX = "INDEX ON :`Login`(`nickName`)";
    private static final String LOGIN_USERNAME_CONSTRAINT = "CONSTRAINT ON ( `login`:`Login` ) ASSERT `login`.`userName` IS UNIQUE";
    private static final String TAG_TAG_CONSTRAINT = "CONSTRAINT ON ( `t-a-g`:`T-A-G` ) ASSERT `t-a-g`.`short-description` IS UNIQUE";
    private static final String TAG_DESC_INDEX = "INDEX ON :`t-a-g`(`short-description`)";

    @Before
    public void setUp() throws Exception {
        service = getGraphDatabaseService();
    }

    @After
    public void tearDown() throws Exception {
        executeDrop(LOGIN_NICKNAME_INDEX, LOGIN_USERNAME_CONSTRAINT, TAG_TAG_CONSTRAINT, TAG_DESC_INDEX);
    }

    @Test
    public void testIndexesAreSuccessfullyAsserted() {

        createAllConstraintsAndIndexes();

        Configuration configuration = getBaseConfiguration().autoIndex("assert").build();
        AutoIndexManager indexManager = new AutoIndexManager(metaData, driver, configuration);

        assertThat(indexManager.getIndexes()).hasSize(2);
        indexManager.build();

        dropAllConstraintAndIndexes();
    }

    @Test
    public void indexIsCreatedForUpdate() throws Exception {
        Configuration configuration = getBaseConfiguration().autoIndex("update").build();
        AutoIndexManager indexManager = new AutoIndexManager(metaData, driver, configuration);
        indexManager.build();

        assertIndexExists("Login", "nickName");
    }

    @Test
    public void name() throws Exception {

    }

    private void assertIndexExists(String label, String property) {

        executeForIndexes(Label.label(label), (indexes) -> {
            assertThat(indexes)
                .extracting((IndexDefinition index) -> index.getPropertyKeys())
                .contains(singletonList(property));

        });
        try (Transaction tx = service.beginTx()) {

            boolean exists = false;
            Iterable<IndexDefinition> indexes = service.schema().getIndexes(Label.label(label));
            for (IndexDefinition index : indexes) {
                for (String key : index.getPropertyKeys()) {
                    if (key.equals(property)) {
                        exists = true;
                    }
                }
            }

            if (!exists) {
                fail("Could not find index on label " + label + " property " + property);
            }

            tx.success();
        }
    }

    private void executeForIndexes(Label label, Consumer<List<IndexDefinition>> consumer) {
        try (Transaction tx = service.beginTx()) {
            Iterable<IndexDefinition> indexes = service.schema().getIndexes(label);
            consumer.accept(newArrayList(indexes));
            tx.success();
        }
    }

    private void createAllConstraintsAndIndexes() {
        executeCreate(LOGIN_NICKNAME_INDEX,
            LOGIN_USERNAME_CONSTRAINT,
            TAG_TAG_CONSTRAINT,
            TAG_DESC_INDEX);
    }

    private void dropAllConstraintAndIndexes() {
        executeDrop(LOGIN_NICKNAME_INDEX,
            LOGIN_USERNAME_CONSTRAINT,
            TAG_TAG_CONSTRAINT,
            TAG_DESC_INDEX);
    }

    private void executeCreate(String... statements) {
        for (String statement : statements) {
            service.execute("CREATE " + statement);
        }
    }

    private void executeDrop(String... statements) {
        for (String statement : statements) {
            try {
                service.execute("DROP " + statement);
            } catch (Exception e) {
                logger.debug("Could not execute drop for statement (this might be expected) {}", statement, e);
            }
        }
    }
}
