/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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
package org.neo4j.ogm.domain.postload;

import java.util.UUID;

import org.neo4j.ogm.annotation.PostLoad;

/**
 * @author Michael J. Simons
 */
public class UserWithBetterPostLoadMethod {
    private Long id;

    private transient String randomName;

    public UserWithBetterPostLoadMethod() {
        this.randomName = "n/a";
    }

    public Long getId() {
        return id;
    }

    public String getRandomName() {
        return randomName;
    }

    @PostLoad final void postLoad() {
        this.randomName = UUID.randomUUID().toString();
    }
}
