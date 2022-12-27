/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
package org.neo4j.ogm.drivers.bolt.driver;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.neo4j.ogm.config.ClasspathConfigurationSource;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class BoltDriverServiceTest extends TestContainersTestBase {

    @Test
    void loadBoltDriver() {
        String uri = new Configuration.Builder(new ClasspathConfigurationSource("ogm.properties")).build().getURI();
        Configuration driverConfiguration = new Configuration.Builder().uri(uri).build();
        SessionFactory sf = new SessionFactory(driverConfiguration, "org.neo4j.ogm.domain.social.User");
        Driver driver = sf.unwrap(Driver.class);
        assertThat(driver).isNotNull();
        sf.close();
    }

    @Test
    void databaseShouldBeConfigurable() {

        assumeTrue(isVersionOrGreater("4.0.0"), "This test requires a 4.0 database");

        Configuration driverConfiguration = getBaseConfigurationBuilder().database("system").build();
        SessionFactory sf = new SessionFactory(driverConfiguration, "org.neo4j.ogm.domain.social.User");
        assumeTrue(sf.unwrap(Driver.class) instanceof BoltDriver, "This test requires the BoltDriver");

        // This is a command only valid in system db
        Result result = sf.openSession().query("SHOW DATABASES;", Collections.emptyMap());
        assertThat(result.iterator().hasNext());
        sf.close();
    }
}
