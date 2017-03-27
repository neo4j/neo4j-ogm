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

package org.neo4j.ogm.persistence.examples.ingredients;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.domain.ingredients.Ingredient;
import org.neo4j.ogm.domain.ingredients.Pairing;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Luanne Misquitta
 */
public class IngredientsIntegrationTest extends MultiDriverTestClass {

    private Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory(baseConfiguration.build(), "org.neo4j.ogm.domain.ingredients").openSession();
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
            assertEquals(i.getName(), 0, i.getPairings().size());
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
        assertNotNull(carrotChicken);
        assertEquals("EXCELLENT", carrotChicken.getAffinity());
        assertNotNull(carrotChicken.getFirst());
        assertEquals(2, carrotChicken.getFirst().getPairings().size());
        assertNotNull(carrotChicken.getSecond());
        assertEquals(2, carrotChicken.getSecond().getPairings().size());

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
        assertNotNull(loadedPineapple);
        assertEquals(1, loadedPineapple.getPairings().size());

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
        assertNotNull(loadedButter);
        assertEquals(1, loadedButter.getPairings().size());

        session.clear();

        //Load pairing (carrot-chicken) to depth 2. Carrot and chicken should be loaded to depth 2
        carrotChicken = session.load(Pairing.class, pairing.getId(), 2);
        assertNotNull(carrotChicken);
        assertEquals("EXCELLENT", carrotChicken.getAffinity());
        assertNotNull(carrotChicken.getFirst());
        assertEquals(2, carrotChicken.getFirst().getPairings().size());
        assertNotNull(carrotChicken.getSecond());
        assertEquals(2, carrotChicken.getSecond().getPairings().size());
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
        assertNotNull(loadedPineapple);
        assertEquals(2, loadedPineapple.getPairings().size());

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
        assertNotNull(loadedButter);
        assertEquals(2, loadedButter.getPairings().size());
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
        assertEquals(1, ingredients.size());

        session.clear();

        ingredients = session.loadAll(Ingredient.class, new Pagination(1, 1));
        assertEquals(1, ingredients.size());

        session.clear();

        ingredients = session.loadAll(Ingredient.class, new Pagination(0, 2));
        assertEquals(2, ingredients.size());

        ingredients = session.loadAll(Ingredient.class, new Pagination(0, 3));
        assertEquals(3, ingredients.size());

        session.clear();

        Collection<Pairing> pairings = session.loadAll(Pairing.class, new Pagination(0, 1));
        assertEquals(1, pairings.size());

        session.clear();

        pairings = session.loadAll(Pairing.class, new Pagination(1, 1));
        assertEquals(1, pairings.size());

        session.clear();

        pairings = session.loadAll(Pairing.class, new Pagination(0, 2));
        assertEquals(2, pairings.size());

        pairings = session.loadAll(Pairing.class, new Pagination(0, 3));
        assertEquals(3, pairings.size());
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

        Collection<Ingredient> ingredients = session.loadAll(Ingredient.class, new Filter("name", ComparisonOperator.EQUALS, "Chicken"), new Pagination(0, 1));
        assertEquals(1, ingredients.size());

        session.clear();

        ingredients = session.loadAll(Ingredient.class, new Filter("name", ComparisonOperator.EQUALS, "Chicken"), new Pagination(1, 1));
        assertEquals(1, ingredients.size());

        session.clear();

        ingredients = session.loadAll(Ingredient.class, new Filter("name", ComparisonOperator.EQUALS, "Chicken"), new Pagination(0, 2));
        assertEquals(2, ingredients.size());

        ingredients = session.loadAll(Ingredient.class, new Filter("name", ComparisonOperator.EQUALS, "Chicken"), new Pagination(0, 3));
        assertEquals(3, ingredients.size());

        session.clear();

        Collection<Pairing> pairings = session.loadAll(Pairing.class, new Filter("affinity", ComparisonOperator.EQUALS, "EXCELLENT"), new Pagination(0, 1));
        assertEquals(1, pairings.size());

        session.clear();

        pairings = session.loadAll(Pairing.class, new Filter("affinity", ComparisonOperator.EQUALS, "EXCELLENT"), new Pagination(1, 1));
        assertEquals(1, pairings.size());

        session.clear();

        pairings = session.loadAll(Pairing.class, new Filter("affinity", ComparisonOperator.EQUALS, "EXCELLENT"), new Pagination(0, 2));
        assertEquals(2, pairings.size());

        pairings = session.loadAll(Pairing.class, new Filter("affinity", ComparisonOperator.EQUALS, "EXCELLENT"), new Pagination(0, 3));
        assertEquals(3, pairings.size());
    }
}
