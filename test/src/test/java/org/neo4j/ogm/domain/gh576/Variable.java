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

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * @author Andreas Berger
 */
@RelationshipEntity(type = "USES")
public class Variable extends BaseEntity {

    @StartNode
    private FormulaItem formulaItem;

    @EndNode
    private DataItem dataItem;

    private String variable;

    public FormulaItem getFormulaItem() {
        return formulaItem;
    }

    public DataItem getDataItem() {
        return dataItem;
    }

    public String getVariable() {
        return variable;
    }
}
