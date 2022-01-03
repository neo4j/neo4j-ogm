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
package org.neo4j.ogm.persistence.examples.pets;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.pets.Dog;
import org.neo4j.ogm.domain.pets.DomesticDog;
import org.neo4j.ogm.domain.pets.Kid;
import org.neo4j.ogm.domain.pets.Mammal;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Luanne Misquitta
 */

public class PetIntegrationTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.pets");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
    }

    @After
    public void teardown() {
        session.purgeDatabase();
    }

    /**
     * @see issue #40
     */
    @Test
    public void shouldResolveMetadataCorrectly() {
        MetaData metaData = new MetaData("org.neo4j.ogm.domain.pets");
        assertThat(metaData.resolve("Animal").name()).isEqualTo("org.neo4j.ogm.domain.pets.Animal");
        assertThat(metaData.resolve("Mammal", "Animal").name()).isEqualTo("org.neo4j.ogm.domain.pets.Mammal");
        assertThat(metaData.resolve("Mammal", "Animal", "Dog").name()).isEqualTo("org.neo4j.ogm.domain.pets.Dog");
        assertThat(metaData.resolve("Dog", "Mammal", "Animal").name()).isEqualTo("org.neo4j.ogm.domain.pets.Dog");
    }

    /**
     * @see issue #40
     */
    @Test
    public void shouldBeAbleToSaveAndLoadMammals() {
        Kid kid = new Kid("Charlie");
        Mammal mammal = new Mammal("unknown");
        kid.hasPet(mammal);
        session.save(kid);

        session.clear();
        Kid charlie = session.loadAll(Kid.class).iterator().next();
        assertThat(charlie).isNotNull();
        assertThat(charlie.getPets()).hasSize(1);
        assertThat(charlie.getPets().iterator().next().getName()).isEqualTo(mammal.getName());
    }

    /**
     * @see issue #40
     */
    @Test
    public void shouldBeAbleToSaveAndLoadDogs() {
        Kid kid = new Kid("Charlie");
        Dog dog = new Dog("Snoopy");
        kid.hasPet(dog);
        session.save(kid);

        session.clear();
        Kid charlie = session.loadAll(Kid.class).iterator().next();
        assertThat(charlie).isNotNull();
        assertThat(charlie.getPets()).hasSize(1);
        assertThat(charlie.getPets().iterator().next().getName()).isEqualTo(dog.getName());
    }

    /**
     * @see issue #40
     */
    @Test
    public void shouldBeAbleToSaveAndLoadDogsDirectly() {
        Dog dog = new Dog("Snoopy");
        session.save(dog);

        session.clear();

        Dog snoopy = session.loadAll(Dog.class).iterator().next();
        assertThat(snoopy).isNotNull();
        assertThat(snoopy.getName()).isEqualTo(dog.getName());
    }

    /**
     * @see issue #40
     */
    @Test
    public void shouldBeAbleToSaveAndLoadDomesticDogsDirectly() {
        DomesticDog dog = new DomesticDog("Snoopy");
        session.save(dog);

        session.clear();

        DomesticDog snoopy = session.loadAll(DomesticDog.class).iterator().next();
        assertThat(snoopy).isNotNull();
        assertThat(snoopy.getName()).isEqualTo(dog.getName());
    }
}
