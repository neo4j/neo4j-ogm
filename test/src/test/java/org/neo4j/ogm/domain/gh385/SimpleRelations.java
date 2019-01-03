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
package org.neo4j.ogm.domain.gh385;

import java.util.Collection;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Michael J. Simons
 */
public class SimpleRelations {

    @NodeEntity("P")
    public static class P {
        @Id @GeneratedValue
        private Long id;

        private String name;

        @Relationship("HAS")
        private Collection<C> c;

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Collection<C> getC() {
            return c;
        }
    }

    @NodeEntity("C")
    public static class C {
        @Id @GeneratedValue
        private Long id;

        private String name;
    }

    private SimpleRelations() {
    }
}
