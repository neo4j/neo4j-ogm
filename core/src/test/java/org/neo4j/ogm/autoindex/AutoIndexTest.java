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

    @Test
    public void shouldProvideValidDropAndCreateStatementsForConstraints() {
        Optional<AutoIndex> optionalAutoIndex = AutoIndex.parse("CONSTRAINT ON ( x:X ) ASSERT x.objId IS UNIQUE");

        assertThat(optionalAutoIndex).isPresent();
        assertThat(optionalAutoIndex).hasValueSatisfying(index -> {
            assertThat(index.getCreateStatement().getStatement())
                .isEqualTo("CREATE CONSTRAINT ON (`x`:`X`) ASSERT `x`.`objId` IS UNIQUE");
            assertThat(index.getDropStatement().getStatement())
                .isEqualTo("DROP CONSTRAINT ON (`x`:`X`) ASSERT `x`.`objId` IS UNIQUE");
        });
    }

    @Test
    public void shouldProvideValidDropAndCreateStatementsForIndexes() {
        Optional<AutoIndex> optionalAutoIndex = AutoIndex.parse("INDEX ON :X(deprecated)");
        assertThat(optionalAutoIndex).isPresent();
        assertThat(optionalAutoIndex).hasValueSatisfying(index -> {
            assertThat(index.getCreateStatement().getStatement()).isEqualTo("CREATE INDEX ON :`X`(`deprecated`)");
            assertThat(index.getDropStatement().getStatement()).isEqualTo("DROP INDEX ON :`X`(`deprecated`)");
        });
    }
}
