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

package org.neo4j.ogm.domain.knowledge;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vince
 */
@NodeEntity
public class Person extends Entity {

    @Relationship(type = "KNOWS_PERSON")
    List<Knows> knownPersons = new ArrayList<>();

    @Relationship(type = "KNOWS_LANGUAGE")
    List<Knows> knownLanguages = new ArrayList<>();

    public Person() {}

    public Person(String name) {
        this.name = name;
    }

    public void knows(Person person) {

        Knows knows = new Knows();
        knows.knower = this;
        knows.knowee = person;

        knownPersons.add(knows);
    }

    public void knows(Language language) {

        Knows knows = new Knows();
        knows.knower = this;
        knows.knowee = language;

        knownLanguages.add(knows);
    }
}
