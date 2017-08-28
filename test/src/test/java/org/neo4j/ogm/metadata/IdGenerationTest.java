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

package org.neo4j.ogm.metadata;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.annotations.ids.ValidAnnotations;
import org.neo4j.ogm.exception.MappingException;
import org.neo4j.ogm.id.IdStrategy;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Frantisek Hartman
 */
public class IdGenerationTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.annotations.ids");
    }

    @Before
    public void setUp() {
        session = sessionFactory.openSession();
    }

    @Test
    public void saveWithoutIdAnnotation() {

        ValidAnnotations.InternalId entity = new ValidAnnotations.InternalId();
        session.save(entity);

        assertThat(entity.id).isNotNull();

        final Session session2 = sessionFactory.openSession();

        final ValidAnnotations.InternalId loaded = session2.load(ValidAnnotations.InternalId.class, entity.id);
        assertThat(loaded).isNotNull();
        assertThat(loaded.id).isNotNull().isEqualTo(entity.id);
    }

    @Test
    public void saveInternalIdWithAnnotation() throws Exception {
        ValidAnnotations.InternalIdWithAnnotation entity = new ValidAnnotations.InternalIdWithAnnotation();
        session.save(entity);

        assertThat(entity.identifier).isNotNull();

        session.clear();
        ValidAnnotations.InternalIdWithAnnotation loaded = session.load(ValidAnnotations.InternalIdWithAnnotation.class, entity.identifier);

        assertThat(loaded).isNotNull();
        assertThat(loaded.identifier).isEqualTo(entity.identifier);
    }

    @Test
    public void saveWithStringUuidGeneration() {

        ValidAnnotations.IdAndGenerationType entity = new ValidAnnotations.IdAndGenerationType();
        session.save(entity);

        assertThat(entity.identifier).isNotNull();

        final Session session2 = sessionFactory.openSession();

        final ValidAnnotations.IdAndGenerationType retrievedEntity = session2.load(ValidAnnotations.IdAndGenerationType.class, entity.identifier);
        assertThat(retrievedEntity).isNotNull();
        assertThat(retrievedEntity.identifier).isNotNull().isEqualTo(entity.identifier);
    }

    @Test
    public void saveWithUuidGeneration() {

        ValidAnnotations.UuidIdAndGenerationType entity = new ValidAnnotations.UuidIdAndGenerationType();
        session.save(entity);

        assertThat(entity.identifier).isNotNull();

        final Session session2 = sessionFactory.openSession();

        final ValidAnnotations.UuidIdAndGenerationType retrievedEntity = session2.load(ValidAnnotations.UuidIdAndGenerationType.class, entity.identifier);
        assertThat(retrievedEntity).isNotNull();
        assertThat(retrievedEntity.identifier).isNotNull().isEqualTo(entity.identifier);
    }

    @Test
    public void saveWithCustomStrategyGeneratesId() throws Exception {
        ValidAnnotations.WithCustomIdStrategy entity = new ValidAnnotations.WithCustomIdStrategy();
        session.save(entity);

        assertThat(entity.idetifier).isEqualTo("test-custom-id");

        session.clear();

        ValidAnnotations.WithCustomIdStrategy loaded = session.load(ValidAnnotations.WithCustomIdStrategy.class, entity.idetifier);
        assertThat(loaded).isNotNull();
        assertThat(loaded.idetifier).isEqualTo("test-custom-id");
    }

    @Test
    public void saveWithContextIdStrategy() throws Exception {
        CustomInstanceIdStrategy strategy = new CustomInstanceIdStrategy("test-custom-instance-id");
        sessionFactory.register(strategy);

        ValidAnnotations.WithCustomInstanceIdStrategy entity = new ValidAnnotations.WithCustomInstanceIdStrategy();
        session.save(entity);

        assertThat(entity.idetifier).isEqualTo("test-custom-instance-id");

        session.clear();
        ValidAnnotations.WithCustomInstanceIdStrategy loaded = session.load(ValidAnnotations.WithCustomInstanceIdStrategy.class, "test-custom-instance-id");
        assertThat(loaded).isNotNull();
        assertThat(loaded.idetifier).isEqualTo("test-custom-instance-id");
    }

    @Test(expected = MappingException.class)
    public void saveWithCustomInstaceIdStrategyWhenStrategyNotRegistered() throws Exception {
        // create new session factory without registered instance of the strategy
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.annotations.ids");
        session = sessionFactory.openSession();

        ValidAnnotations.WithCustomInstanceIdStrategy entity = new ValidAnnotations.WithCustomInstanceIdStrategy();
        session.save(entity);
    }

    public static class CustomIdStrategy implements IdStrategy {

        @Override
        public Object generateId(Object entity) {
            return "test-custom-id";
        }
    }

    public static class CustomInstanceIdStrategy implements IdStrategy {

        String value;

        public CustomInstanceIdStrategy(String value) {
            this.value = value;
        }

        @Override
        public Object generateId(Object entity) {
            return value;
        }
    }

}
