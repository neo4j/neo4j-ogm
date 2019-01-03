/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.ogm.persistence.types.nativetypes;

import java.io.File;

import org.assertj.core.util.Files;
import org.junit.BeforeClass;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.session.SessionFactory;

/**
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
public class SpatialEmbeddedTest extends SpatialTestBase {

    @BeforeClass
    public static void init() {

        File temporaryFolder = Files.newTemporaryFolder();
        temporaryFolder.deleteOnExit();

        File databaseDirectory = Files.newFolder(temporaryFolder.getAbsolutePath() + "/database");

        Configuration ogmConfiguration = new Configuration.Builder()
            .uri("file://" + databaseDirectory.getAbsolutePath())
            .useNativeTypes()
            .build();

        EmbeddedDriver driver = new EmbeddedDriver();
        driver.configure(ogmConfiguration);
        sessionFactory = new SessionFactory(driver, SpatialEmbeddedTest.class.getPackage().getName());
    }

}
