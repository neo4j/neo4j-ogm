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

import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Luanne Misquitta
 */
public class Seasoning {

    private Long id;
    private String name;

    @Relationship(type = "HAS", direction = "INCOMING")
    private Set<PizzaSeasoning> pizzas = new HashSet();

    public Seasoning() {
    }

    public Seasoning(String name) {
        this.name = name;
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

    public Set<PizzaSeasoning> getPizzas() {
        return pizzas;
    }

    public void setPizzas(Set<PizzaSeasoning> pizzas) {
        this.pizzas = pizzas;
    }
}
