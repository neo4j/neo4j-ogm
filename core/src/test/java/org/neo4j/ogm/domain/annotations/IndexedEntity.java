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

package org.neo4j.ogm.domain.annotations;

import java.lang.annotation.*;

/**
 * @author vince
 */
public class IndexedEntity {

    Long id;

    @Indexed(b = 'a', c = 1, d = 0.01d, f = 0.02f, i = 5, j = 6, s = 3, t = "t", z = true)
    String ref;

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    @Inherited
    public @interface Indexed {
        byte b();
        char c();
        double d();
        float f();
        int i();
        long j();
        short s();
        String t();
        boolean z();

    }

}


