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
 * Annotated incoming field with annotated getters and setters. Relationship type derived from property name
 *
 * @author Luanne Misquitta
 */
public class UserV1 extends Entity {

    @Relationship(type = "KNOWN_BY", direction = "INCOMING")
    private UserV1 knownBy;

    public UserV1() {
    }

    @Relationship(type = "KNOWN_BY", direction = "INCOMING")
    //We MUST annotate the getter if present and the relationship direction is incoming
    public UserV1 getKnownBy() {
        return knownBy;
    }

    @Relationship(type = "KNOWN_BY", direction = "INCOMING")
    //We MUST annotate the setter if present and the relationship direction is incoming
    public void setKnownBy(UserV1 knownBy) {
        this.knownBy = knownBy;
    }
}
