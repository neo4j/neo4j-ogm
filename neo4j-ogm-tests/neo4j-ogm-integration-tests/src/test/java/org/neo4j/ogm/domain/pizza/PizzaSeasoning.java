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

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * @author Luanne Misquitta
 */
@RelationshipEntity(type = "HAS")
public class PizzaSeasoning {

    Long id;
    @StartNode
    Pizza pizza;
    @EndNode
    Seasoning seasoning;
    Quantity quantity;

    public PizzaSeasoning() {
    }

    public PizzaSeasoning(Pizza pizza, Seasoning seasoning, Quantity quantity) {
        this.pizza = pizza;
        this.seasoning = seasoning;
        this.quantity = quantity;
        this.pizza.getSeasonings().add(this);
        this.seasoning.getPizzas().add(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pizza getPizza() {
        return pizza;
    }

    public void setPizza(Pizza pizza) {
        this.pizza = pizza;
    }

    public Seasoning getSeasoning() {
        return seasoning;
    }

    public void setSeasoning(Seasoning seasoning) {
        this.seasoning = seasoning;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public void setQuantity(Quantity quantity) {
        this.quantity = quantity;
    }
}
