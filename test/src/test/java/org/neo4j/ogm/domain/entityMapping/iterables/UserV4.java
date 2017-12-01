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

package org.neo4j.ogm.domain.entityMapping.iterables;

import java.util.Set;

import org.neo4j.ogm.annotation.Relationship;

/**
 * Annotated incoming iterable field with annotated getters and setters. Relationship type different from property name
 *
 * @author Luanne Misquitta
 */
public class UserV4 extends Entity {

    @Relationship(type = "KNOWS", direction = "INCOMING")
    private Set<UserV4> friend;

    public Set<UserV4> getFriend() {
        return friend;
    }

    public void setFriend(Set<UserV4> friend) {
        this.friend = friend;
    }
}
