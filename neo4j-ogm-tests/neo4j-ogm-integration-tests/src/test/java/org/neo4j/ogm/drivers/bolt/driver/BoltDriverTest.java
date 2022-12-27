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

import java.lang.reflect.Field;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.exceptions.ServiceUnavailableException;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.SessionFactory;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class BoltDriverTest {

    @Test
    void shouldDetectSimpleBoltSchemes() {

        for (String aSimpleScheme : Arrays.asList("bolt", "Bolt", "neo4j", "Neo4J")) {
            assertThat(BoltDriver.isSimpleScheme(aSimpleScheme)).isTrue();
        }
    }

    @Test
    void shouldDetectAdvancedBoltSchemes() {

        for (String anAdvancedScheme : Arrays.asList("bolt+s", "Bolt+ssc", "neo4j+s", "Neo4J+ssc")) {
            assertThat(BoltDriver.isSimpleScheme(anAdvancedScheme)).isFalse();
        }
    }

    @Test
    void shouldFailEarlyOnInvalidSchemes() {

        // Here `bolt+routing`  is invalid, as the Configuration respectively Drivers translates it into neo4j.
        for (String invalidScheme : Arrays.asList("bolt+routing", "bolt+x", "neo4j+wth")) {

            assertThatIllegalArgumentException()
                .isThrownBy(() -> BoltDriver.isSimpleScheme(invalidScheme))
                .withMessage("'%s' is not a supported scheme.", invalidScheme);
        }
    }

    @Test
    void schemesShouldBeApplied() throws NoSuchFieldException, IllegalAccessException {

        Field configField = BoltDriver.class.getDeclaredField("driverConfig");
        configField.setAccessible(true);

        for (String scheme : Arrays.asList("bolt+s", "bolt+ssc", "neo4j+s", "neo4j+ssc")) {

            String uri = String.format("%s://localhost:7687", scheme);
            Configuration configuration = new Configuration.Builder()
                .uri(uri)
                .verifyConnection(false)
                .build();

            assertThat(configuration.getURI()).isEqualTo(uri);

            BoltDriver boltDriver = new BoltDriver();
            boltDriver.configure(configuration);

            // Cannot get a Driver from the BoltTransport without opening up a connection
            Config config = (Config) configField.get(boltDriver);

            // This would already fail if both the scheme and a programmatic approach would have been applied
            Driver driver = GraphDatabase.driver(uri, AuthTokens.none(), config);
            // Be on par with the neo4j-java-driver-spring-boot-starter
            assertThat(driver.isEncrypted()).isTrue();
        }
    }
}
