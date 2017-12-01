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

package org.neo4j.ogm.domain.travel;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.id.UuidStrategy;

/**
 * @author Frantisek Hartman
 */
@RelationshipEntity(type = "VISITED")
public class Visit {

    private Long id;

    @Id
    @GeneratedValue(strategy = UuidStrategy.class)
    private String identifier;

    @StartNode
    private Person person;

    @EndNode
    private Place place;

    private String reasonForVisit;

    public Visit() {
    }

    public Visit(Person person, Place place, String reasonForVisit) {
        this.person = person;
        this.place = place;
        this.reasonForVisit = reasonForVisit;
    }

    public Long getId() {
        return id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Person getPerson() {
        return person;
    }

    public Place getPlace() {
        return place;
    }

    public String getReasonForVisit() {
        return reasonForVisit;
    }
}
