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
package org.neo4j.ogm.domain.typed_relationships;

/**
 * @author Michael J. Simons
 */
public class TypedEntity<T> {

    private Long id;

    private T someThing;

    @SuppressWarnings({ "unused" })
    public TypedEntity() {
    }

    public TypedEntity(T someThing) {
        this.someThing = someThing;
    }

    public T getSomeThing() {
        return someThing;
    }
}
