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
package org.neo4j.ogm.domain.simpleNetwork.abstractions;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.domain.simpleNetwork.interfaces.InterfaceIdentityNode;
import org.neo4j.ogm.domain.simpleNetwork.interfaces.InterfaceTimeRelation;

import java.util.Set;

/**
 * @author vince
 */
public abstract class AbstractIdentityNode<R extends InterfaceTimeRelation> implements InterfaceIdentityNode {

    @GraphId
    private Long id;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public abstract Set<R> getStates();

    public abstract void setStates(Set<R> states);
}
