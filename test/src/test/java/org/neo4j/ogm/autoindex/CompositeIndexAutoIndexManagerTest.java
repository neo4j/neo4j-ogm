package org.neo4j.ogm.autoindex;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assume.*;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.autoindex.CompositeIndexChild;
import org.neo4j.ogm.domain.autoindex.CompositeIndexEntity;
import org.neo4j.ogm.domain.autoindex.MultipleCompositeIndexEntity;
import org.neo4j.ogm.metadata.MetaData;

/**
 * @author Frantisek Hartman
 */
public class CompositeIndexAutoIndexManagerTest extends BaseAutoIndexManagerTest {

    private static final String INDEX = "INDEX ON :`Entity`(`name`,`age`)";
    private static final String CONSTRAINT = "CONSTRAINT ON (entity:Entity) ASSERT (entity.name, entity.age) IS NODE KEY";

    public CompositeIndexAutoIndexManagerTest() {
        super(INDEX, CompositeIndexEntity.class.getName(), CompositeIndexChild.class.getName());
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        assumeTrue("This test uses composite index and node key constraint and can only be run on enterprise edition",
            isEnterpriseEdition());

        assumeTrue("This tests uses composite index and can only be run on Neo4j 3.2.0 and later",
            isVersionOrGreater("3.2.0"));
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        executeDrop(CONSTRAINT);
    }

    @Test
    public void testAutoIndexManagerUpdateConstraintChangedToIndex() throws Exception {
        executeCreate(CONSTRAINT);

        runAutoIndex("update");

        executeForIndexes(indexes -> {
            assertThat(indexes).hasSize(1);
        });
        executeForConstraints(constraints -> {
            assertThat(constraints).isEmpty();
        });
    }

    @Test
    public void testMultipleCompositeIndexAnnotations() throws Exception {
        metaData = new MetaData(MultipleCompositeIndexEntity.class.getName());

        try {

            runAutoIndex("update");

            executeForIndexes(indexes -> {
                assertThat(indexes).hasSize(2);
            });
        } finally {
            executeDrop("INDEX ON :Entity(name, age)");
            executeDrop("INDEX ON :Entity(name, email)");
        }
    }
}
