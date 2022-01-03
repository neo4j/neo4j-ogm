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
package org.neo4j.ogm.domain.drink;

import static org.neo4j.ogm.annotation.Relationship.*;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * Ingredient is an entity which has only graph id
 */
@NodeEntity
public class Ingredient {

    private Long id;

    private String name;

    @Relationship(type = "CONTAINS", direction = Direction.INCOMING)
    private Set<Beverage> beverages;

    public Ingredient() {
    }

    public Ingredient(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Beverage> getBeverages() {
        return beverages;
    }

    /**
     * Only adds ingredient -> beverage, use {@link org.neo4j.ogm.domain.drink.Beverage#addIngredient(Ingredient)}
     * to add both sides
     */
    public void addBeverage(Beverage beverage) {
        if (beverages == null) {
            beverages = new HashSet<>();
        }
        beverages.add(beverage);
    }
}
