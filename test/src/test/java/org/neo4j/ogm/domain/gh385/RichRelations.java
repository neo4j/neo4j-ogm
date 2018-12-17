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
import java.util.List;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * @author Michael J. Simons
 */
public class RichRelations {

    @NodeEntity("S")
    public static class S {
        @Id @GeneratedValue
        private Long id;

        private String name;

        @Relationship("R")
        Collection<R> r;

        public S() {
        }

        public S(String name) {
            this.name = name;
        }

        public Collection<R> getR() {
            return r;
        }

        public void setR(List<R> r) {
            this.r = r;
        }

        public Long getId() {
            return id;
        }
    }

    @RelationshipEntity("R")
    public static class R {
        @Id @GeneratedValue
        private Long id;

        @StartNode
        private S s;

        @EndNode
        private E e;

        private String name;

        public R() {
        }

        public R(S s, E e, String name) {
            this.s = s;
            this.e = e;
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public E getE() {
            return e;
        }
    }

    @NodeEntity("E")
    public static class E {
        @Id @GeneratedValue
        private Long id;

        private String name;

        public E() {
        }

        public E(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private RichRelations() {
    }
}
