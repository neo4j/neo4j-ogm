/*
 * Copyright (c) 2002-2020 "Neo4j,"
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for parsing the index/constraint description to {@link org.neo4j.ogm.autoindex.AutoIndex}
 *
 * @author Frantisek Hartman
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
public class AutoIndexTest {

    private Map<String, Object> indexRow;
    private Map<String, Object> constraintRow;

    @Before
    public void prepare() {

        indexRow = new HashMap<>();
        indexRow.put("type", "node_label_property");
        constraintRow = new HashMap<>();
        constraintRow.put("type", "node_unique_property");
    }

    @Test
    public void parseIndex() {
        indexRow.put("description", "INDEX ON :Person(name)");

        AutoIndex index = AutoIndex.parseIndex(indexRow, "3.5").get();
        assertThat(index.getOwningType()).isEqualTo("Person");
        assertThat(index.getProperties()).containsOnly("name");
        assertThat(index.getType()).isEqualTo(IndexType.SINGLE_INDEX);
        assertThat(index.getDescription()).isEqualTo("INDEX ON :`Person`(`name`)");
    }

    @Test
    public void parseCompositeIndex() {
        indexRow.put("description", "INDEX ON :Person(name,id)");

        AutoIndex index = AutoIndex.parseIndex(indexRow, "3.5").get();
        assertThat(index.getOwningType()).isEqualTo("Person");
        assertThat(index.getProperties()).containsOnly("name", "id");
        assertThat(index.getType()).isEqualTo(IndexType.COMPOSITE_INDEX);
        assertThat(index.getDescription()).isEqualTo("INDEX ON :`Person`(`name`,`id`)");
    }

    @Test
    public void parseUniqueConstraint() {
        constraintRow.put("description", "CONSTRAINT ON ( person:Person ) ASSERT person.name IS UNIQUE");

        AutoIndex index = AutoIndex.parseConstraint(constraintRow, "3.5").get();
        assertThat(index.getOwningType()).isEqualTo("Person");
        assertThat(index.getProperties()).containsOnly("name");
        assertThat(index.getType()).isEqualTo(IndexType.UNIQUE_CONSTRAINT);
        assertThat(index.getDescription())
            .isEqualTo("CONSTRAINT ON (`person`:`Person`) ASSERT `person`.`name` IS UNIQUE");
    }

    @Test
    public void parseNodeKeyConstraint() {
        constraintRow.put("description", "CONSTRAINT ON ( person:Person ) ASSERT (person.name, person.id) IS NODE KEY");

        AutoIndex index = AutoIndex.parseConstraint(constraintRow, "3.5").get();
        assertThat(index.getOwningType()).isEqualTo("Person");
        assertThat(index.getProperties()).containsOnly("name", "id");
        assertThat(index.getType()).isEqualTo(IndexType.NODE_KEY_CONSTRAINT);
        assertThat(index.getDescription())
            .isEqualTo("CONSTRAINT ON (`person`:`Person`) ASSERT (`person`.`name`,`person`.`id`) IS NODE KEY");
    }

    @Test
    public void parseNodePropertyExistenceConstraint() {
        constraintRow.put("description", "CONSTRAINT ON ( person:Person ) ASSERT exists(person.name)");

        AutoIndex index = AutoIndex.parseConstraint(constraintRow, "3.5").get();
        assertThat(index.getOwningType()).isEqualTo("Person");
        assertThat(index.getProperties()).containsOnly("name");
        assertThat(index.getType()).isEqualTo(IndexType.NODE_PROP_EXISTENCE_CONSTRAINT);
        assertThat(index.getDescription())
            .isEqualTo("CONSTRAINT ON (`person`:`Person`) ASSERT exists(`person`.`name`)");
    }

    @Test
    public void shouldRelationshipPropertyExistenceConstraint() {
        constraintRow.put("description", "CONSTRAINT ON ()-[like:LIKED]-() ASSERT exists(like.stars)");

        AutoIndex index = AutoIndex.parseConstraint(constraintRow, "3.5").get();
        assertThat(index.getOwningType()).isEqualTo("LIKED");
        assertThat(index.getProperties()).containsOnly("stars");
        assertThat(index.getType()).isEqualTo(IndexType.REL_PROP_EXISTENCE_CONSTRAINT);
        assertThat(index.getDescription())
            .isEqualTo("CONSTRAINT ON ()-[`liked`:`LIKED`]-() ASSERT exists(`liked`.`stars`)");
    }

    @Test
    public void shouldProvideValidDropAndCreateStatementsForConstraints() {
        constraintRow.put("description", "CONSTRAINT ON ( x:X ) ASSERT x.objId IS UNIQUE");

        Optional<AutoIndex> optionalAutoIndex = AutoIndex.parseConstraint(constraintRow, "3.5");

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
        indexRow.put("description", "INDEX ON :X(deprecated)");

        Optional<AutoIndex> optionalAutoIndex = AutoIndex.parseIndex(indexRow, "3.5");
        assertThat(optionalAutoIndex).isPresent();
        assertThat(optionalAutoIndex).hasValueSatisfying(index -> {
            assertThat(index.getCreateStatement().getStatement()).isEqualTo("CREATE INDEX ON :`X`(`deprecated`)");
            assertThat(index.getDropStatement().getStatement()).isEqualTo("DROP INDEX ON :`X`(`deprecated`)");
            assertThat(index.getDescription()).isEqualTo("INDEX ON :`X`(`deprecated`)");
        });
    }

    @Test // GH-759
    public void shouldNotFailOnUnknownIndexType() {

        indexRow.put("description", "INDEX ON NODE:User(givenName, familyName)");
        indexRow.put("indexName", "name");
        indexRow.put("tokenNames", new String[] { "User" });
        indexRow.put("properties", new String[] { "givenName", "familyName" });
        indexRow.put("state", "ONLINE");
        indexRow.put("type", "node_fulltext");
        indexRow.put("progress", 100.0);
        Map<String, String> provider = new HashMap<>();
        provider.put("version", "1.0");
        provider.put("key", "fulltext");
        indexRow.put("provider", provider);
        indexRow.put("id", 3);
        indexRow.put("failureMessage", "");

        Optional<AutoIndex> optionalAutoIndex = AutoIndex.parseIndex(indexRow, "3.5");
        assertThat(optionalAutoIndex).isEmpty();

        optionalAutoIndex = AutoIndex.parseIndex(indexRow, "4.0");
        assertThat(optionalAutoIndex).isEmpty();
    }
}
