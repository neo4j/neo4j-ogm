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
package org.neo4j.ogm.domain.pizza;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.neo4j.ogm.annotation.Labels;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * An entity that contains multiple relationships and RE's with the same relationship type and direction.
 *
 * @author Luanne Misquitta
 * @author Jonathan D'Orleans
 * @author Michael J. Simons
 */
@NodeEntity(label = "Pizza")
public class Pizza {

    private Long id;
    private String name;

    @Relationship(type = "FOR", direction = Relationship.Direction.INCOMING)
    private Crust crust;

    @Relationship(type = "FOR", direction = Relationship.Direction.INCOMING)
    private List<Topping> toppings;

    @Relationship(type = "FOR", direction = Relationship.Direction.INCOMING)
    private PizzaSauce pizzaSauce;

    @Relationship(type = "HAS", direction = Relationship.Direction.OUTGOING)
    private Set<PizzaSeasoning> seasonings = new HashSet<>();

    @Relationship(type = "HAS", direction = Relationship.Direction.OUTGOING)
    private Set<PizzaCheese> cheeses = new HashSet<>();

    @Labels
    private List<String> labels = new ArrayList<>();

    public Pizza() {
    }

    public Pizza(String name) {
        this.name = name;
    }

    public Pizza(String name, Crust crust, List<Topping> toppings) {
        this.name = name;
        this.crust = crust;
        this.toppings = toppings;
    }

    public void addLabel(Object... additionalLabels) {
        for (Object label : additionalLabels) {
            this.labels.add(label.toString());
        }
    }

    public void removeLabel(Object... labelsToRemove) {
        for (Object label : labelsToRemove) {
            this.labels.remove(label.toString());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Crust getCrust() {
        return crust;
    }

    public void setCrust(Crust crust) {
        this.crust = crust;
    }

    public List<Topping> getToppings() {
        return toppings;
    }

    public void setToppings(List<Topping> toppings) {
        this.toppings = toppings;
    }

    public PizzaSauce getPizzaSauce() {
        return pizzaSauce;
    }

    public void setPizzaSauce(PizzaSauce pizzaSauce) {
        this.pizzaSauce = pizzaSauce;
        this.pizzaSauce.setPizza(this);
    }

    public Set<PizzaSeasoning> getSeasonings() {
        return seasonings;
    }

    public void setSeasonings(Set<PizzaSeasoning> seasonings) {
        this.seasonings = seasonings;
    }

    public Set<PizzaCheese> getCheeses() {
        return cheeses;
    }

    public void setCheeses(Set<PizzaCheese> cheeses) {
        this.cheeses = cheeses;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }
}
