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

package org.neo4j.ogm.persistence.examples.pizza;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

import org.junit.*;
import org.neo4j.ogm.domain.pizza.*;
import org.neo4j.ogm.exception.MappingException;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.GraphTestUtils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Luanne Misquitta
 */
public class PizzaIntegrationTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getBaseConfiguration().build(), "org.neo4j.ogm.domain.pizza");

    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
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
        Pizza pizza = new Pizza("Mushroom & Pepperoni", crust, Arrays.asList(mushroom, pepperoni));
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
        PizzaSeasoning pizzaSeasoning = new PizzaSeasoning(pizza, seasoning, Quantity.DIE_TOMORROW);
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
        Pizza pizza = new Pizza("Mushroom & Pepperoni", crust, Arrays.asList(mushroom, pepperoni));
        pizza.setPizzaSauce(pizzaSauce);
        Seasoning seasoning = new Seasoning("Chilli Flakes");
        PizzaSeasoning pizzaSeasoning = new PizzaSeasoning(pizza, seasoning, Quantity.DIE_TOMORROW);
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

    /**
     * @see Issue #61
     */
    @Test
    public void shouldUseOptimizedCypherWhenSavingRelationships() {
        Crust crust = new Crust("Thin Crust");
        session.save(crust);
        Topping mushroom = new Topping("Mushroom");
        session.save(mushroom);
        Topping pepperoni = new Topping("Pepperoni");
        session.save(pepperoni);
        Pizza pizza = new Pizza();
        pizza.setName("Mushroom & Pepperoni");
        session.save(pizza);

        pizza.setCrust(crust);
        pizza.setToppings(Arrays.asList(mushroom, pepperoni));
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
     * @see issue #159
     */
    @Test
    public void shouldSyncMappedLabelsFromEntityToTheNode_and_NodeToEntity_noGetterOrSetter() {

        Pizza pizza = new Pizza();
        pizza.setName("Mushroom & Pepperoni");
        List<String> labels = new ArrayList<>();
        labels.add("Delicious");
        labels.add("Hot");
        labels.add("Spicy");
        pizza.setLabels(labels);

        session.save(pizza);
        session.clear();
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(), "CREATE (n:`Pizza`:`Spicy`:`Hot`:`Delicious` {name: 'Mushroom & Pepperoni'})");

        Pizza loadedPizza = session.load(Pizza.class, pizza.getId());
        assertTrue(loadedPizza.getLabels().contains("Delicious"));
        assertTrue(loadedPizza.getLabels().contains("Hot"));
        assertTrue(loadedPizza.getLabels().contains("Spicy"));
        assertEquals(3, loadedPizza.getLabels().size());

        List<String> newLabels = new ArrayList<>();
        newLabels.add("Cold");
        newLabels.add("Stale");
        loadedPizza.setLabels(newLabels);

        session.save(loadedPizza);
        session.clear();

        Pizza reloadedPizza = session.load(Pizza.class, pizza.getId());
        assertEquals(2, reloadedPizza.getLabels().size());
        assertTrue(reloadedPizza.getLabels().contains("Cold"));
        assertTrue(reloadedPizza.getLabels().contains("Stale"));

        newLabels = new ArrayList<>();
        newLabels.add("Cold");
        newLabels.add("Decomposed");
        reloadedPizza.setLabels(newLabels);

        session.save(reloadedPizza);
        session.clear();

        Pizza zombiePizza = session.load(Pizza.class, pizza.getId());
        assertEquals(2, zombiePizza.getLabels().size());
        assertTrue(zombiePizza.getLabels().contains("Cold"));
        assertTrue(zombiePizza.getLabels().contains("Decomposed"));
    }

    /**
     * @see issue #159
     */
    @Test
    public void shouldApplyLabelsWhenSessionClearedBeforeSave() {

        Pizza pizza = new Pizza();
        pizza.setName("Mushroom & Pepperoni");
        List<String> labels = new ArrayList<>();
        labels.add("Delicious");
        labels.add("Hot");
        labels.add("Spicy");
        pizza.setLabels(labels);

        session.save(pizza);
        session.clear();
        GraphTestUtils.assertSameGraph(getGraphDatabaseService(), "CREATE (n:`Pizza`:`Spicy`:`Hot`:`Delicious` {name: 'Mushroom & Pepperoni'})");

        Pizza loadedPizza = session.load(Pizza.class, pizza.getId());
        assertTrue(loadedPizza.getLabels().contains("Delicious"));
        assertTrue(loadedPizza.getLabels().contains("Hot"));
        assertTrue(loadedPizza.getLabels().contains("Spicy"));
        assertEquals(3, loadedPizza.getLabels().size());

        List<String> newLabels = new ArrayList<>();
        newLabels.add("Cold");
        newLabels.add("Stale");
        loadedPizza.setLabels(newLabels);

        session.clear(); //Clear session before save
        session.save(loadedPizza);

        Pizza reloadedPizza = session.load(Pizza.class, pizza.getId());
        assertEquals(2, reloadedPizza.getLabels().size());
        assertTrue(reloadedPizza.getLabels().contains("Cold"));
        assertTrue(reloadedPizza.getLabels().contains("Stale"));
    }


    /**
     * @see issue #159
     */
    @Test
    public void shouldRaiseExceptionWhenAmbiguousClassLabelApplied() {

        Session session = new SessionFactory(getBaseConfiguration().build(), "org.neo4j.ogm.domain.pizza", "org.neo4j.ogm.domain.music").openSession();

        Pizza pizza = new Pizza();
        pizza.setName("Mushroom & Pepperoni");
        List<String> labels = new ArrayList<>();
        //We're adding the studio label, which is mapped to a type
        labels.add("Studio");
        pizza.setLabels(labels);

        session.save(pizza);
        session.clear();

        try {
            session.load(Pizza.class, pizza.getId());
        } catch (MappingException e) {
            assertEquals("Multiple classes found in type hierarchy that map to: [Pizza, Studio]", e.getCause().getMessage());
        }
    }

    @Test
    public void shouldUpdateSessionContextAfterSaveForSingleAndMultiStatementCypherQueries() {

        Pizza pizza = new Pizza();

        session.save(pizza);

        // detach the pizza
        session.clear();

        // NOTE: if we instead reload into our pizza object, the test will pass, but what
        // this does is create a new object in the session. And, rather than work with
        // the new attached object 'loadedPizza', we continue to work with the detached object, 'pizza'.
        // this is the first condition for failure.
        Pizza loadedPizza = session.load(Pizza.class, pizza.getId());

        // now we create a relationship and update a property on the detached pizza object.
        // note that we don't save the Crust first. Crust is a new object when pizza is saved, so
        // we will generate a 2-statement cypher request. This is the second condition for failure
        Crust crust = new Crust("Thin Crust");
        pizza.setCrust(crust);
        pizza.setName("Just bread");

        // pizza should be dirty
        Assert.assertTrue(((Neo4jSession) session).context().isDirty(pizza));
        session.save(pizza);
        // pizza should NOT be dirty
        Assert.assertFalse(((Neo4jSession) session).context().isDirty(pizza)); // this should pass
    }


    @Test
    public void shouldBeAbleToModifyPropertiesAndRelsWithinSingleSave() {
        Crust crust = new Crust("Thin Crust");
        Topping pepperoni = new Topping("Pepperoni");
        final ArrayList<Topping> toppings = new ArrayList<>();
        toppings.add(pepperoni);
        Pizza pizza = new Pizza("Godfather", crust, toppings);

        session.save(pizza);

        Topping mushroom = new Topping("Mushroom");

        session.save(pepperoni);
        session.save(mushroom);

        session.clear();

        Long id = pizza.getId();

        Assert.assertNotNull(id);
        // detach the pizza

        Session session2 = sessionFactory.openSession();

        // NOTE: if we instead reload into our pizza object, the test will pass, but what
        // this does is create a new object in the session. And, rather than work with
        // the new attached object 'loadedPizza', we continue to work with the detached object, 'pizza'.
        // this is the first condition for failure.
        Pizza loadedPizza = session2.load(Pizza.class, id);

        // now we create a relationship and update a property on the detached pizza object.
        // note that we don't save the Crust first. Crust is a new object when pizza is saved, so
        // we will generate a 2-statement cypher request. This is the second condition for failure
        loadedPizza.setName("Just bread");

        loadedPizza.getToppings().clear();

        Topping pepperoniTopping = session2.load(Topping.class, pepperoni.getId());
        loadedPizza.getToppings().add(pepperoniTopping);

        Topping mushroomTopping = session2.load(Topping.class, pepperoni.getId());
        loadedPizza.getToppings().add(mushroomTopping);

        // pizza should be dirty
        Assert.assertTrue(((Neo4jSession) session2).context().isDirty(loadedPizza));
        session2.save(loadedPizza);
        // pizza should NOT be dirty - but it is, indicating it's not current in the cache.
        Assert.assertFalse(((Neo4jSession) session2).context().isDirty(loadedPizza)); // this should pass
    }

    /**
     * @see issue #209
     */
    @Test
    public void shouldMarkLabelsAsDirtyWhenExistingCollectionUpdated() {
        Pizza entity = new Pizza();
        List<String> labels = new ArrayList<>();
        labels.add("TestLabel1");
        labels.add("TestLabel2");
        entity.setLabels(labels);
        session.save(entity);
        session.clear();

        entity = session.load(Pizza.class, entity.getId());
        entity.getLabels().remove("TestLabel1");
        session.save(entity);
        session.clear();

        labels = session.load(Pizza.class, entity.getId()).getLabels();
        assertEquals(1, labels.size());
    }

    @Test
    public void shouldDeleteChangedIncomingRelationship() throws Exception {
        Pizza pizza = new Pizza();
        Crust deepDishCrust = new Crust("Deep Dish");
        Crust thinNCrispyCrust = new Crust("Thin 'n Crispy");

        pizza.setCrust(deepDishCrust);
        session.save(pizza);

        assertOneRelationshipInDb();

        pizza.setCrust(thinNCrispyCrust);
        session.save(pizza);

        assertOneRelationshipInDb();
    }


    @Test
    public void shouldDeleteChangedIncomingRelationshipWithClearSessionAndLoad() throws Exception {

        Pizza pizza = new Pizza();
        Crust deepDishCrust = new Crust("Deep Dish");
        Crust thinNCrispyCrust = new Crust("Thin 'n Crispy");

        pizza.setCrust(deepDishCrust);
        session.save(pizza);

        assertOneRelationshipInDb();

        session.clear();
        pizza = session.load(Pizza.class, pizza.getId());
        pizza.setCrust(thinNCrispyCrust);
        session.save(pizza);

        assertOneRelationshipInDb();
    }

    private void assertOneRelationshipInDb() {
        Result result = session.query("MATCH (p:Pizza)-[r]-() return count(r) as c", new HashMap<String, Object>());
        Map<String, Object> row = result.iterator().next();
        Number count = (Number) row.get("c");
        assertEquals(1L, count.longValue());
    }
}
