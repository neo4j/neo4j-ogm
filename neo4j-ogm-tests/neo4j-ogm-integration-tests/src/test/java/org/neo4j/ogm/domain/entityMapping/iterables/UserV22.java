/*
 * Copyright (c) 2002-2020 "Neo4j,"
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
package org.neo4j.ogm.domain.entityMapping.iterables;

import java.util.List;

import org.neo4j.ogm.annotation.Relationship;

/**
 * Two iterables, same relationship type and direction, outgoing, methods annotated
 *
 * @author Luanne Misquitta
 */
public class UserV22 {

    @Relationship(type = "KNOWS")
    public List<PlainUser> plainUsers;

    @Relationship(type = "KNOWS")
    public UserV22 user;

    public UserV22() {
    }

    public List<PlainUser> getPlainUsers() {
        return plainUsers;
    }

    public void setPlainUsers(List<PlainUser> plainUsers) {
        this.plainUsers = plainUsers;
    }

    public UserV22 getUser() {
        return user;
    }

    public void setUser(UserV22 user) {
        this.user = user;
    }
}
