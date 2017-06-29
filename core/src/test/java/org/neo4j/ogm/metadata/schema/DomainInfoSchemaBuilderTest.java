/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.metadata.schema;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.metadata.DomainInfo;
import org.neo4j.ogm.metadata.schema.inheritance.Associated;
import org.neo4j.ogm.metadata.schema.simple.Mortal;
import org.neo4j.ogm.metadata.schema.simple.Organisation;
import org.neo4j.ogm.metadata.schema.simple.Vertex;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.neo4j.ogm.annotation.Relationship.INCOMING;
import static org.neo4j.ogm.annotation.Relationship.OUTGOING;

/**
 * @author Frantisek Hartman
 */
public class DomainInfoSchemaBuilderTest {

    public static final String SIMPLE_SCHEMA = "org.neo4j.ogm.metadata.schema.simple";

    private Schema schema;

    @Before
    public void setUp() throws Exception {
        DomainInfo domainInfo = DomainInfo.create(SIMPLE_SCHEMA);
        schema = new DomainInfoSchemaBuilder(domainInfo).build();
    }

    @Test
    public void givenNodeEntity_thenNodeHasLabelFromEntity() throws Exception {
        Node person = schema.findNode("Person");
        assertThat(person).isNotNull();
        assertThat(person.labels()).containsOnly("Person");
    }

    @Test
    public void whenBuildSchema_thenNodesHaveLabelsFromNodeEntityAnnotation() throws Exception {
        // label provided in annotation
        Node company = schema.findNode("Happening");
        assertThat(company).isNotNull();
        assertThat(company.labels()).containsOnly("Happening");
    }

    @Test
    public void givenNodeEntityWithoutRelationships_whenBuildSchema_thenNodeHasNoRelationships() throws Exception {
        Node organisation = schema.findNode("Happening");

        assertThat(organisation.relationships()).isEmpty();
    }

    @Test
    public void givenSingleEndedRelationship_thenRelationshipExists() throws Exception {

        Node person = schema.findNode("Person");
        Map<String, Relationship> relationships = person.relationships();

        Relationship employer = relationships.get("employer");
        assertThat(employer).isNotNull();
        assertThat(employer.type()).isEqualTo("EMPLOYED_BY");
        assertThat(employer.direction(person)).isEqualTo(OUTGOING);
        assertThat(employer.other(person)).isEqualTo(schema.findNode("Organisation"));
    }

    @Test
    public void givenSingleEndedRelationshipEntity_thenRelationshipExists() throws Exception {
        Node person = schema.findNode("Person");
        Map<String, Relationship> relationships = person.relationships();

        Relationship organisations = relationships.get("organisations");
        assertThat(organisations).isNotNull();
        assertThat(organisations.type()).isEqualTo("FOUNDED");
        assertThat(organisations.direction(person)).isEqualTo(OUTGOING);
        assertThat(organisations.other(person)).isEqualTo(schema.findNode("Organisation"));
    }

    @Test
    public void givenDoubleEndedRelationship_thenRelationshipExistsInBoth() throws Exception {
        Node person = schema.findNode("Person");
        Node location = schema.findNode("Location");

        Relationship locationRel = person.relationships().get("location");
        assertThat(locationRel).isNotNull();
        assertThat(locationRel.type()).isEqualTo("LIVES_AT");
        assertThat(locationRel.direction(person)).isEqualTo(OUTGOING);
        assertThat(locationRel.other(person)).isEqualTo(location);


        Relationship residents = location.relationships().get("residents");
        assertThat(residents).isNotNull();
        assertThat(residents.type()).isEqualTo("LIVES_AT");
        assertThat(residents.direction(location)).isEqualTo(INCOMING);
        assertThat(residents.other(location)).isEqualTo(person);
    }

    @Test
    public void givenStartNodeIsSupertype_thenMapCorrectly() throws Exception {
        DomainInfo domainInfo = DomainInfo.create(Associated.class.getPackage().getName());
        schema = new DomainInfoSchemaBuilder(domainInfo).build();

        Node entity = schema.findNode("Entity");
        Node organisation = schema.findNode("Organisation");

        Relationship associations = organisation.relationships().get("associations");
        assertThat(associations.type()).isEqualTo("ASSOCIATED_WITH");
        assertThat(associations.other(organisation)).isEqualTo(entity);
    }

    @Test
    public void givenSubtypeOfGenericType_thenPamCorrectly() throws Exception {
        DomainInfo domainInfo = DomainInfo.create(org.neo4j.ogm.metadata.schema.generics.Person.class.getPackage().getName());
        schema = new DomainInfoSchemaBuilder(domainInfo).build();

        Node person = schema.findNode("Person");
        Map<String, Relationship> relationships = person.relationships();

        Relationship organisations = relationships.get("organisations");
        assertThat(organisations).isNotNull();
        assertThat(organisations.type()).isEqualTo("FOUNDED");
        assertThat(organisations.direction(person)).isEqualTo(OUTGOING);
        assertThat(organisations.other(person)).isEqualTo(schema.findNode("Organisation"));
    }

    @Test
    public void givenIncomingRelationshipToSelf_thenMapDirectionCorrectly() throws Exception {
        DomainInfo domainInfo = DomainInfo.create(Mortal.class.getPackage().getName());
        schema = new DomainInfoSchemaBuilder(domainInfo).build();

        Node mortal = schema.findNode("Mortal");
        Relationship knownBy = mortal.relationships().get("knownBy");
        assertThat(knownBy.direction(mortal)).isEqualTo(INCOMING);
    }

    @Test
    public void givenRelationshipIsArray_thenMapRelationshipType() throws Exception {
        DomainInfo domainInfo = DomainInfo.create(Vertex.class.getName());
        schema = new DomainInfoSchemaBuilder(domainInfo).build();

        Node node = schema.findNode("Vertex");
        Relationship nodes = node.relationships().get("vertices");
        assertThat(nodes.type()).isEqualTo("EDGE");
        assertThat(nodes.direction(node)).isEqualTo(OUTGOING);
        assertThat(nodes.other(node)).isEqualTo(node);
    }
}
