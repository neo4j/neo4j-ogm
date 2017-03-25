/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.drivers;

import org.junit.Test;
import org.neo4j.ogm.config.ClasspathConfigurationSource;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.DriverManager;
import org.neo4j.ogm.exception.ConnectionException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

/**
 * @author Luanne Misquitta
 * @see Issue 133
 */
public class DriverExceptionTest {

    // TODO: All drivers should consistently throw the same exception on configuration or connection failure.
    @Test(expected = Exception.class)
    public void shouldThrowExceptionWhenHttpDriverCannotConnect() {
        Configuration configuration = new Configuration.Builder(new ClasspathConfigurationSource("ogm-http-invalid.properties")).build();
        SessionFactory sessionFactory = new SessionFactory(configuration, "org.neo4j.ogm.domain.social");
        Session session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    // TODO: All drivers should consistently throw the same exception on configuration or connection failure.
    @Test(expected = Exception.class)
    public void shouldThrowExceptionWhenEmbeddedDriverCannotConnect() {
        Configuration configuration = new Configuration.Builder(new ClasspathConfigurationSource("ogm-embedded-invalid.properties")).build();
        SessionFactory sessionFactory = new SessionFactory(configuration,"org.neo4j.ogm.domain.social");
        Session session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    // TODO: All drivers should consistently throw the same exception on configuration or connection failure.
    @Test(expected = Exception.class)
    public void shouldThrowExceptionWhenBoltDriverCannotConnect() {
        Configuration configuration = new Configuration.Builder(new ClasspathConfigurationSource("ogm-bolt-invalid.properties")).build();
        SessionFactory sessionFactory = new SessionFactory(configuration, "org.neo4j.ogm.domain.social");
        Session session = sessionFactory.openSession();
        session.purgeDatabase();
    }
}
