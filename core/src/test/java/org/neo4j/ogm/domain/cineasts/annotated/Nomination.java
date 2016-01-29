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

package org.neo4j.ogm.domain.cineasts.annotated;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * @author Michal Bachman
 */
@RelationshipEntity(type = "NOMINATION")
public class Nomination {

    Long id;
    @EndNode
    Movie movie;
    @StartNode
    Actor actor;
    String name;
    int year;

    public Nomination() {
    }

    public Nomination(Movie movie, Actor actor, String name, int year) {
        this.movie = movie;
        this.actor = actor;
        this.name = name;
        this.year = year;
    }
}
