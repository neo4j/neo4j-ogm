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

package org.neo4j.ogm.domain.entityMapping;

import org.neo4j.ogm.annotation.Relationship;

/**
 * Annotated field (implied outgoing), non annotated getter and setter, relationship type different from property name
 *
 * @author Luanne Misquitta
 */
public class UserV3 extends Entity {

    @Relationship(type = "KNOWS")
    private UserV3 friend;

    public UserV3() {
    }

    public UserV3 getFriend() {
        return friend;
    }

    public void setFriend(UserV3 friend) {
        this.friend = friend;
    }
}
