/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

package org.neo4j.ogm.domain.pizza;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Labels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An entity that contains multiple relationships and RE's with the same relationship type and direction.
 *
 * @author Luanne Misquitta
 */
@NodeEntity(label = "Pizza")
public class Pizza {

    private Long id;
    private String name;

    @Relationship(type = "FOR", direction = "INCOMING")
    private Crust crust;

    @Relationship(type = "FOR", direction = "INCOMING")
    private List<Topping> toppings;

    @Relationship(type = "FOR", direction = "INCOMING")
    private PizzaSauce pizzaSauce;

    @Relationship(type = "HAS", direction = "OUTGOING")
    private Set<PizzaSeasoning> seasonings = new HashSet<>();

    @Relationship(type = "HAS", direction = "OUTGOING")
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

    public List<String> getLabels() { return labels; }

    public void setLabels(List<String> labels) { this.labels = labels;}

}
