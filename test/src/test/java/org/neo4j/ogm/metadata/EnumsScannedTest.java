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

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.food.entities.scanned.Pizza;
import org.neo4j.ogm.domain.food.entities.scanned.Risk;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * Tests behaviour of enums that have been scanned when creating a SessionFactory
 *
 * @author Mihai Raulea
 * @author Luanne Misquitta
 * @see issue #145
 */
public class EnumsScannedTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(baseConfiguration.build(),"org.neo4j.ogm.domain.food.entities.scanned");
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();
    }

    @After
    public void tearDown() {
        session.purgeDatabase();
    }

    @Test
    public void shouldHandleEnumWithNoConverterOrPropertyAnnotation() {
        Pizza pizza = new Pizza();
        pizza.strokeRisk = Risk.LOW;
        session.save(pizza);

        session.clear();

        pizza = session.load(Pizza.class, pizza.id);
        assertNotNull(pizza);
        assertEquals(Risk.LOW, pizza.strokeRisk);
        assertNull(pizza.cancerRisk);
        assertNull(pizza.diabetesRisk);
        assertNull(pizza.hypertensionRisk);
    }

    @Test
    public void shouldHandleEnumWithConverterButNoPropertyAnnotation() {
        Pizza pizza = new Pizza();
        pizza.diabetesRisk = Risk.HIGH;
        session.save(pizza);

        session.clear();

        pizza = session.load(Pizza.class, pizza.id);
        assertNotNull(pizza);
        assertEquals(Risk.HIGH, pizza.diabetesRisk);
        assertNull(pizza.cancerRisk);
        assertNull(pizza.strokeRisk);
        assertNull(pizza.hypertensionRisk);
    }

    @Test
    public void shouldHandleEnumWithPropertyAnnotationButNoConverter() {
        Pizza pizza = new Pizza();
        pizza.cancerRisk = Risk.LOW;
        session.save(pizza);

        session.clear();

        pizza = session.load(Pizza.class, pizza.id);
        assertNotNull(pizza);
        assertEquals(Risk.LOW, pizza.cancerRisk);
        assertNull(pizza.diabetesRisk);
        assertNull(pizza.strokeRisk);
        assertNull(pizza.hypertensionRisk);
    }

    @Test
    public void shouldHandleEnumWithConverterAndPropertyAnnotation() {
        Pizza pizza = new Pizza();
        pizza.hypertensionRisk = Risk.HIGH;
        session.save(pizza);

        session.clear();

        pizza = session.load(Pizza.class, pizza.id);
        assertNotNull(pizza);
        assertEquals(Risk.HIGH, pizza.hypertensionRisk);
        assertNull(pizza.diabetesRisk);
        assertNull(pizza.strokeRisk);
        assertNull(pizza.cancerRisk);
    }
}
