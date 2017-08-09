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

package org.neo4j.ogm.domain.postload;

import org.neo4j.ogm.annotation.PostLoad;

/**
 * @author Frantisek Hartman
 */
public class User {

    static int postLoadCount = 0;

    Long id;

    public static int getPostLoadCount() {
        return postLoadCount;
    }

    public static void resetPostLoadCount() {
        postLoadCount = 0;
    }

    public Long getId() {
        return id;
    }

    @PostLoad
    public void postLoad() {
        postLoadCount++;
    }

}
