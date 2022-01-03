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
package org.neo4j.ogm.metadata;

import static org.assertj.core.api.Assertions.*;

import java.math.RoundingMode;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.food.entities.notScanned.Pizza;
import org.neo4j.ogm.domain.food.entities.scanned.Risk;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * Tests behaviour of enums that have not been scanned when creating a SessionFactory
 *
 * @author Mihai Raulea
 * @author Luanne Misquitta
 * @see issue #145
 */
public class EnumsNotScannedTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.food.entities.notScanned");
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();
    }

    @After
    public void cleanup() {
        session.purgeDatabase();
    }

    @Test
    public void shouldHandleEnumWithNoConverterOrPropertyAnnotation() {
        Pizza pizza = new Pizza();
        pizza.strokeRisk = Risk.LOW;
        session.save(pizza);

        session.clear();

        pizza = session.load(Pizza.class, pizza.id);
        assertThat(pizza).isNotNull();
        assertThat(pizza.strokeRisk).isEqualTo(Risk.LOW);
        assertThat(pizza.cancerRisk).isNull();
        assertThat(pizza.diabetesRisk).isNull();
        assertThat(pizza.hypertensionRisk).isNull();
    }

    @Test
    public void shouldHandleEnumWithConverterButNoPropertyAnnotation() {
        Pizza pizza = new Pizza();
        pizza.diabetesRisk = Risk.HIGH;
        session.save(pizza);

        session.clear();

        pizza = session.load(Pizza.class, pizza.id);
        assertThat(pizza).isNotNull();
        assertThat(pizza.diabetesRisk).isEqualTo(Risk.HIGH);
        assertThat(pizza.cancerRisk).isNull();
        assertThat(pizza.strokeRisk).isNull();
        assertThat(pizza.hypertensionRisk).isNull();
    }

    @Test
    public void shouldHandleEnumWithPropertyAnnotationButNoConverter() {
        Pizza pizza = new Pizza();
        pizza.cancerRisk = Risk.LOW;
        session.save(pizza);

        session.clear();

        pizza = session.load(Pizza.class, pizza.id);
        assertThat(pizza).isNotNull();
        assertThat(pizza.cancerRisk).isEqualTo(Risk.LOW);
        assertThat(pizza.diabetesRisk).isNull();
        assertThat(pizza.strokeRisk).isNull();
        assertThat(pizza.hypertensionRisk).isNull();
    }

    @Test
    public void shouldHandleEnumWithConverterAndPropertyAnnotation() {
        Pizza pizza = new Pizza();
        pizza.hypertensionRisk = Risk.HIGH;
        session.save(pizza);

        session.clear();

        pizza = session.load(Pizza.class, pizza.id);
        assertThat(pizza).isNotNull();
        assertThat(pizza.hypertensionRisk).isEqualTo(Risk.HIGH);
        assertThat(pizza.diabetesRisk).isNull();
        assertThat(pizza.strokeRisk).isNull();
        assertThat(pizza.cancerRisk).isNull();
    }

    /**
     * @see DATAGRAPH-659
     */
    @Test
    public void shouldHandleJavaEnums() {
        Pizza pizza = new Pizza();
        pizza.roundingMode = RoundingMode.CEILING;
        session.save(pizza);

        session.clear();
        pizza = session.load(Pizza.class, pizza.id);
        assertThat(pizza).isNotNull();
        assertThat(pizza.roundingMode).isEqualTo(RoundingMode.CEILING);
    }
}
