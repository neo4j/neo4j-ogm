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

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.Test;

/**
 * Test for parsing the index/constraint description to {@link org.neo4j.ogm.autoindex.AutoIndex}
 *
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class AutoIndexTest {

    @Test
    public void parseIndex() {
        AutoIndex index = AutoIndex.parse("INDEX ON :Person(name)").get();
        assertThat(index.getOwningType()).isEqualTo("Person");
        assertThat(index.getProperties()).containsOnly("name");
        assertThat(index.getType()).isEqualTo(IndexType.SINGLE_INDEX);
        assertThat(index.getDescription()).isEqualTo("INDEX ON :`Person`(`name`)");
    }

    @Test
    public void parseCompositeIndex() {
        AutoIndex index = AutoIndex.parse("INDEX ON :Person(name,id)").get();
        assertThat(index.getOwningType()).isEqualTo("Person");
        assertThat(index.getProperties()).containsOnly("name", "id");
        assertThat(index.getType()).isEqualTo(IndexType.COMPOSITE_INDEX);
        assertThat(index.getDescription()).isEqualTo("INDEX ON :`Person`(`name`,`id`)");
    }

    @Test
    public void parseUniqueConstraint() {
        AutoIndex index = AutoIndex.parse("CONSTRAINT ON ( person:Person ) ASSERT person.name IS UNIQUE").get();
        assertThat(index.getOwningType()).isEqualTo("Person");
        assertThat(index.getProperties()).containsOnly("name");
        assertThat(index.getType()).isEqualTo(IndexType.UNIQUE_CONSTRAINT);
        assertThat(index.getDescription()).isEqualTo("CONSTRAINT ON (`person`:`Person`) ASSERT `person`.`name` IS UNIQUE");
    }

    @Test
    public void parseNodeKeyConstraint() {
        AutoIndex index = AutoIndex
            .parse("CONSTRAINT ON ( person:Person ) ASSERT (person.name, person.id) IS NODE KEY").get();
        assertThat(index.getOwningType()).isEqualTo("Person");
        assertThat(index.getProperties()).containsOnly("name", "id");
        assertThat(index.getType()).isEqualTo(IndexType.NODE_KEY_CONSTRAINT);
        assertThat(index.getDescription()).isEqualTo("CONSTRAINT ON (`person`:`Person`) ASSERT (`person`.`name`,`person`.`id`) IS NODE KEY");
    }

    @Test
    public void parseNodePropertyExistenceConstraint() {
        AutoIndex index = AutoIndex.parse("CONSTRAINT ON ( person:Person ) ASSERT exists(person.name)").get();
        assertThat(index.getOwningType()).isEqualTo("Person");
        assertThat(index.getProperties()).containsOnly("name");
        assertThat(index.getType()).isEqualTo(IndexType.NODE_PROP_EXISTENCE_CONSTRAINT);
        assertThat(index.getDescription()).isEqualTo("CONSTRAINT ON (`person`:`Person`) ASSERT exists(`person`.`name`)");
    }

    @Test
    public void shouldRelationshipPropertyExistenceConstraint() {
        AutoIndex index = AutoIndex.parse("CONSTRAINT ON ()-[like:LIKED]-() ASSERT exists(like.stars)").get();
        assertThat(index.getOwningType()).isEqualTo("LIKED");
        assertThat(index.getProperties()).containsOnly("stars");
        assertThat(index.getType()).isEqualTo(IndexType.REL_PROP_EXISTENCE_CONSTRAINT);
        assertThat(index.getDescription()).isEqualTo("CONSTRAINT ON ()-[`liked`:`LIKED`]-() ASSERT exists(`liked`.`stars`)");
    }

    @Test
    public void shouldProvideValidDropAndCreateStatementsForConstraints() {
        Optional<AutoIndex> optionalAutoIndex = AutoIndex.parse("CONSTRAINT ON ( x:X ) ASSERT x.objId IS UNIQUE");

        assertThat(optionalAutoIndex).isPresent();
        assertThat(optionalAutoIndex).hasValueSatisfying(index -> {
            assertThat(index.getCreateStatement().getStatement())
                .isEqualTo("CREATE CONSTRAINT ON (`x`:`X`) ASSERT `x`.`objId` IS UNIQUE");
            assertThat(index.getDropStatement().getStatement())
                .isEqualTo("DROP CONSTRAINT ON (`x`:`X`) ASSERT `x`.`objId` IS UNIQUE");
            assertThat(index.getDescription()).isEqualTo("CONSTRAINT ON (`x`:`X`) ASSERT `x`.`objId` IS UNIQUE");
        });
    }

    @Test
    public void shouldProvideValidDropAndCreateStatementsForIndexes() {
        Optional<AutoIndex> optionalAutoIndex = AutoIndex.parse("INDEX ON :X(deprecated)");
        assertThat(optionalAutoIndex).isPresent();
        assertThat(optionalAutoIndex).hasValueSatisfying(index -> {
            assertThat(index.getCreateStatement().getStatement()).isEqualTo("CREATE INDEX ON :`X`(`deprecated`)");
            assertThat(index.getDropStatement().getStatement()).isEqualTo("DROP INDEX ON :`X`(`deprecated`)");
            assertThat(index.getDescription()).isEqualTo("INDEX ON :`X`(`deprecated`)");
        });
    }
}
