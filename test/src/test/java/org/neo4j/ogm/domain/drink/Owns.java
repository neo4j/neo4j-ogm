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

package org.neo4j.ogm.domain.drink;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.id.UuidStrategy;

/**
 * @author Frantisek Hartman
 */
@RelationshipEntity(type = "OWNS")
public class Owns {

    @Id
    @GeneratedValue(strategy = UuidStrategy.class)
    private String acquisitionId;

    @StartNode
    private Manufacturer owner;

    @EndNode
    private Manufacturer ownee;

    private int acquiredYear;

    public Owns() {
    }

    public Owns(Manufacturer owner, Manufacturer ownee, int acquiredYear) {
        this.owner = owner;
        this.ownee = ownee;
        this.acquiredYear = acquiredYear;
    }

    public String getAcquisitionId() {
        return acquisitionId;
    }

    public Manufacturer getOwner() {
        return owner;
    }

    public Manufacturer getOwnee() {
        return ownee;
    }

    public int getAcquiredYear() {
        return acquiredYear;
    }

    public void setAcquiredYear(int acquiredYear) {
        this.acquiredYear = acquiredYear;
    }
}
