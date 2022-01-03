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

import org.junit.Test;
import org.neo4j.ogm.metadata.DomainInfo;
import org.neo4j.ogm.metadata.schema.DomainInfoSchemaBuilder;
import org.neo4j.ogm.metadata.schema.Schema;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class SchemaNodeLoadClauseBuilderTest {

    @Test
    public void buildQuery() {
        SchemaNodeLoadClauseBuilder queryBuilder = createQueryBuilder();

        String query = queryBuilder.build("n", "Person", 0);

        assertThat(query).isEqualTo(" RETURN n");
    }

    @Test
    public void buildQueryFromLocation() {
        SchemaNodeLoadClauseBuilder queryBuilder = createQueryBuilder();

        String query = queryBuilder.build("n", "Location", 1);
        assertThat(query).isEqualTo(" RETURN n,[ [ (n)<-[r_l1:`LIVES_AT`]-(p1:`Person`) | [ r_l1, p1 ] ] ]");
    }

    @Test
    public void buildQueryFromPerson() {
        SchemaNodeLoadClauseBuilder queryBuilder = createQueryBuilder();

        String query = queryBuilder.build("n", "Person", 2);
        assertThat(query).isEqualTo(" RETURN n,[ " +
            "[ (n)-[r_f1:`FOUNDED`]->(o1:`Organisation`) | [ r_f1, o1 ] ], " +
            "[ (n)-[r_e1:`EMPLOYED_BY`]->(o1:`Organisation`) | [ r_e1, o1 ] ], " +
            "[ (n)-[r_l1:`LIVES_AT`]->(l1:`Location`) | [ r_l1, l1, " +
            "[ [ (l1)<-[r_l2:`LIVES_AT`]-(p2:`Person`) | [ r_l2, p2 ] ] ] " +
            "] ] " +
            "]");
    }

    @Test
    public void givenNodeWithNoRelationships_thenCreateSimpleQuery() {
        SchemaNodeLoadClauseBuilder queryBuilder = createQueryBuilder();

        String query = queryBuilder.build("n", "Organisation", 1);

        assertThat(query).isEqualTo(" RETURN n");
    }

    @Test
    public void givenLabelStartingWithR_thenNodeNameAndrelationshipNameShouldNotConflict() {
        SchemaNodeLoadClauseBuilder queryBuilder = createQueryBuilder();

        String query = queryBuilder.build("n", "Restaurant", 1);

        assertThat(query).isEqualTo(" RETURN n,[ [ (n)-[r_s1:`SIMILAR_TO`]->(r1:`Restaurant`) | [ r_s1, r1 ] ] ]");
    }

    @Test // GH-670
    public void shouldMatchRelationshipForAllPossibleMappings() {
        DomainInfo domainInfo = DomainInfo.create("org.neo4j.ogm.domain.gh670");
        Schema schema = new DomainInfoSchemaBuilder(domainInfo).build();
        SchemaNodeLoadClauseBuilder builder = new SchemaNodeLoadClauseBuilder(schema);

        String query;
        query = builder.build("n", "Person", 1); // Most generic query
        assertThat(query).isEqualTo(
            " RETURN n,[ [ (n)-[r_c1:`COURCES`]->(c1:`Course`) | [ r_c1, c1 ] ], [ (n)-[r_t1:`TAKES`]->(c1:`Course`) | [ r_t1, c1 ] ], [ (n)<-[r_t1:`TAUGHT_BY`]-(t1:`Teacher`) | [ r_t1, t1 ] ], [ (n)-[r_f1:`FRIENDS`]->(p1:`Pupil`) | [ r_f1, p1 ] ] ]");

        query = builder.build("n", "Pupil", 1); // Only the Klassenclown below
        assertThat(query).isEqualTo(
            " RETURN n,[ [ (n)-[r_t1:`TAKES`]->(c1:`Course`) | [ r_t1, c1 ] ], [ (n)<-[r_t1:`TAUGHT_BY`]-(t1:`Teacher`) | [ r_t1, t1 ] ], [ (n)-[r_f1:`FRIENDS`]->(p1:`Pupil`) | [ r_f1, p1 ] ] ]");

        query = builder.build("n", "Klassenclown", 1); // Is actually the same
        assertThat(query).isEqualTo(
            " RETURN n,[ [ (n)-[r_t1:`TAKES`]->(c1:`Course`) | [ r_t1, c1 ] ], [ (n)<-[r_t1:`TAUGHT_BY`]-(t1:`Teacher`) | [ r_t1, t1 ] ], [ (n)-[r_f1:`FRIENDS`]->(p1:`Pupil`) | [ r_f1, p1 ] ] ]");

        query = builder.build("n", "Teacher", 1); // No inheritance below teacher
        assertThat(query).isEqualTo(" RETURN n,[ [ (n)-[r_c1:`COURCES`]->(c1:`Course`) | [ r_c1, c1 ] ] ]");

        query = builder.build("n", "Teacher",
            3); // No inheritance below teacher, but triggering the person hierachy with depth 3 (course - takenBy - pupil)
        assertThat(query).isEqualTo(
            " RETURN n,[ [ (n)-[r_c1:`COURCES`]->(c1:`Course`) | [ r_c1, c1, [ [ (c1)<-[r_t2:`TAKES`]-(p2:`Pupil`) | [ r_t2, p2, [ [ (p2)-[r_t3:`TAKES`]->(c3:`Course`) | [ r_t3, c3 ] ], [ (p2)<-[r_t3:`TAUGHT_BY`]-(t3:`Teacher`) | [ r_t3, t3 ] ], [ (p2)-[r_f3:`FRIENDS`]->(p3:`Pupil`) | [ r_f3, p3 ] ] ] ] ] ] ] ] ]");
    }

    private SchemaNodeLoadClauseBuilder createQueryBuilder() {
        DomainInfo domainInfo = DomainInfo.create("org.neo4j.ogm.domain.simple");
        Schema schema = new DomainInfoSchemaBuilder(domainInfo).build();
        return new SchemaNodeLoadClauseBuilder(schema);
    }

}
