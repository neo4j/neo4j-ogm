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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for parsing the index/constraint description to {@link org.neo4j.ogm.autoindex.AutoIndex}
 * @author Frantisek Hartman
 */
public class AutoIndexTest {

    @Test
    public void parseIndex() throws Exception {
        AutoIndex index = AutoIndex.parse("INDEX ON :Person(name)").get();
        assertThat(index.getOwningType()).isEqualTo("Person");
        assertThat(index.getProperties()).containsOnly("name");
        assertThat(index.getType()).isEqualTo(IndexType.SINGLE_INDEX);
    }

    @Test
    public void parseCompositeIndex() throws Exception {
        AutoIndex index = AutoIndex.parse("INDEX ON :Person(name,id)").get();
        assertThat(index.getOwningType()).isEqualTo("Person");
        assertThat(index.getProperties()).containsOnly("name", "id");
        assertThat(index.getType()).isEqualTo(IndexType.COMPOSITE_INDEX);
    }

    @Test
    public void parseUniqueConstraint() throws Exception {
        AutoIndex index = AutoIndex.parse("CONSTRAINT ON ( person:Person ) ASSERT person.name IS UNIQUE").get();
        assertThat(index.getOwningType()).isEqualTo("Person");
        assertThat(index.getProperties()).containsOnly("name");
        assertThat(index.getType()).isEqualTo(IndexType.UNIQUE_CONSTRAINT);
    }

    @Test
    public void parseNodeKeyConstraint() throws Exception {
        AutoIndex index = AutoIndex
            .parse("CONSTRAINT ON ( person:Person ) ASSERT (person.name, person.id) IS NODE KEY").get();
        assertThat(index.getOwningType()).isEqualTo("Person");
        assertThat(index.getProperties()).containsOnly("name", "id");
        assertThat(index.getType()).isEqualTo(IndexType.NODE_KEY_CONSTRAINT);

    }

    @Test
    public void parseNodePropertyExistenceConstraint() throws Exception {
        AutoIndex index = AutoIndex.parse("CONSTRAINT ON ( person:Person ) ASSERT exists(person.name)").get();
        assertThat(index.getOwningType()).isEqualTo("Person");
        assertThat(index.getProperties()).containsOnly("name");
        assertThat(index.getType()).isEqualTo(IndexType.NODE_PROP_EXISTENCE_CONSTRAINT);
    }

    @Test
    public void shouldRelationshipPropertyExistenceConstraint() throws Exception {
        AutoIndex index = AutoIndex.parse("CONSTRAINT ON ()-[like:LIKED]-() ASSERT exists(like.stars)").get();
        assertThat(index.getOwningType()).isEqualTo("LIKED");
        assertThat(index.getProperties()).containsOnly("stars");
        assertThat(index.getType()).isEqualTo(IndexType.REL_PROP_EXISTENCE_CONSTRAINT);
    }
}
