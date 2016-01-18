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
 * Annotated outgoing field, non annotated getter and setter, relationship name same as property name
 *
 * @author Luanne Misquitta
 */
public class UserV2 extends Entity {

    @Relationship(type = "KNOWS", direction = "OUTGOING")
    private UserV2 knows;

    public UserV2() {
    }

    public UserV2 getKnows() {
        return knows;
    }

    public void setKnows(UserV2 knows) {
        this.knows = knows;
    }
}
