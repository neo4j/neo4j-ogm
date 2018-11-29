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

package org.neo4j.ogm.drivers.bolt.response;

import java.util.List;
import java.util.Map;

import org.neo4j.ogm.driver.TypeSystem;
import org.neo4j.ogm.drivers.bolt.driver.BoltEntityAdapter;
import org.neo4j.ogm.result.adapter.GraphModelAdapter;

/**
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class BoltGraphModelAdapter extends GraphModelAdapter {

    private final BoltEntityAdapter entityAdapter;

    public BoltGraphModelAdapter(BoltEntityAdapter entityAdapter) {
        this.entityAdapter = entityAdapter;
    }

    @Override
    public boolean isPath(Object value) {
        return entityAdapter.isPath(value);
    }

    @Override
    public boolean isNode(Object value) {
        return entityAdapter.isNode(value);
    }

    @Override
    public boolean isRelationship(Object value) {
        return entityAdapter.isRelationship(value);
    }

    @Override
    public long nodeId(Object node) {
        return entityAdapter.nodeId(node);
    }

    @Override
    public List<String> labels(Object entity) {
        return entityAdapter.labels(entity);
    }

    @Override
    public long relationshipId(Object relationship) {
        return entityAdapter.relationshipId(relationship);
    }

    @Override
    public String relationshipType(Object entity) {
        return entityAdapter.relationshipType(entity);
    }

    @Override
    public Long startNodeId(Object relationship) {
        return entityAdapter.startNodeId(relationship);
    }

    @Override
    public Long endNodeId(Object relationship) {
        return entityAdapter.endNodeId(relationship);
    }

    @Override
    public Map<String, Object> properties(Object container) {
        return entityAdapter.properties(container);
    }

    @Override
    public List<Object> nodesInPath(Object pathValue) {
        return entityAdapter.nodesInPath(pathValue);
    }

    @Override
    public List<Object> relsInPath(Object pathValue) {
        return entityAdapter.relsInPath(pathValue);
    }
}
