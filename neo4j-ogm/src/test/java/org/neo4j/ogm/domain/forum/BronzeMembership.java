/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.domain.forum;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity(label ="Bronze")
public class BronzeMembership extends Membership {

    @Override
    public boolean getCanPost() {
        return false;
    }

    @Override
    public boolean getCanComment() {
        return true;
    }

    @Override
    public boolean getCanFollow() {
        return false;
    }

    @Override
    public IMembership[] getUpgrades() {
        return new IMembership[] { new SilverMembership(), new GoldMembership()};
    }
}
