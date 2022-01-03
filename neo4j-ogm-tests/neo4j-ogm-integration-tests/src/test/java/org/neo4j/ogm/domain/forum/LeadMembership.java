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
