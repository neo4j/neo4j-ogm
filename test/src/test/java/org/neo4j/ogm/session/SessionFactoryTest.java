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
