/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.domain.simpleNetwork.abstractions;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.domain.simpleNetwork.interfaces.InterfaceIdentityNode;
import org.neo4j.ogm.domain.simpleNetwork.interfaces.InterfaceStateNode;
import org.neo4j.ogm.domain.simpleNetwork.interfaces.InterfaceTimeRelation;

/**
 * See issue #42
 * <br>
 * This class posed problems for the OGM because S and T are defined as classes that extend interfaces, rather
 * than concrete classes. Prior to the fix for this issue, this OGM would only work correctly if S and T were defined
 * as extending concrete classes.
 *
 * @param <T> a class implementing InterfaceStateNode
 * @param <S> a class implementing InterfaceIdentityNode
 * @author vince
 */
public abstract class AbstractTimeRelation<S extends InterfaceIdentityNode, T extends InterfaceStateNode>
    implements InterfaceTimeRelation<S, T> {

    @Id @GeneratedValue
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
