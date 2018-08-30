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

package org.neo4j.ogm.domain.generic_hierarchy;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Jonathan D'Orleans
 * @author Michael J. Simons
 */
public class ChildA extends Entity {

    private String value;

    private Set<AnotherEntity> children = new HashSet<>();

    public ChildA() {
    }

    public ChildA(String uuid) {
        super(uuid);
    }

    public ChildA add(AnotherEntity childB) {
        children.add(childB);
        return this;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Set<AnotherEntity> getChildren() {
        return children;
    }

    public void setChildren(Set<AnotherEntity> children) {
        this.children = children;
    }
}
