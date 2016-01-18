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
import org.neo4j.ogm.domain.simpleNetwork.interfaces.InterfaceStateNode;
import org.neo4j.ogm.domain.simpleNetwork.interfaces.InterfaceTimeRelation;

/**
 * @author vince
 */
public abstract class AbstractStateNode<R extends InterfaceTimeRelation> implements InterfaceStateNode<R> {

    @GraphId
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public abstract R getIdentityRelation();

    @Override
    public abstract void setIdentityRelation(R identityRelation);
}
