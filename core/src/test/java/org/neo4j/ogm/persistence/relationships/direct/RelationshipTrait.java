/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.persistence.relationships.direct;


import org.neo4j.ogm.testutil.MultiDriverTestClass;

import static org.junit.Assert.fail;

/**
 * @author Vince Bickers
 */
public class RelationshipTrait extends MultiDriverTestClass {

    public void assertSameArray(Object[] as, Object[] bs) {

        if (as == null || bs == null) fail("null arrays not allowed");
        if (as.length != bs.length) fail("arrays are not same length");


        for (Object a : as) {
            boolean found = false;
            for (Object b : bs) {
                if (b.toString().equals(a.toString())) {
                    found = true;
                    break;
                }
            }
            if (!found) fail("array contents are not the same: " + as + ", " + bs);
        }
    }

}
