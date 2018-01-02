/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

package org.neo4j.ogm.domain.drink;

import static org.neo4j.ogm.annotation.Relationship.*;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.id.UuidStrategy;

/**
 * Beverage is an entity which has only @Id
 */
@NodeEntity
public class Beverage {

    @Id
    @GeneratedValue(strategy = UuidStrategy.class)
    private String uuid;

    private String name;

    @Relationship(type = "CONTAINS")
    private Set<Ingredient> ingredients;

    @Relationship(type = "MAKES", direction = INCOMING)
    private Manufacturer manufacturer;

    public Beverage() {
    }

    public Beverage(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Ingredient> getIngredients() {
        return ingredients;
    }

    public void addIngredient(Ingredient ingredient) {
        if (ingredients == null) {
            ingredients = new HashSet<>();
        }
        ingredients.add(ingredient);

        ingredient.addBeverage(this);
    }

    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
    }

    @Override
    public String toString() {
        return "Beverage{" +
            "uuid='" + uuid + '\'' +
            ", name='" + name + '\'' +
            '}';
    }
}
