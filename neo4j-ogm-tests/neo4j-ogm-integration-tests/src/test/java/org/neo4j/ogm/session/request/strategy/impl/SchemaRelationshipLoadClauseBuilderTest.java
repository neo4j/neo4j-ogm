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
package org.neo4j.ogm.session.request.strategy.impl;

import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.metadata.DomainInfo;
import org.neo4j.ogm.metadata.schema.DomainInfoSchemaBuilder;
import org.neo4j.ogm.metadata.schema.Schema;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class SchemaRelationshipLoadClauseBuilderTest {

    private SchemaRelationshipLoadClauseBuilder builder;

    @Before
    public void setUp() {
        builder = createLoadClauseBuilder();
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildClauseDepthZeroNotAllowed() {


        builder.build("r", "FRIEND_OF", 0);
    }

    @Test
    public void buildClauseWithDepthTwo() {
        String clause = builder.build("r", "FOUNDED", 1);

        assertThat(clause).isEqualToIgnoringWhitespace(
            " RETURN r,n,"
                + "[ "
                + "[ (n)-[r_f1:`FOUNDED`]->(o1:`Organisation`) | [ r_f1, o1 ] ],"
                + "[ (n)-[r_e1:`EMPLOYED_BY`]->(o1:`Organisation`) | [ r_e1, o1 ] ],"
                + "[ (n)-[r_l1:`LIVES_AT`]->(l1:`Location`) | [ r_l1, l1 ] ] "
                + "],"
                + "m"
        );
    }

    private SchemaRelationshipLoadClauseBuilder createLoadClauseBuilder() {
        DomainInfo domainInfo = DomainInfo.create("org.neo4j.ogm.domain.simple");
        Schema schema = new DomainInfoSchemaBuilder(domainInfo).build();
        return new SchemaRelationshipLoadClauseBuilder(schema);
    }
}
