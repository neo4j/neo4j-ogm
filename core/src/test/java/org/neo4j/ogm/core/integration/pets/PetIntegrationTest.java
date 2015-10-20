/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.core.integration.pets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.api.driver.Driver;
import org.neo4j.ogm.config.Components;
import org.neo4j.ogm.core.domain.pets.Dog;
import org.neo4j.ogm.core.domain.pets.DomesticDog;
import org.neo4j.ogm.core.domain.pets.Kid;
import org.neo4j.ogm.core.domain.pets.Mammal;
import org.neo4j.ogm.core.domain.pets.Dog;
import org.neo4j.ogm.core.domain.pets.DomesticDog;
import org.neo4j.ogm.core.domain.pets.Kid;
import org.neo4j.ogm.core.domain.pets.Mammal;
import org.neo4j.ogm.core.MetaData;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Luanne Misquitta
 */

public class PetIntegrationTest {
    private static final Driver driver = Components.driver();

	private Session session;

	@Before
	public void init() throws IOException {
		session = new SessionFactory("org.neo4j.ogm.domain.pets").openSession(driver);
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
		assertEquals("org.neo4j.ogm.domain.pets.Animal",metaData.resolve("Animal").name());
		assertEquals("org.neo4j.ogm.domain.pets.Mammal", metaData.resolve("Mammal", "Animal").name());
		assertEquals("org.neo4j.ogm.domain.pets.Dog", metaData.resolve("Mammal", "Animal", "Dog").name());
		assertEquals("org.neo4j.ogm.domain.pets.Dog", metaData.resolve("Dog", "Mammal", "Animal").name());
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
		assertNotNull(charlie);
		assertEquals(1, charlie.getPets().size());assertEquals(mammal.getName(), charlie.getPets().iterator().next().getName());
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
		assertNotNull(charlie);
		assertEquals(1, charlie.getPets().size());
		assertEquals(dog.getName(), charlie.getPets().iterator().next().getName());
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
		assertNotNull(snoopy);
		assertEquals(dog.getName(),snoopy.getName());
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
		assertNotNull(snoopy);
		assertEquals(dog.getName(),snoopy.getName());
	}

}
