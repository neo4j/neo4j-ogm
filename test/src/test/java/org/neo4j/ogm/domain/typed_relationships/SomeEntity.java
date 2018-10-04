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

import java.util.List;

import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Michael J. Simons
 */

public class SomeEntity {

    private Long id;

    @Relationship(type = "IRRELEVANT", direction = Relationship.INCOMING)
    private TypedEntity<?> thing;

    @Relationship(type = "ALSO_IRRELEVANT", direction = Relationship.INCOMING)
    private List<TypedEntity<?>> moreThings;

    protected List<String> someOtherStuff;

    public Long getId() {
        return id;
    }

    public TypedEntity<?> getThing() {
        return thing;
    }

    public void setThing(TypedEntity<?> thing) {
        this.thing = thing;
    }

    public List<TypedEntity<?>> getMoreThings() {
        return moreThings;
    }

    public void setMoreThings(List<TypedEntity<?>> moreThings) {
        this.moreThings = moreThings;
    }

    public List<String> getSomeOtherStuff() {
        return someOtherStuff;
    }

    public void setSomeOtherStuff(List<String> someOtherStuff) {
        this.someOtherStuff = someOtherStuff;
    }
}
