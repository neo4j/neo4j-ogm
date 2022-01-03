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
package org.neo4j.ogm.persistence.examples.pizza;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.pizza.*;
import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.GraphTestUtils;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Luanne Misquitta
 * @author Jonathan D'Orleans
 * @author Michael J. Simons
 */
public class PizzaIntegrationTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.pizza");

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
        assertThat(loadedPizza).isNotNull();
        assertThat(loadedPizza.getName()).isEqualTo(pizza.getName());
        assertThat(loadedPizza.getCrust()).isNotNull();
        assertThat(loadedPizza.getCrust().getName()).isEqualTo(crust.getName());
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
        assertThat(loadedPizza).isNotNull();
        assertThat(loadedPizza.getName()).isEqualTo(pizza.getName());
        assertThat(loadedPizza.getToppings()).isNotNull();
        assertThat(loadedPizza.getToppings()).hasSize(2);
        assertThat(loadedPizza.getToppings().contains(mushroom)).isTrue();
        assertThat(loadedPizza.getToppings().contains(pepperoni)).isTrue();
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
        assertThat(loadedPizza).isNotNull();
        assertThat(loadedPizza.getName()).isEqualTo(pizza.getName());
        assertThat(loadedPizza.getCrust()).isNotNull();
        assertThat(loadedPizza.getCrust().getName()).isEqualTo(crust.getName());
        assertThat(loadedPizza.getToppings()).isNotNull();
        assertThat(loadedPizza.getToppings()).hasSize(2);
        assertThat(loadedPizza.getToppings().contains(mushroom)).isTrue();
        assertThat(loadedPizza.getToppings().contains(pepperoni)).isTrue();
    }

    @Test // See #36
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
        assertThat(loadedPizza).isNotNull();
        assertThat(loadedPizza.getName()).isEqualTo(pizza.getName());
        assertThat(loadedPizza.getPizzaSauce()).isNotNull();
        assertThat(loadedPizza.getPizzaSauce().isSpicy()).isTrue();
        assertThat(loadedPizza.getPizzaSauce().getSauce().getName()).isEqualTo(sauce.getName());
    }

    @Test // See #36
    public void shouldBeAbleToSaveAndLoadAPizzaWithSeasonings() {
        Seasoning seasoning = new Seasoning("Chilli Flakes");
        Pizza pizza = new Pizza("Crazy Hot Pizza");
        PizzaSeasoning pizzaSeasoning = new PizzaSeasoning(pizza, seasoning, Quantity.DIE_TOMORROW);
        session.save(pizzaSeasoning);

        session.clear();
        Pizza loadedPizza = session.load(Pizza.class, pizza.getId());
        assertThat(loadedPizza).isNotNull();
        assertThat(loadedPizza.getName()).isEqualTo(pizza.getName());
        assertThat(loadedPizza.getSeasonings()).hasSize(1);
        assertThat(loadedPizza.getSeasonings().iterator().next().getSeasoning().getName())
            .isEqualTo(seasoning.getName());
        assertThat(loadedPizza.getSeasonings().iterator().next().getQuantity()).isEqualTo(Quantity.DIE_TOMORROW);
    }

    @Test // See #36
    public void shouldBeAbleToSaveAndLoadAPizzaWithCheese() {
        Cheese cheese = new Cheese("Mozzarella");
        Pizza pizza = new Pizza("Cheesy!");
        PizzaCheese pizzaCheese = new PizzaCheese(pizza, cheese, Quantity.DOUBLE);
        session.save(pizzaCheese);

        session.clear();
        Pizza loadedPizza = session.load(Pizza.class, pizza.getId());
        assertThat(loadedPizza).isNotNull();
        assertThat(loadedPizza.getName()).isEqualTo(pizza.getName());
        assertThat(loadedPizza.getCheeses()).hasSize(1);
        assertThat(loadedPizza.getCheeses().iterator().next().getCheese().getName()).isEqualTo(cheese.getName());
        assertThat(loadedPizza.getCheeses().iterator().next().getQuantity()).isEqualTo(Quantity.DOUBLE);
    }

    @Test // See #36
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
        assertThat(loadedPizza).isNotNull();
        assertThat(loadedPizza.getName()).isEqualTo(pizza.getName());
        assertThat(loadedPizza.getPizzaSauce()).isNotNull();
        assertThat(loadedPizza.getPizzaSauce().isSpicy()).isTrue();
        assertThat(loadedPizza.getPizzaSauce().getSauce().getName()).isEqualTo(sauce.getName());
        assertThat(loadedPizza.getCrust()).isNotNull();
        assertThat(loadedPizza.getCrust().getName()).isEqualTo(crust.getName());
        assertThat(loadedPizza.getToppings()).isNotNull();
        assertThat(loadedPizza.getToppings()).hasSize(2);
        assertThat(loadedPizza.getToppings().contains(mushroom)).isTrue();
        assertThat(loadedPizza.getToppings().contains(pepperoni)).isTrue();
        assertThat(loadedPizza.getSeasonings()).hasSize(1);
        assertThat(loadedPizza.getSeasonings().iterator().next().getSeasoning().getName())
            .isEqualTo(seasoning.getName());
        assertThat(loadedPizza.getSeasonings().iterator().next().getQuantity()).isEqualTo(Quantity.DIE_TOMORROW);
        assertThat(loadedPizza.getCheeses()).hasSize(1);
        assertThat(loadedPizza.getCheeses().iterator().next().getCheese().getName()).isEqualTo(cheese.getName());
        assertThat(loadedPizza.getCheeses().iterator().next().getQuantity()).isEqualTo(Quantity.DOUBLE);
    }

    @Test // See #61
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
        assertThat(loadedPizza).isNotNull();
        assertThat(loadedPizza.getName()).isEqualTo(pizza.getName());
        assertThat(loadedPizza.getCrust()).isNotNull();
        assertThat(loadedPizza.getCrust().getName()).isEqualTo(crust.getName());
        assertThat(loadedPizza.getToppings()).isNotNull();
        assertThat(loadedPizza.getToppings()).hasSize(2);
        assertThat(loadedPizza.getToppings().contains(mushroom)).isTrue();
        assertThat(loadedPizza.getToppings().contains(pepperoni)).isTrue();
    }

    @Test // See #159
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
//        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
//            "CREATE (n:`Pizza`:`Spicy`:`Hot`:`Delicious` {name: 'Mushroom & Pepperoni'})");

        Pizza loadedPizza = session.load(Pizza.class, pizza.getId());
        assertThat(loadedPizza.getLabels().contains("Delicious")).isTrue();
        assertThat(loadedPizza.getLabels().contains("Hot")).isTrue();
        assertThat(loadedPizza.getLabels().contains("Spicy")).isTrue();
        assertThat(loadedPizza.getLabels()).hasSize(3);

        List<String> newLabels = new ArrayList<>();
        newLabels.add("Cold");
        newLabels.add("Stale");
        loadedPizza.setLabels(newLabels);

        session.save(loadedPizza);
        session.clear();

        Pizza reloadedPizza = session.load(Pizza.class, pizza.getId());
        assertThat(reloadedPizza.getLabels()).hasSize(2);
        assertThat(reloadedPizza.getLabels().contains("Cold")).isTrue();
        assertThat(reloadedPizza.getLabels().contains("Stale")).isTrue();

        newLabels = new ArrayList<>();
        newLabels.add("Cold");
        newLabels.add("Decomposed");
        reloadedPizza.setLabels(newLabels);

        session.save(reloadedPizza);
        session.clear();

        Pizza zombiePizza = session.load(Pizza.class, pizza.getId());
        assertThat(zombiePizza.getLabels()).hasSize(2);
        assertThat(zombiePizza.getLabels().contains("Cold")).isTrue();
        assertThat(zombiePizza.getLabels().contains("Decomposed")).isTrue();
    }

    @Test // See #159
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
//        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
//            "CREATE (n:`Pizza`:`Spicy`:`Hot`:`Delicious` {name: 'Mushroom & Pepperoni'})");

        Pizza loadedPizza = session.load(Pizza.class, pizza.getId());
        assertThat(loadedPizza.getLabels().contains("Delicious")).isTrue();
        assertThat(loadedPizza.getLabels().contains("Hot")).isTrue();
        assertThat(loadedPizza.getLabels().contains("Spicy")).isTrue();
        assertThat(loadedPizza.getLabels()).hasSize(3);

        List<String> newLabels = new ArrayList<>();
        newLabels.add("Cold");
        newLabels.add("Stale");
        loadedPizza.setLabels(newLabels);

        session.clear(); //Clear session before save
        session.save(loadedPizza);

        Pizza reloadedPizza = session.load(Pizza.class, pizza.getId());
        assertThat(reloadedPizza.getLabels()).hasSize(2);
        assertThat(reloadedPizza.getLabels().contains("Cold")).isTrue();
        assertThat(reloadedPizza.getLabels().contains("Stale")).isTrue();
    }

    @Test // See #159
    public void shouldRaiseExceptionWhenAmbiguousClassLabelApplied() {

        Session sessionWithAmbiguousDomain = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.pizza", "org.neo4j.ogm.domain.music")
            .openSession();

        Pizza pizza = new Pizza();
        pizza.setName("Mushroom & Pepperoni");
        List<String> labels = new ArrayList<>();
        //We're adding the studio label, which is mapped to a type
        labels.add("Studio");
        pizza.setLabels(labels);

        sessionWithAmbiguousDomain.save(pizza);
        sessionWithAmbiguousDomain.clear();

        try {
            sessionWithAmbiguousDomain.load(Pizza.class, pizza.getId());
        } catch (MappingException e) {
            assertThat(e.getMessage())
                .isEqualTo("Multiple classes found in type hierarchy that map to: [Pizza, Studio]");
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
        assertThat(((Neo4jSession) session).context().isDirty(pizza)).isTrue();
        session.save(pizza);
        // pizza should NOT be dirty
        assertThat(((Neo4jSession) session).context().isDirty(pizza)).isFalse(); // this should pass
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

        assertThat(id).isNotNull();
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
        assertThat(((Neo4jSession) session2).context().isDirty(loadedPizza)).isTrue();
        session2.save(loadedPizza);
        // pizza should NOT be dirty - but it is, indicating it's not current in the cache.
        assertThat(((Neo4jSession) session2).context().isDirty(loadedPizza)).isFalse(); // this should pass
    }

    @Test // See #209
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
        assertThat(labels).hasSize(1);
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

    @Test // See 488
    public void shouldUpdateLabelWhenLoadingEntityInSameSession() {
        Pizza pizza = new Pizza();
        pizza.addLabel("A0");
        session.save(pizza);
        session.clear();

        Pizza dbPizza = session.load(Pizza.class, pizza.getId());
        assertThat(dbPizza.getLabels().size()).isEqualTo(1);
        assertThat(dbPizza.getLabels()).contains("A0");
        dbPizza.removeLabel("A0");
        dbPizza.addLabel("A1");
        session.save(dbPizza);
        session.clear();

        dbPizza = session.load(Pizza.class, pizza.getId());
        assertThat(dbPizza.getLabels().size()).isEqualTo(1);
        assertThat(dbPizza.getLabels()).contains("A1");
    }

    @Test  // See 488
    public void shouldUpdateLabelWhenLoadingEntityInNewSession() {
        Pizza pizza = new Pizza();
        pizza.addLabel("A0");
        session.save(pizza);
        Session newSession = sessionFactory.openSession();

        Pizza dbPizza = newSession.load(Pizza.class, pizza.getId());
        assertThat(dbPizza.getLabels().size()).isEqualTo(1);
        assertThat(dbPizza.getLabels()).contains("A0");
        dbPizza.removeLabel("A0");
        dbPizza.addLabel("A1");
        newSession.save(dbPizza);
        newSession.clear();

        dbPizza = newSession.load(Pizza.class, pizza.getId());
        assertThat(dbPizza.getLabels().size()).isEqualTo(1);
        assertThat(dbPizza.getLabels()).contains("A1");
    }


    private void assertOneRelationshipInDb() {
        Result result = sessionFactory.openSession().query("MATCH (p:Pizza)-[r]-() return count(r) as c", new HashMap<>());
        Map<String, Object> row = result.iterator().next();
        Number count = (Number) row.get("c");
        assertThat(count.longValue()).isEqualTo(1L);
    }
}
