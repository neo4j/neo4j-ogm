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

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Labels;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.id.UuidStrategy;

/**
 * @author Frantisek Hartman
 */
@NodeEntity
public class Manufacturer {

    @Id
    @GeneratedValue(strategy = UuidStrategy.class)
    private String uuid;

    private String name;

    @Relationship(type = "MAKES")
    private Set<Beverage> beverages;

    @Relationship(type = "OWNS")
    private Set<Owns> acquisitions;

    @Labels
    private Set<String> labels;

    public Manufacturer() {
    }

    public Manufacturer(String name) {
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

    public Set<Beverage> getBeverages() {
        return beverages;
    }

    public void addBeverage(Beverage beverage) {
        if (beverages == null) {
            beverages = new HashSet<>();
        }
        beverages.add(beverage);
        beverage.setManufacturer(this);
    }

    public Set<Owns> getAcquisitions() {
        return acquisitions;
    }

    public void acquired(int year, Manufacturer manufacturer) {
        if (acquisitions == null) {
            acquisitions = new HashSet<>();
        }
        acquisitions.add(new Owns(this, manufacturer, year));
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }

    public void addLabel(String label) {
        if (labels == null) {
            labels = new HashSet<>();
        }
        labels.add(label);
    }
}
