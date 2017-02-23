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

package org.neo4j.ogm.domain.social;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.Relationship;

/**
 * POJO to test the direction of an incoming relationships.
 *
 * @author Luanne Misquitta
 */
public class Mortal {

    private Long id;
    private String name;

    @Relationship(type = "KNOWN_BY", direction = "INCOMING")
    private Set<Mortal> knownBy = new HashSet<>();

    public Mortal() {
    }

    public Mortal(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Mortal> getKnownBy() {
        return knownBy;
    }

    public void setKnownBy(Set<Mortal> knownBy) {
        this.knownBy = knownBy;
    }

    /*
     * this method is here to prove that only true JavaBean style setters/getters are invoked by the mapper
     * see ClassInfo.findSetters() and ClassInfo.findGetters()
     */
    public void addKnownBy(Mortal mortal) {
        knownBy.add(mortal);
    }
}
