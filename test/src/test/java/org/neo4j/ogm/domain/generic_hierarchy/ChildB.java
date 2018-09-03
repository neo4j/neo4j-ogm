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

/**
 * @author Jonathan D'Orleans
 * @author Michael J. Simons
 */
public class ChildB extends AnotherEntity {

    private Integer value;

    public ChildB() {
    }

    public ChildB(String uuid) {
        super(uuid);
    }

    @Override
    public void postLoad() {
        value = uuid.hashCode();
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

}
