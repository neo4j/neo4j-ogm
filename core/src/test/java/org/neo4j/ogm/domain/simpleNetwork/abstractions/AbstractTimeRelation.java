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

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.domain.simpleNetwork.interfaces.InterfaceIdentityNode;
import org.neo4j.ogm.domain.simpleNetwork.interfaces.InterfaceStateNode;
import org.neo4j.ogm.domain.simpleNetwork.interfaces.InterfaceTimeRelation;

/**
 * @author vince
 */

/**
 * @param <S> a class implementing InterfaceIdentityNode
 * @param <T> a class implementing InterfaceStateNode
 * @see issue #42
 * <p/>
 * This class posed problems for the OGM because S and T are defined as classes that extend interfaces, rather
 * than concrete classes. Prior to the fix for this issue, this OGM would only work correctly if S and T were defined
 * as extending concrete classes.
 */
public abstract class AbstractTimeRelation<S extends InterfaceIdentityNode, T extends InterfaceStateNode> implements InterfaceTimeRelation<S, T> {

    @GraphId
    private Long graphId;

    @StartNode
    private S identity;

    @EndNode
    private T state;

    @Property
    private Long from;

    @Property
    private Long to;

    @Override
    public S getSourceNode() {
        return identity;
    }

    @Override
    public void setSourceNode(S sourceNode) {
        this.identity = sourceNode;
    }

    @Override
    public T getTargetNode() {
        return state;
    }

    @Override
    public void setTargetNode(T targetNode) {
        this.state = targetNode;
    }

    @Override
    public Long getRelationshipId() {
        return graphId;
    }

    @Override
    public void setRelationshipId(Long relationshipId) {
        this.graphId = relationshipId;
    }

    @Override
    public Long getTo() {
        return to;
    }

    @Override
    public void setTo(Long to) {
        this.to = to;
    }

    @Override
    public Long getFrom() {
        return from;
    }

    @Override
    public void setFrom(Long from) {
        this.from = from;
    }
}
