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
 * Two annotated outgoing fields, same relationship type
 *
 * @author Luanne Misquitta
 */
public class UserV21 {
    @Relationship(type = "KNOWS")
    UserV21 user;

    @Relationship(type = "KNOWS")
    PlainUser plainUser;

    public UserV21() {
    }

    public UserV21 getUser() {
        return user;
    }

    public PlainUser getPlainUser() {
        return plainUser;
    }
}
