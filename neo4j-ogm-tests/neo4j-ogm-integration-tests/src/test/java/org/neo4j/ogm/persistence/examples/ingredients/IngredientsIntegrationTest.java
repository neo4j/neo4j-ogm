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
package org.neo4j.ogm.persistence.examples.ingredients;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.domain.ingredients.Ingredient;
import org.neo4j.ogm.domain.ingredients.Pairing;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Luanne Misquitta
 */
public class IngredientsIntegrationTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.ingredients");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    /**
     * @see DATAGRAPH-639
     */
    @Test
    public void shouldBeAbleToAddInterrelatedPairings() {

        Ingredient chicken = new Ingredient("Chicken");
        session.save(chicken);

        Ingredient carrot = new Ingredient("Carrot");
        session.save(carrot);

        Ingredient butter = new Ingredient("Butter");
        session.save(butter);

        Pairing pairing = new Pairing();
        pairing.setFirst(chicken);
        pairing.setSecond(carrot);
        pairing.setAffinity("EXCELLENT");
        carrot.addPairing(pairing);
        session.save(chicken);

        Pairing pairing2 = new Pairing();
        pairing2.setFirst(chicken);
        pairing2.setSecond(butter);
        pairing2.setAffinity("EXCELLENT");
        carrot.addPairing(pairing2);
        session.save(chicken);

        Pairing pairing3 = new Pairing();
        pairing3.setFirst(carrot);
        pairing3.setSecond(butter);
        pairing3.setAffinity("EXCELLENT");
        carrot.addPairing(pairing3);
        session.save(carrot); //NullPointerException
    }

    @Test
    public void shouldBeAbleToLoadIngredientsWithoutPairings() {

        Ingredient chicken = new Ingredient("Chicken");
        session.save(chicken);

        Ingredient carrot = new Ingredient("Carrot");
        session.save(carrot);

        Ingredient butter = new Ingredient("Butter");
        session.save(butter);

        Pairing pairing = new Pairing();
        pairing.setFirst(chicken);
        pairing.setSecond(carrot);
        pairing.setAffinity("EXCELLENT");
        carrot.addPairing(pairing);
        session.save(chicken);

        Pairing pairing2 = new Pairing();
        pairing2.setFirst(chicken);
        pairing2.setSecond(butter);
        pairing2.setAffinity("EXCELLENT");
        carrot.addPairing(pairing2);
        session.save(chicken);

        Pairing pairing3 = new Pairing();
        pairing3.setFirst(carrot);
        pairing3.setSecond(butter);
        pairing3.setAffinity("EXCELLENT");
        carrot.addPairing(pairing3);
        session.save(carrot); //NullPointerException

        // it is important to clear the session if you intend the depth of the
        // the objects you want returned to not be the same as the depth currently
        // held in the mapping context.
        session.clear();

        Iterator<Ingredient> it = session.loadAll(Ingredient.class, 0).iterator();

        while (it.hasNext()) {
            Ingredient i = it.next();
            assertThat(i.getPairings()).as(i.getName()).hasSize(0);
        }
    }

    /**
     * @see Issue #94
     */
    @Test
    public void shouldBeAbleToLoadPairingWithCustomDepth() {

        Ingredient chicken = new Ingredient("Chicken");
        session.save(chicken);

        Ingredient carrot = new Ingredient("Carrot");
        session.save(carrot);

        Ingredient butter = new Ingredient("Butter");
        session.save(butter);

        Ingredient pineapple = new Ingredient("Pineapple");
        session.save(pineapple);

        Ingredient ham = new Ingredient("Ham");
        session.save(ham);

        Ingredient sage = new Ingredient("Sage");
        session.save(sage);

        Pairing pairing = new Pairing();
        pairing.setFirst(chicken);
        pairing.setSecond(carrot);
        pairing.setAffinity("EXCELLENT");
        session.save(pairing);

        Pairing pairing2 = new Pairing();
        pairing2.setFirst(chicken);
        pairing2.setSecond(pineapple);
        pairing2.setAffinity("GOOD");
        session.save(pairing2);

        Pairing pairing3 = new Pairing();
        pairing3.setFirst(pineapple);
        pairing3.setSecond(ham);
        pairing3.setAffinity("TRIED AND TESTED");
        session.save(pairing3);

        Pairing pairing4 = new Pairing();
        pairing4.setFirst(carrot);
        pairing4.setSecond(butter);
        pairing4.setAffinity("GOOD");
        session.save(pairing4);

        Pairing pairing5 = new Pairing();
        pairing5.setFirst(butter);
        pairing5.setSecond(sage);
        pairing5.setAffinity("EXCELLENT");
        session.save(pairing5);

        session.clear();

        //Load pairing (carrot-chicken) to default depth 1. Carrot and Chicken should be loaded to depth 1
        Pairing carrotChicken = session.load(Pairing.class, pairing.getId());
        assertThat(carrotChicken).isNotNull();
        assertThat(carrotChicken.getAffinity()).isEqualTo("EXCELLENT");
        assertThat(carrotChicken.getFirst()).isNotNull();
        assertThat(carrotChicken.getFirst().getPairings()).hasSize(2);
        assertThat(carrotChicken.getSecond()).isNotNull();
        assertThat(carrotChicken.getSecond().getPairings()).hasSize(2);

        Ingredient loadedChicken;
        if (carrotChicken.getFirst().getName().equals("Chicken")) {
            loadedChicken = carrotChicken.getFirst();
        } else {
            loadedChicken = carrotChicken.getSecond();
        }

        Ingredient loadedPineapple = null;
        for (Pairing p : loadedChicken.getPairings()) {
            if (p.getFirst().getName().equals("Pineapple")) {
                loadedPineapple = p.getFirst();
            }
            if (p.getSecond().getName().equals("Pineapple")) {
                loadedPineapple = p.getSecond();
            }
        }
        assertThat(loadedPineapple).isNotNull();
        assertThat(loadedPineapple.getPairings()).hasSize(1);

        Ingredient loadedCarrot;
        if (carrotChicken.getFirst().getName().equals("Carrot")) {
            loadedCarrot = carrotChicken.getFirst();
        } else {
            loadedCarrot = carrotChicken.getSecond();
        }

        Ingredient loadedButter = null;
        for (Pairing p : loadedCarrot.getPairings()) {
            if (p.getFirst().getName().equals("Butter")) {
                loadedButter = p.getFirst();
            }
            if (p.getSecond().getName().equals("Butter")) {
                loadedButter = p.getSecond();
            }
        }
        assertThat(loadedButter).isNotNull();
        assertThat(loadedButter.getPairings()).hasSize(1);

        session.clear();

        //Load pairing (carrot-chicken) to depth 2. Carrot and chicken should be loaded to depth 2
        carrotChicken = session.load(Pairing.class, pairing.getId(), 2);
        assertThat(carrotChicken).isNotNull();
        assertThat(carrotChicken.getAffinity()).isEqualTo("EXCELLENT");
        assertThat(carrotChicken.getFirst()).isNotNull();
        assertThat(carrotChicken.getFirst().getPairings()).hasSize(2);
        assertThat(carrotChicken.getSecond()).isNotNull();
        assertThat(carrotChicken.getSecond().getPairings()).hasSize(2);
        if (carrotChicken.getFirst().getName().equals("Chicken")) {
            loadedChicken = carrotChicken.getFirst();
        } else {
            loadedChicken = carrotChicken.getSecond();
        }

        loadedPineapple = null;
        for (Pairing p : loadedChicken.getPairings()) {
            if (p.getFirst().getName().equals("Pineapple")) {
                loadedPineapple = p.getFirst();
            }
            if (p.getSecond().getName().equals("Pineapple")) {
                loadedPineapple = p.getSecond();
            }
        }
        assertThat(loadedPineapple).isNotNull();
        assertThat(loadedPineapple.getPairings()).hasSize(2);

        if (carrotChicken.getFirst().getName().equals("Carrot")) {
            loadedCarrot = carrotChicken.getFirst();
        } else {
            loadedCarrot = carrotChicken.getSecond();
        }

        loadedButter = null;
        for (Pairing p : loadedCarrot.getPairings()) {
            if (p.getFirst().getName().equals("Butter")) {
                loadedButter = p.getFirst();
            }
            if (p.getSecond().getName().equals("Butter")) {
                loadedButter = p.getSecond();
            }
        }
        assertThat(loadedButter).isNotNull();
        assertThat(loadedButter.getPairings()).hasSize(2);
    }

    /**
     * @see Issue 97
     */
    @Test
    public void shouldBeAbleToLoadIngredientsWithPagingAndDepth() {

        Ingredient chicken = new Ingredient("Chicken");
        session.save(chicken);

        Ingredient carrot = new Ingredient("Carrot");
        session.save(carrot);

        Ingredient butter = new Ingredient("Butter");
        session.save(butter);

        Pairing pairing = new Pairing();
        pairing.setFirst(chicken);
        pairing.setSecond(carrot);
        pairing.setAffinity("EXCELLENT");
        carrot.addPairing(pairing);
        session.save(chicken);

        Pairing pairing2 = new Pairing();
        pairing2.setFirst(chicken);
        pairing2.setSecond(butter);
        pairing2.setAffinity("EXCELLENT");
        carrot.addPairing(pairing2);
        session.save(chicken);

        Pairing pairing3 = new Pairing();
        pairing3.setFirst(carrot);
        pairing3.setSecond(butter);
        pairing3.setAffinity("EXCELLENT");
        carrot.addPairing(pairing3);
        session.save(carrot);

        session.clear();

        Collection<Ingredient> ingredients = session.loadAll(Ingredient.class, new Pagination(0, 1));
        assertThat(ingredients).hasSize(1);

        session.clear();

        ingredients = session.loadAll(Ingredient.class, new Pagination(1, 1));
        assertThat(ingredients).hasSize(1);

        session.clear();

        ingredients = session.loadAll(Ingredient.class, new Pagination(0, 2));
        assertThat(ingredients).hasSize(2);

        ingredients = session.loadAll(Ingredient.class, new Pagination(0, 3));
        assertThat(ingredients).hasSize(3);

        session.clear();

        Collection<Pairing> pairings = session.loadAll(Pairing.class, new Pagination(0, 1));
        assertThat(pairings).hasSize(1);

        session.clear();

        pairings = session.loadAll(Pairing.class, new Pagination(1, 1));
        assertThat(pairings).hasSize(1);

        session.clear();

        pairings = session.loadAll(Pairing.class, new Pagination(0, 2));
        assertThat(pairings).hasSize(2);

        session.clear();

        pairings = session.loadAll(Pairing.class, new Pagination(0, 3));
        assertThat(pairings).hasSize(3);
    }

    /**
     * @see Issue 97
     */
    @Test
    public void shouldBeAbleToLoadIngredientsWithFiltersPagingAndDepth() {

        Ingredient chicken = new Ingredient("Chicken");
        session.save(chicken);

        Ingredient carrot = new Ingredient("Chicken");
        session.save(carrot);

        Ingredient butter = new Ingredient("Chicken");
        session.save(butter);

        Pairing pairing = new Pairing();
        pairing.setFirst(chicken);
        pairing.setSecond(carrot);
        pairing.setAffinity("EXCELLENT");
        carrot.addPairing(pairing);
        session.save(chicken);

        Pairing pairing2 = new Pairing();
        pairing2.setFirst(chicken);
        pairing2.setSecond(butter);
        pairing2.setAffinity("EXCELLENT");
        carrot.addPairing(pairing2);
        session.save(chicken);

        Pairing pairing3 = new Pairing();
        pairing3.setFirst(carrot);
        pairing3.setSecond(butter);
        pairing3.setAffinity("EXCELLENT");
        carrot.addPairing(pairing3);
        session.save(carrot);

        session.clear();

        Collection<Ingredient> ingredients = session
            .loadAll(Ingredient.class, new Filter("name", ComparisonOperator.EQUALS, "Chicken"), new Pagination(0, 1));
        assertThat(ingredients).hasSize(1);

        session.clear();

        ingredients = session
            .loadAll(Ingredient.class, new Filter("name", ComparisonOperator.EQUALS, "Chicken"), new Pagination(1, 1));
        assertThat(ingredients).hasSize(1);

        session.clear();

        ingredients = session
            .loadAll(Ingredient.class, new Filter("name", ComparisonOperator.EQUALS, "Chicken"), new Pagination(0, 2));
        assertThat(ingredients).hasSize(2);

        ingredients = session
            .loadAll(Ingredient.class, new Filter("name", ComparisonOperator.EQUALS, "Chicken"), new Pagination(0, 3));
        assertThat(ingredients).hasSize(3);

        session.clear();

        Collection<Pairing> pairings = session
            .loadAll(Pairing.class, new Filter("affinity", ComparisonOperator.EQUALS, "EXCELLENT"),
                new Pagination(0, 1));
        assertThat(pairings).hasSize(1);

        session.clear();

        pairings = session.loadAll(Pairing.class, new Filter("affinity", ComparisonOperator.EQUALS, "EXCELLENT"),
            new Pagination(1, 1));
        assertThat(pairings).hasSize(1);

        session.clear();

        pairings = session.loadAll(Pairing.class, new Filter("affinity", ComparisonOperator.EQUALS, "EXCELLENT"),
            new Pagination(0, 2));
        assertThat(pairings).hasSize(2);

        pairings = session.loadAll(Pairing.class, new Filter("affinity", ComparisonOperator.EQUALS, "EXCELLENT"),
            new Pagination(0, 3));
        assertThat(pairings).hasSize(3);
    }
}
