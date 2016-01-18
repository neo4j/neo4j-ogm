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
 * One undirected relationship field annotated. Methods not annotated.
 *
 * @author Luanne Misquitta
 */
public class UserV15 extends Entity {

    @Relationship(type = "KNOWS", direction = "UNDIRECTED")
    private UserV15 knows;

    public UserV15() {
    }

    public UserV15 getKnows() {
        return knows;
    }

    public void setKnows(UserV15 knows) {
        this.knows = knows;
    }
}
