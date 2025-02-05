/*
 * Copyright (c) 2002-2025 "Neo4j,"
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
package org.neo4j.ogm.metadata.schema;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.neo4j.ogm.annotation.Relationship.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.ogm.metadata.DomainInfo;
import org.neo4j.ogm.domain.inheritance.Associated;
import org.neo4j.ogm.domain.simple.Mortal;
import org.neo4j.ogm.domain.simple.Vertex;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class DomainInfoSchemaBuilderTest {

    public static final String SIMPLE_SCHEMA = "org.neo4j.ogm.domain.simple";

    private Schema schema;

    @BeforeEach
    public void setUp() {
        DomainInfo domainInfo = DomainInfo.create(SIMPLE_SCHEMA);
        schema = new DomainInfoSchemaBuilder(domainInfo).build();
    }

    @Test
    void givenNodeEntity_thenNodeHasLabelFromEntity() {
        Node person = schema.findNode("Person");
        assertThat(person).isNotNull();
        assertThat(person.label().get()).isEqualTo("Person");
    }

    @Test
    void whenBuildSchema_thenNodesHaveLabelsFromNodeEntityAnnotation() {
        // label provided in annotation
        Node company = schema.findNode("Happening");
        assertThat(company).isNotNull();
        assertThat(company.label().get()).isEqualTo("Happening");
    }

    @Test
    void givenNodeEntityWithoutRelationships_whenBuildSchema_thenNodeHasNoRelationships() {
        Node organisation = schema.findNode("Happening");

        assertThat(organisation.relationships()).isEmpty();
    }

    @Test
    void givenSingleEndedRelationship_thenRelationshipExists() {

        Node person = schema.findNode("Person");
        Map<String, Relationship> relationships = person.relationships();

        Relationship employer = relationships.get("employer");
        assertThat(employer).isNotNull();
        assertThat(employer.type()).isEqualTo("EMPLOYED_BY");
        assertThat(employer.direction(person)).isEqualTo(Direction.OUTGOING);
        assertThat(employer.other(person)).isEqualTo(schema.findNode("Organisation"));
    }

    @Test
    void givenSingleEndedRelationshipEntity_thenRelationshipExists() {
        Node person = schema.findNode("Person");
        Map<String, Relationship> relationships = person.relationships();

        Relationship organisations = relationships.get("organisations");
        assertThat(organisations).isNotNull();
        assertThat(organisations.type()).isEqualTo("FOUNDED");
        assertThat(organisations.direction(person)).isEqualTo(Direction.OUTGOING);
        assertThat(organisations.other(person)).isEqualTo(schema.findNode("Organisation"));
    }

    @Test
    void givenDoubleEndedRelationship_thenRelationshipExistsInBoth() {
        Node person = schema.findNode("Person");
        Node location = schema.findNode("Location");

        Relationship locationRel = person.relationships().get("location");
        assertThat(locationRel).isNotNull();
        assertThat(locationRel.type()).isEqualTo("LIVES_AT");
        assertThat(locationRel.direction(person)).isEqualTo(Direction.OUTGOING);
        assertThat(locationRel.other(person)).isEqualTo(location);

        Relationship residents = location.relationships().get("residents");
        assertThat(residents).isNotNull();
        assertThat(residents.type()).isEqualTo("LIVES_AT");
        assertThat(residents.direction(location)).isEqualTo(Direction.INCOMING);
        assertThat(residents.other(location)).isEqualTo(person);
    }

    @Test
    void givenStartNodeIsSupertype_thenMapCorrectly() {
        DomainInfo domainInfo = DomainInfo.create(Associated.class.getPackage().getName());
        schema = new DomainInfoSchemaBuilder(domainInfo).build();

        Node entity = schema.findNode("Entity");
        Node organisation = schema.findNode("Organisation");

        Relationship associations = organisation.relationships().get("associations");
        assertThat(associations.type()).isEqualTo("ASSOCIATED_WITH");
        assertThat(associations.other(organisation)).isEqualTo(entity);
    }

    @Test
    void givenSubtypeOfGenericType_thenPamCorrectly() {
        DomainInfo domainInfo = DomainInfo
            .create(org.neo4j.ogm.domain.generics.Person.class.getPackage().getName());
        schema = new DomainInfoSchemaBuilder(domainInfo).build();

        Node person = schema.findNode("Person");
        Map<String, Relationship> relationships = person.relationships();

        Relationship organisations = relationships.get("organisations");
        assertThat(organisations).isNotNull();
        assertThat(organisations.type()).isEqualTo("FOUNDED");
        assertThat(organisations.direction(person)).isEqualTo(Direction.OUTGOING);
        assertThat(organisations.other(person)).isEqualTo(schema.findNode("Organisation"));
    }

    @Test
    void givenNonAnnotatedAbstractClass_thenThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {

            DomainInfo domainInfo = DomainInfo
                .create(org.neo4j.ogm.domain.inheritance.Person.class.getPackage().getName());
            schema = new DomainInfoSchemaBuilder(domainInfo).build();

            Node entity = schema.findNode("Entity");
            assertThat(entity.label().get()).isEqualTo("Entity");

            schema.findNode("Company");
        });
    }

    @Test
    void givenIncomingRelationshipToSelf_thenMapDirectionCorrectly() {
        DomainInfo domainInfo = DomainInfo.create(Mortal.class.getPackage().getName());
        schema = new DomainInfoSchemaBuilder(domainInfo).build();

        Node mortal = schema.findNode("Mortal");
        Relationship knownBy = mortal.relationships().get("knownBy");
        assertThat(knownBy.direction(mortal)).isEqualTo(Direction.INCOMING);
    }

    @Test
    void givenRelationshipIsArray_thenMapRelationshipType() {
        DomainInfo domainInfo = DomainInfo.create(Vertex.class.getName());
        schema = new DomainInfoSchemaBuilder(domainInfo).build();

        Node node = schema.findNode("Vertex");
        Relationship nodes = node.relationships().get("vertices");
        assertThat(nodes.type()).isEqualTo("EDGE");
        assertThat(nodes.direction(node)).isEqualTo(Direction.OUTGOING);
        assertThat(nodes.other(node)).isEqualTo(node);
    }

    @Test
    void givenRelationshipEntity_whenFindRelationship_thenRelationshipIsFound() {
        Relationship relationship = schema.findRelationship("FOUNDED");

        assertThat(relationship.type()).isEqualTo("FOUNDED");
    }

    @Test
    void givenRelationshipEntityNotReferredFromNodeEntities_whenFindRelationship_thenRelationshipIsFound() {
        Relationship relationship = schema.findRelationship("VISITED");

        assertThat(relationship).isNotNull();
        assertThat(relationship.start()).isEqualTo(schema.findNode("Person"));
        assertThat(relationship.type()).isEqualTo("VISITED");
    }
}
