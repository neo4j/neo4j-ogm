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

import org.junit.Test;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.domain.bike.Bike;
import org.neo4j.ogm.domain.blog.Author;
import org.neo4j.ogm.domain.pizza.Pizza;

/**
 * @author Michael J. Simons
 */
public class SessionFactoryTest {

    @Test
    public void shouldMergeBasePackages() {

        Configuration configuration = new Configuration.Builder()
            .withBasePackages(Bike.class.getPackage().getName())
            .build();

        SessionFactory sessionFactory = new SessionFactory(configuration, Author.class.getPackage().getName());
        assertThat(sessionFactory.metaData().classInfo(Bike.class)).isNotNull();
        assertThat(sessionFactory.metaData().classInfo(Author.class)).isNotNull();
        assertThat(sessionFactory.metaData().classInfo(Pizza.class)).isNull();
    }
}
