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
package org.neo4j.ogm.persistence.examples.drink;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.drink.Beverage;
import org.neo4j.ogm.domain.drink.Ingredient;
import org.neo4j.ogm.domain.drink.Manufacturer;
import org.neo4j.ogm.domain.drink.Owns;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.event.EventListenerAdapter;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class DrinkIntegrationTest extends TestContainersTestBase {

    private static SessionFactory sf;
    private Session session;

    @BeforeClass
    public static void setUpClass() throws Exception {
        sf = new SessionFactory(getDriver(), Beverage.class.getPackage().getName());

        // register dummy listener to SaveEventDelegate code
        sf.register(new EventListenerAdapter());
    }

    @Before
    public void setUp() throws Exception {
        session = sf.openSession();

        session.purgeDatabase();
    }

    @Test
    public void shouldSaveAndLoadEntityWithoutGraphId() throws Exception {
        Beverage beverage = new Beverage("Pilsner Urquell");
        session.save(beverage);

        assertBeverageInDB(beverage);

        session.clear();
        assertBeverageInDB(beverage);
    }

    @Test
    public void shouldLoadAllBeverages() throws Exception {
        Beverage pilsner = new Beverage("Pilsner Urquell");
        session.save(pilsner);

        Beverage budweiser = new Beverage("Budweiser Budvar");
        session.save(budweiser);

        // contains same instances
        Collection<Beverage> beverages = session.loadAll(Beverage.class);
        assertThat(beverages).containsOnly(pilsner, budweiser);

        session.clear();

        // after clear -> comparing uuids - no equals on beverage
        Collection<Beverage> loaded = session.loadAll(Beverage.class);
        assertThat(loaded).extracting(Beverage::getUuid)
            .containsOnly(pilsner.getUuid(), budweiser.getUuid());
    }

    @Test
    public void shouldLoadAllByInstances() throws Exception {
        Beverage pilsner = new Beverage("Pilsner Urquell");
        session.save(pilsner);

        Collection<Beverage> beverages = session.loadAll(Arrays.asList(pilsner));
        assertThat(beverages).containsOnly(pilsner);
    }

    @Test
    public void loadReturnsSameInstance() throws Exception {
        Beverage beverage = new Beverage("Pilsner Urquell");
        session.save(beverage);

        Beverage loaded = session.load(Beverage.class, beverage.getUuid());
        assertThat(loaded).isSameAs(beverage);
    }

    @Test
    public void shouldCorrectlyUpdateInDb() throws Exception {
        Beverage beverage = new Beverage("Pilsenerer Urquell");
        session.save(beverage);

        beverage.setName("Pilsner Urquell");
        session.save(beverage);

        assertBeverageInDB(beverage);

        session.clear();
        assertBeverageInDB(beverage);
    }

    @Test
    public void shouldDeleteEntity() throws Exception {
        Beverage beverage = new Beverage("Pilsenerer Urquell");
        session.save(beverage);

        session.delete(beverage);

        assertNoBeverage();
    }

    @Test
    public void shouldDeleteRelationship() throws Exception {
        Manufacturer prazdroj = new Manufacturer("Plzeňský Prazdroj, a. s.");
        Beverage pilsner = new Beverage("Pilsenerer Urquell");
        prazdroj.addBeverage(pilsner);

        session.save(prazdroj);

        prazdroj.getBeverages().clear();
        pilsner.setManufacturer(null);

        // should delete relationship
        session.save(prazdroj);

        Manufacturer loadedPrazdroj = session.load(Manufacturer.class, prazdroj.getUuid());
        assertThat(loadedPrazdroj.getBeverages()).isNullOrEmpty();

        session.clear();
        loadedPrazdroj = session.load(Manufacturer.class, prazdroj.getUuid());
        assertThat(loadedPrazdroj.getBeverages()).isNullOrEmpty();
    }

    @Test
    public void shouldDeleteRelationshipEntity() throws Exception {
        Manufacturer prazdroj = new Manufacturer("Plzeňský Prazdroj, a. s.");
        Manufacturer asahi = new Manufacturer("Asahi Breweries, Ltd.");
        asahi.acquired(2017, prazdroj);

        session.save(asahi);

        asahi.getAcquisitions().clear();

        session.save(asahi);
        Manufacturer loaded = session.load(Manufacturer.class, asahi.getUuid());
        assertThat(loaded.getAcquisitions()).isNullOrEmpty();

        session.clear();
        loaded = session.load(Manufacturer.class, asahi.getUuid());
        assertThat(loaded.getAcquisitions()).isNullOrEmpty();
    }

    private void assertBeverageInDB(Beverage beverage) {
        Beverage loaded = session.load(Beverage.class, beverage.getUuid());
        assertThat(loaded.getUuid()).isEqualTo(beverage.getUuid());
        assertThat(loaded.getName()).isEqualTo(beverage.getName());
    }

    @Test
    public void shouldSaveEntityWithoutAndWithGraphID() throws Exception {
        Beverage pilsner = new Beverage("Pilsner Urquell");
        Ingredient water = new Ingredient("Water");
        pilsner.addIngredient(water);

        session.save(pilsner);
        assertThat(pilsner.getUuid()).isNotNull();
        assertThat(water.getId()).isNotNull();

        assertBeverageAndIngredientInDB(pilsner, water);

        session.clear();
        assertBeverageAndIngredientInDB(pilsner, water);
    }

    /**
     * Saving opposite direction than shouldSaveEntityWithoutAndWithGraphID
     */
    @Test
    public void shouldSaveEntitydWithGraphIdAndWIthoutId() throws Exception {
        Beverage pilsner = new Beverage("Pilsner Urquell");
        Ingredient water = new Ingredient("Water");
        pilsner.addIngredient(water);

        session.save(water);
        assertThat(water.getId()).isNotNull();
        assertThat(pilsner.getUuid()).isNotNull();

        assertBeverageAndIngredientInDB(pilsner, water);

        session.clear();
        assertBeverageAndIngredientInDB(pilsner, water);
    }

    @Test
    public void shouldSaveEntitiesWithoutGraphIds() throws Exception {
        Beverage pilsner = new Beverage("Pilsner Urquell");
        Manufacturer prazdroj = new Manufacturer("Plzeňský Prazdroj, a. s.");
        prazdroj.addBeverage(pilsner);

        session.save(pilsner);

        Beverage loaded = session.load(Beverage.class, pilsner.getUuid());
        assertThat(loaded.getManufacturer()).isSameAs(prazdroj);

        assertBeverageAndManufacturerInDB(pilsner, prazdroj);

        session.clear();
        assertBeverageAndManufacturerInDB(pilsner, prazdroj);
    }

    @Test
    public void shouldSaveAndLoadRelationshipEntityWithoutId() throws Exception {
        Manufacturer prazdroj = new Manufacturer("Plzeňský Prazdroj, a. s.");
        Manufacturer asahi = new Manufacturer("Asahi Breweries, Ltd.");
        asahi.acquired(2017, prazdroj);

        session.save(asahi);

        session.clear();

        Manufacturer loaded = session.load(Manufacturer.class, asahi.getUuid());
        assertThat(loaded.getName()).isEqualTo("Asahi Breweries, Ltd.");
        Owns acquisition = loaded.getAcquisitions().iterator().next();
        assertThat(acquisition.getOwnee().getName()).isEqualTo("Plzeňský Prazdroj, a. s.");
    }

    @Test
    public void shouldSaveAndLoadRelationshipEntityWithoutIdDirect() throws Exception {
        Manufacturer prazdroj = new Manufacturer("Plzeňský Prazdroj, a. s.");
        Manufacturer asahi = new Manufacturer("Asahi Breweries, Ltd.");
        asahi.acquired(2017, prazdroj);

        Owns owns = asahi.getAcquisitions().iterator().next();

        session.save(owns);
        session.clear();

        Manufacturer loaded = session.load(Manufacturer.class, asahi.getUuid());
        assertThat(loaded.getName()).isEqualTo("Asahi Breweries, Ltd.");
        Owns acquisition = loaded.getAcquisitions().iterator().next();
        assertThat(acquisition.getOwnee().getName()).isEqualTo("Plzeňský Prazdroj, a. s.");
    }

    @Test
    public void shouldUpdateRelationshipEntityTransitive() throws Exception {
        Manufacturer prazdroj = new Manufacturer("Plzeňský Prazdroj, a. s.");
        Manufacturer asahi = new Manufacturer("Asahi Breweries, Ltd.");
        asahi.acquired(1917, prazdroj);

        session.save(asahi);

        Owns owns = asahi.getAcquisitions().iterator().next();
        owns.setAcquiredYear(2017);

        session.save(asahi);

        session.clear();
        Manufacturer loaded = session.load(Manufacturer.class, asahi.getUuid());
        Owns acquisition = loaded.getAcquisitions().iterator().next();
        assertThat(acquisition.getAcquiredYear()).isEqualTo(2017);
    }

    @Test
    public void shouldUpdateRelationshipEntityDirect() throws Exception {
        Manufacturer prazdroj = new Manufacturer("Plzeňský Prazdroj, a. s.");
        Manufacturer asahi = new Manufacturer("Asahi Breweries, Ltd.");
        asahi.acquired(1917, prazdroj);

        Owns owns = asahi.getAcquisitions().iterator().next();

        session.save(owns);

        owns.setAcquiredYear(2017);

        session.save(owns);

        session.clear();
        Manufacturer loaded = session.load(Manufacturer.class, asahi.getUuid());
        Owns acquisition = loaded.getAcquisitions().iterator().next();
        assertThat(acquisition.getAcquiredYear()).isEqualTo(2017);
    }

    /**
     * Not sure this is valid use case - the object graph is not consistent
     * RE Owns refers to asahi node, but it does not refer to owns instance
     * Similar as {@link org.neo4j.ogm.persistence.examples.ingredients.IngredientsIntegrationTest#shouldBeAbleToLoadPairingWithCustomDepth}
     * Executes !compiler.context().visitedRelationshipEntity branch in
     * {@link org.neo4j.ogm.context.EntityGraphMapper#map(java.lang.Object, int)}
     */
    @Test
    public void shouldCreateRelationshipEntityDirect() throws Exception {
        Manufacturer prazdroj = new Manufacturer("Plzeňský Prazdroj, a. s.");
        Manufacturer asahi = new Manufacturer("Asahi Breweries, Ltd.");
        session.save(prazdroj);
        session.save(asahi);

        Owns owns = new Owns(asahi, prazdroj, 2017);
        session.save(owns);

        session.clear();
        Manufacturer loaded = session.load(Manufacturer.class, asahi.getUuid());
        Owns acquisition = loaded.getAcquisitions().iterator().next();
        assertThat(acquisition.getAcquiredYear()).isEqualTo(2017);
    }

    @Test
    public void shouldAddAndRemoveLabels() throws Exception {
        Manufacturer prazdroj = new Manufacturer("Plzeňský Prazdroj, a. s.");

        session.save(prazdroj);
        prazdroj.addLabel("BestBeer");

        session.save(prazdroj);

        session.clear();
        Manufacturer loaded = session.load(Manufacturer.class, prazdroj.getUuid());
        assertThat(loaded.getLabels()).containsOnly("BestBeer");

        prazdroj.setLabels(emptySet());
        session.save(prazdroj);

        loaded = session.load(Manufacturer.class, prazdroj.getUuid());
        assertThat(loaded.getLabels()).isEmpty();
    }

    private void assertBeverageAndManufacturerInDB(Beverage beverage, Manufacturer manufacturer) {
        Beverage loaded = session.load(Beverage.class, beverage.getUuid());
        assertThat(loaded.getName()).isEqualTo(beverage.getName());
        assertThat(loaded.getManufacturer().getName()).isEqualTo(manufacturer.getName());
    }

    /*
        session.clear should clear all (label id -> graph id
         */
    private void assertBeverageAndIngredientInDB(Beverage pilsner, Ingredient water) {
        Beverage loaded = session.load(Beverage.class, pilsner.getUuid());
        assertThat(loaded.getName()).isEqualTo("Pilsner Urquell");
        assertThat(loaded.getIngredients())
            .extracting(Ingredient::getName)
            .containsOnly(water.getName());
    }

    private void assertNoBeverage() {
        Collection<Beverage> beverages = session.loadAll(Beverage.class);
        assertThat(beverages).hasSize(0);
    }
}
