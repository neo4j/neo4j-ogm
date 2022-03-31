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
package org.neo4j.ogm.autoindex;

import static org.junit.Assume.*;

import org.junit.BeforeClass;
import org.neo4j.ogm.domain.autoindex.Entity;
import org.neo4j.ogm.domain.autoindex.RelPropertyExistenceConstraintEntity;

/**
 * While this test looks empty, it's put in place to execute all the stuff in {@link BaseAutoIndexManagerTestClass}
 * with the given constraints in the constructor.
 *
 * @author Frantisek Hartman
 * @author Michael J. Simons
 * @author Gerrit Meier
 */
public class RelPropertyExistenceConstraintAutoIndexManagerTest extends BaseAutoIndexManagerTestClass {

    public RelPropertyExistenceConstraintAutoIndexManagerTest() {
        super(new String[] { "CONSTRAINT org_neo4j_ogm_domain_autoindex_relpropertyexistenceconstraintentity_description_rel_prop_existence FOR ()-[`rel`:`REL`]-() REQUIRE `rel`.`description` IS NOT NULL" },
            Entity.class, RelPropertyExistenceConstraintEntity.class);
    }

    @BeforeClass
    public static void setUpClass() {
        assumeTrue("This test uses existence constraint and can only be run on enterprise edition",
            useEnterpriseEdition());
    }

    @Override
    protected void additionalTearDown() {
        // Nothing to be done here.
    }
}
