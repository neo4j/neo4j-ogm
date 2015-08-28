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

package org.neo4j.ogm.integration.pizza;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.neo4j.ogm.domain.pizza.*;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.Neo4jIntegrationTestRule;

/**
 * @author Luanne Misquitta
 */
public class PizzaIntegrationTest {
	@ClassRule
	public static Neo4jIntegrationTestRule neo4jRule = new Neo4jIntegrationTestRule();

	private Session session;

	@Before
	public void init() throws IOException {
		session = new SessionFactory("org.neo4j.ogm.domain.pizza").openSession(neo4jRule.url());
	}

	@After
	public void teardown() {
		session.purgeDatabase();
	}

	@Test
	public void shouldBeAbleToSaveAndLoadPizzaWithCrustOnly() {
		Crust crust = new Crust("Thin Crust");
		Pizza pizza = new Pizza("Just bread");
		pizza.setCrust(crust);
		session.save(pizza);

		session.clear();

		Pizza loadedPizza = session.load(Pizza.class, pizza.getId());
		assertNotNull(loadedPizza);
		assertEquals(pizza.getName(), loadedPizza.getName());
		assertNotNull(loadedPizza.getCrust());
		assertEquals(crust.getName(), loadedPizza.getCrust().getName());
	}

	@Test
	public void shouldBeAbleToSaveAndLoadPizzaWithToppingsOnly() {
		Topping mushroom = new Topping("Mushroom");
		Topping pepperoni = new Topping("Pepperoni");
		Pizza pizza = new Pizza("Just toppings");
		pizza.setToppings(Arrays.asList(mushroom, pepperoni));
		session.save(pizza);

		session.clear();

		Pizza loadedPizza = session.load(Pizza.class, pizza.getId());
		assertNotNull(loadedPizza);
		assertEquals(pizza.getName(), loadedPizza.getName());
		assertNotNull(loadedPizza.getToppings());
		assertEquals(2, loadedPizza.getToppings().size());
		assertTrue(loadedPizza.getToppings().contains(mushroom));
		assertTrue(loadedPizza.getToppings().contains(pepperoni));
	}

	@Test
	public void shouldBeAbleToSaveAndLoadPizzaWithToppingsAndCrust() {
		Crust crust = new Crust("Thin Crust");
		Topping mushroom = new Topping("Mushroom");
		Topping pepperoni = new Topping("Pepperoni");
		Pizza pizza = new Pizza("Mushroom & Pepperoni", crust, Arrays.asList(mushroom,pepperoni));
		session.save(pizza);

		session.clear();

		Pizza loadedPizza = session.load(Pizza.class, pizza.getId());
		assertNotNull(loadedPizza);
		assertEquals(pizza.getName(), loadedPizza.getName());
		assertNotNull(loadedPizza.getCrust());
		assertEquals(crust.getName(), loadedPizza.getCrust().getName());
		assertNotNull(loadedPizza.getToppings());
		assertEquals(2, loadedPizza.getToppings().size());
		assertTrue(loadedPizza.getToppings().contains(mushroom));
		assertTrue(loadedPizza.getToppings().contains(pepperoni));
	}

	/**
	 * @see issue #36
	 */
	@Test
	public void shouldBeAbleToSavePizzaWithOnlySauce() {
		Sauce sauce = new Sauce("Marinara");
		PizzaSauce pizzaSauce = new PizzaSauce();
		pizzaSauce.setSauce(sauce);
		pizzaSauce.setSpicy(true);
		Pizza pizza = new Pizza("Just sauce");
		pizza.setPizzaSauce(pizzaSauce);
		//sauce.setPizzaSauce(pizzaSauce);
		session.save(pizza);

		session.clear();

		Pizza loadedPizza = session.load(Pizza.class, pizza.getId());
		assertNotNull(loadedPizza);
		assertEquals(pizza.getName(), loadedPizza.getName());
		assertNotNull(loadedPizza.getPizzaSauce());
		assertTrue(loadedPizza.getPizzaSauce().isSpicy());
		assertEquals(sauce.getName(), loadedPizza.getPizzaSauce().getSauce().getName());
	}

	/**
	 * @see issue #36
	 */
	@Test
	public void shouldBeAbleToSaveAndLoadAPizzaWithSeasonings() {
		Seasoning seasoning = new Seasoning("Chilli Flakes");
		Pizza pizza = new Pizza("Crazy Hot Pizza");
		PizzaSeasoning pizzaSeasoning= new PizzaSeasoning(pizza,seasoning,Quantity.DIE_TOMORROW);
		session.save(pizzaSeasoning);

		session.clear();
		Pizza loadedPizza = session.load(Pizza.class, pizza.getId());
		assertNotNull(loadedPizza);
		assertEquals(pizza.getName(), loadedPizza.getName());
		assertEquals(1, loadedPizza.getSeasonings().size());
		assertEquals(seasoning.getName(), loadedPizza.getSeasonings().iterator().next().getSeasoning().getName());
		assertEquals(Quantity.DIE_TOMORROW, loadedPizza.getSeasonings().iterator().next().getQuantity());
	}

	/**
	 * @see issue #36
	 */
	@Test
	public void shouldBeAbleToSaveAndLoadAPizzaWithCheese() {
		Cheese cheese = new Cheese("Mozzarella");
		Pizza pizza = new Pizza("Cheesy!");
		PizzaCheese pizzaCheese = new PizzaCheese(pizza, cheese, Quantity.DOUBLE);
		session.save(pizzaCheese);

		session.clear();
		Pizza loadedPizza = session.load(Pizza.class, pizza.getId());
		assertNotNull(loadedPizza);
		assertEquals(pizza.getName(), loadedPizza.getName());
		assertEquals(1, loadedPizza.getCheeses().size());
		assertEquals(cheese.getName(), loadedPizza.getCheeses().iterator().next().getCheese().getName());
		assertEquals(Quantity.DOUBLE, loadedPizza.getCheeses().iterator().next().getQuantity());
	}


	/**
	 * @see issue #36
	 */
	@Test
	public void shouldBeAbleToSaveAndRetrieveFullyLoadedPizza() {
		Crust crust = new Crust("Thin Crust");
		Topping mushroom = new Topping("Mushroom");
		Topping pepperoni = new Topping("Pepperoni");
		Sauce sauce = new Sauce("Marinara");
		PizzaSauce pizzaSauce = new PizzaSauce();
		pizzaSauce.setSauce(sauce);
		pizzaSauce.setSpicy(true);
		Pizza pizza = new Pizza("Mushroom & Pepperoni", crust, Arrays.asList(mushroom,pepperoni));
		pizza.setPizzaSauce(pizzaSauce);
		Seasoning seasoning = new Seasoning("Chilli Flakes");
		PizzaSeasoning pizzaSeasoning= new PizzaSeasoning(pizza,seasoning,Quantity.DIE_TOMORROW);
		Cheese cheese = new Cheese("Mozzarella");
		PizzaCheese pizzaCheese = new PizzaCheese(pizza, cheese, Quantity.DOUBLE);
		session.save(pizza);
		session.save(pizzaSeasoning);
		session.save(pizzaCheese);

		session.clear();

		Pizza loadedPizza = session.load(Pizza.class, pizza.getId());
		assertNotNull(loadedPizza);
		assertEquals(pizza.getName(), loadedPizza.getName());
		assertNotNull(loadedPizza.getPizzaSauce());
		assertTrue(loadedPizza.getPizzaSauce().isSpicy());
		assertEquals(sauce.getName(), loadedPizza.getPizzaSauce().getSauce().getName());
		assertNotNull(loadedPizza.getCrust());
		assertEquals(crust.getName(), loadedPizza.getCrust().getName());
		assertNotNull(loadedPizza.getToppings());
		assertEquals(2, loadedPizza.getToppings().size());
		assertTrue(loadedPizza.getToppings().contains(mushroom));
		assertTrue(loadedPizza.getToppings().contains(pepperoni));
		assertEquals(1, loadedPizza.getSeasonings().size());
		assertEquals(seasoning.getName(), loadedPizza.getSeasonings().iterator().next().getSeasoning().getName());
		assertEquals(Quantity.DIE_TOMORROW, loadedPizza.getSeasonings().iterator().next().getQuantity());
		assertEquals(1, loadedPizza.getCheeses().size());
		assertEquals(cheese.getName(), loadedPizza.getCheeses().iterator().next().getCheese().getName());
		assertEquals(Quantity.DOUBLE, loadedPizza.getCheeses().iterator().next().getQuantity());
	}


}
