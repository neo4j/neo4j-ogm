/*
 * Copyright (c) 2002-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.autoindex;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.neo4j.ogm.config.AutoIndexMode;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * Make sure this tests works across all drivers supporting Neo4j 3.x and above.
 *
 * @author Mark Angrish
 * @author Eric Spiegelberg
 */
public class AutoIndexManagerTest extends MultiDriverTestClass {

    private MetaData metaData = new MetaData("org.neo4j.ogm.domain.forum");
    private SessionFactory sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.forum");

    private static final String CREATE_LOGIN_CONSTRAINT_CYPHER = "CREATE CONSTRAINT ON ( `login`:`Login` ) ASSERT `login`.`userName` IS UNIQUE";
    private static final String CREATE_TAG_CONSTRAINT_CYPHER = "CREATE CONSTRAINT ON ( `t-a-g`:`T-A-G` ) ASSERT `t-a-g`.`short-description` IS UNIQUE";
    private static final String CREATE_TAG_INDEX_CYPHER = "CREATE INDEX ON :`t-a-g`(`short-description`)";

    private static final String DROP_LOGIN_CONSTRAINT_CYPHER = "DROP CONSTRAINT ON (`login`:`Login`) ASSERT `login`.`userName` IS UNIQUE";
    private static final String DROP_TAG_CONSTRAINT_CYPHER = "DROP CONSTRAINT ON (`t-a-g`:`T-A-G`) ASSERT `t-a-g`.`short-description` IS UNIQUE";

    @Test
    public void shouldPreserveNoneConfiguration() {
        Configuration configuration = getBaseConfiguration().autoIndex("none").build();
        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.NONE);
    }

    @Test
    public void shouldPreserveAssertConfiguration() {
        Configuration configuration = getBaseConfiguration().autoIndex("assert").build();
        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.ASSERT);
    }

    @Test
    public void shouldPreserveValidateConfiguration() {
        Configuration configuration = getBaseConfiguration().autoIndex("validate").build();
        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.VALIDATE);
    }

    @Test
    public void shouldPreserveDumpConfiguration() {
        Configuration configuration = getBaseConfiguration().autoIndex("dump").build();
        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.DUMP);
    }

    @Test
    public void testIndexesAreSuccessfullyValidated() {

        createConstraints();

        Configuration configuration = getBaseConfiguration().autoIndex("validate").build();
        Neo4jSession session = (Neo4jSession) sessionFactory.openSession();
        AutoIndexManager indexManager = new AutoIndexManager(metaData, configuration, session);
        assertThat(indexManager.getIndexes()).hasSize(2);
        indexManager.build();

        dropConstraints();
    }

    @Test(expected = MissingIndexException.class)
    public void testIndexesAreFailValidation() {
        Configuration configuration = getBaseConfiguration().autoIndex("validate").build();
        Neo4jSession session = (Neo4jSession) sessionFactory.openSession();
        AutoIndexManager indexManager = new AutoIndexManager(metaData, configuration, session);
        indexManager.build();
    }

    @Test
    public void testIndexDumpMatchesDatabaseIndexes() throws IOException {

        createConstraints();

        File file = new File("./test.cql");

        try {
            Configuration configuration = getBaseConfiguration()
                .autoIndex("dump")
                .generatedIndexesOutputDir(".")
                .generatedIndexesOutputFilename("test.cql")
                .build();

            Neo4jSession session = (Neo4jSession) sessionFactory.openSession();
            AutoIndexManager indexManager = new AutoIndexManager(metaData, configuration, session);
            assertThat(indexManager.getIndexes()).hasSize(2);
            indexManager.build();

            assertThat(file.exists()).isTrue();
            try (InputStream is = new FileInputStream("./test.cql")) {
                String actual = IOUtils.toString(is);
                String expected = CREATE_LOGIN_CONSTRAINT_CYPHER + " " + CREATE_TAG_CONSTRAINT_CYPHER;
                assertThat(actual).isEqualToIgnoringWhitespace(expected);
            }

        } finally {
            file.delete();
        }

        dropConstraints();
    }

    @Test
    public void testIndexesAreSuccessfullyAsserted() {

        createConstraints();

        Configuration configuration = getBaseConfiguration().autoIndex("assert").build();
        Neo4jSession session = (Neo4jSession) sessionFactory.openSession();
        AutoIndexManager indexManager = new AutoIndexManager(metaData, configuration, session);

        assertThat(indexManager.getIndexes()).hasSize(2);
        indexManager.build();

        dropConstraints();
    }

    private void createConstraints() {
        getGraphDatabaseService().execute(CREATE_LOGIN_CONSTRAINT_CYPHER);
        getGraphDatabaseService().execute(CREATE_TAG_CONSTRAINT_CYPHER);
        getGraphDatabaseService().execute(CREATE_TAG_INDEX_CYPHER);
    }

    private void dropConstraints() {
        getGraphDatabaseService().execute(DROP_LOGIN_CONSTRAINT_CYPHER);
        getGraphDatabaseService().execute(DROP_TAG_CONSTRAINT_CYPHER);
    }

}
