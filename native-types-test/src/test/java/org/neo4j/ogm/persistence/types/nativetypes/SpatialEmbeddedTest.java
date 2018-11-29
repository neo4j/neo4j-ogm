/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */
package org.neo4j.ogm.persistence.types.nativetypes;

import java.io.File;
import java.nio.file.Paths;

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
