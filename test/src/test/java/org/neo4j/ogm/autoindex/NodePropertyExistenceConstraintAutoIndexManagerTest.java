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

package org.neo4j.ogm.autoindex;

import static org.junit.Assume.*;

import org.junit.BeforeClass;
import org.neo4j.ogm.domain.autoindex.NodePropertyExistenceConstraintEntity;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class NodePropertyExistenceConstraintAutoIndexManagerTest extends BaseAutoIndexManagerTestClass {

    public NodePropertyExistenceConstraintAutoIndexManagerTest() {
        super(new String[] { "CONSTRAINT ON (`entity`:`Entity`) ASSERT exists(`entity`.`login`)" },
            NodePropertyExistenceConstraintEntity.class);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        assumeTrue("This test uses existence constraint and can only be run on enterprise edition",
            isEnterpriseEdition());
    }
}
