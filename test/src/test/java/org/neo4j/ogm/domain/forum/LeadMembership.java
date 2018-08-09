/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

package org.neo4j.ogm.domain.forum;

import java.time.Year;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

/**
 * @author Michael J. Simons
 */
@NodeEntity("Lead")
public class LeadMembership extends Membership {
    @Property
    private Year yearOfRegistration;

    public Year getYearOfRegistration() {
        return yearOfRegistration;
    }

    public void setYearOfRegistration(Year yearOfRegistration) {
        this.yearOfRegistration = yearOfRegistration;
    }

    @Override
    public boolean getCanPost() {
        return false;
    }

    @Override
    public boolean getCanComment() {
        return false;
    }

    @Override
    public boolean getCanFollow() {
        return false;
    }

    @Override
    public IMembership[] getUpgrades() {
        return new IMembership[0];
    }
}
