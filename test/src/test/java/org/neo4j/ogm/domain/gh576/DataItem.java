/*
 * Copyright (c) 2002-2019 "Neo Technology,"
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
package org.neo4j.ogm.domain.gh576;

import java.util.List;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Andreas Berger
 */
@NodeEntity
public class DataItem extends BaseEntity {

    private String nodeId;

    /*
     * This field is here, b/c the neo4j ogm driver optimizes queries against the class given to the query.
     * If we want the returned child entities to have its relations mapped as well, we need to tell OGM all
     * the fields by adding them here
     */
    @Relationship(type = "USES")
    protected List<Variable> variables;

    public String getNodeId() {
        return nodeId;
    }
}
