/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.ogm.autoindex;

import static org.junit.Assume.*;

import org.junit.BeforeClass;
import org.neo4j.ogm.domain.autoindex.Entity;
import org.neo4j.ogm.domain.autoindex.RelPropertyExistenceConstraintEntity;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class RelPropertyExistenceConstraintAutoIndexManagerTest extends BaseAutoIndexManagerTestClass {

    public RelPropertyExistenceConstraintAutoIndexManagerTest() {
        super(new String[] { "CONSTRAINT ON ()-[`rel`:`REL`]-() ASSERT exists(`rel`.`description`)" },
            Entity.class, RelPropertyExistenceConstraintEntity.class);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        assumeTrue("This test uses existence constraint and can only be run on enterprise edition",
            isEnterpriseEdition());
    }
}
