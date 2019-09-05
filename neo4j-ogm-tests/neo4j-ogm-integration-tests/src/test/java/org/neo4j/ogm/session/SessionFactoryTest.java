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
package org.neo4j.ogm.session;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assumptions.*;

import org.assertj.core.api.Assumptions;
import org.junit.Test;
import org.neo4j.driver.Driver;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.domain.bike.Bike;
import org.neo4j.ogm.domain.blog.Author;
import org.neo4j.ogm.domain.pizza.Pizza;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Michael J. Simons
 */
public class SessionFactoryTest extends MultiDriverTestClass {

    @Test
    public void shouldMergeBasePackages() {

        Configuration configuration = getBaseConfiguration()
            .withBasePackages(Bike.class.getPackage().getName())
            .build();

        SessionFactory sessionFactory = new SessionFactory(configuration, Author.class.getPackage().getName());
        assertThat(sessionFactory.metaData().classInfo(Bike.class)).isNotNull();
        assertThat(sessionFactory.metaData().classInfo(Author.class)).isNotNull();
        assertThat(sessionFactory.metaData().classInfo(Pizza.class)).isNull();
    }


    @Test
    public void shouldUnwrapBoltDriver() {

        assumeThat(driver).isInstanceOf(BoltDriver.class);

        SessionFactory sessionFactory = new SessionFactory(driver, Bike.class.getPackage().getName());

        // Neo4j-OGM Driver
        assertThat(sessionFactory.unwrap(BoltDriver.class))
            .isInstanceOf(BoltDriver.class);
        // Native driver
        assertThat(sessionFactory.unwrap(Driver.class))
            .isInstanceOf(Driver.class);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sessionFactory.unwrap(EmbeddedDriver.class));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sessionFactory.unwrap(GraphDatabaseService.class));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sessionFactory.unwrap(HttpDriver.class));
    }

    @Test
    public void shouldUnwrapEmbeddedDriver() {

        assumeThat(driver).isInstanceOf(EmbeddedDriver.class);

        SessionFactory sessionFactory = new SessionFactory(driver, Bike.class.getPackage().getName());

        // Neo4j-OGM Driver
        assertThat(sessionFactory.unwrap(EmbeddedDriver.class))
            .isInstanceOf(EmbeddedDriver.class);
        // Underlying embedded instance
        assertThat(sessionFactory.unwrap(GraphDatabaseService.class))
            .isInstanceOf(GraphDatabaseService.class);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sessionFactory.unwrap(BoltDriver.class));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sessionFactory.unwrap(Driver.class));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sessionFactory.unwrap(HttpDriver.class));
    }

    @Test
    public void shouldUnwrapHttpDriver() {

        assumeThat(driver).isInstanceOf(HttpDriver.class);

        SessionFactory sessionFactory = new SessionFactory(driver, Bike.class.getPackage().getName());

        // Neo4j-OGM Driver
        assertThat(sessionFactory.unwrap(HttpDriver.class))
            .isInstanceOf(HttpDriver.class);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sessionFactory.unwrap(BoltDriver.class));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sessionFactory.unwrap(Driver.class));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sessionFactory.unwrap(EmbeddedDriver.class));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sessionFactory.unwrap(GraphDatabaseService.class));
    }
}
