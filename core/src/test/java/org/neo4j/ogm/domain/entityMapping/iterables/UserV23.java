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

package org.neo4j.ogm.domain.entityMapping.iterables;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.domain.entityMapping.PlainUser;

import java.util.List;

/**
 * One iterable and one scalar, same relationship types, incoming, fields and methods annotated
 *
 * @author Luanne Misquitta
 */
public class UserV23 {

    @Relationship(type = "KNOWS", direction = "INCOMING")
    public PlainUser plainUsers;

    @Relationship(type = "KNOWS", direction = "INCOMING")
    public List<UserV23> user;

    public UserV23() {
    }

    public PlainUser getPlainUsers() {
        return plainUsers;
    }

    public void setPlainUsers(PlainUser plainUsers) {
        this.plainUsers = plainUsers;
    }

    public List<UserV23> getUser() {
        return user;
    }

    public void setUser(List<UserV23> user) {
        this.user = user;
    }
}
