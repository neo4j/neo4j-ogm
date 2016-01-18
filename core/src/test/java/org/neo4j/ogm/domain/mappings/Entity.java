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

package org.neo4j.ogm.domain.mappings;

import org.neo4j.ogm.annotation.GraphId;

/**
 * @author Nils Dr\u00F6ge
 */
public abstract class Entity {
    @GraphId
    private Long nodeId;

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || nodeId == null || getClass() != o.getClass()) return false;

        Entity entity = (Entity) o;

        if (!nodeId.equals(entity.nodeId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (nodeId == null) ? -1 : nodeId.hashCode();
    }
}
