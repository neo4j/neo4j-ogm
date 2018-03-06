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

package org.neo4j.ogm.context;

import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.annotations.ids.ValidAnnotations.UuidAndGenerationType;
import org.neo4j.ogm.domain.policy.Person;
import org.neo4j.ogm.domain.policy.Policy;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.typeconversion.UuidStringConverter;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
public class MappingContextTest {

    private MappingContext mappingContext;
    private MetaData metaData;

    @Before
    public void setUp() {
        metaData = new MetaData("org.neo4j.ogm.domain.policy", "org.neo4j.ogm.context", "org.neo4j.ogm.domain.annotations.ids");
        mappingContext = new MappingContext(metaData);
    }

    @Test
    public void testPath() {

        Person jim = new Person("jim");
        jim.setId(1L);

        Policy policy = new Policy("healthcare");
        policy.setId(2L);

        mappingContext.addNodeEntity(jim);
        mappingContext.addNodeEntity(policy);
        mappingContext.addRelationship(
            new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), Person.class, Policy.class));

        assertThat(mappingContext.getNodeEntity(jim.getId())).isEqualTo(jim);
        assertThat(mappingContext.getNodeEntity(policy.getId())).isEqualTo(policy);
        assertThat(mappingContext.containsRelationship(
            new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), Person.class, Policy.class))).isTrue();
    }

    @Test
    public void clearOne() {

        Person jim = new Person("jim");
        jim.setId(1L);

        Policy policy = new Policy("healthcare");
        policy.setId(2L);

        mappingContext.addNodeEntity(jim);
        mappingContext.addNodeEntity(policy);
        mappingContext.addRelationship(
            new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), Person.class, Policy.class));
        mappingContext.removeEntity(jim);

        assertThat(mappingContext.getNodeEntity(jim.getId())).isEqualTo(null);
        assertThat(mappingContext.getNodeEntity(policy.getId())).isEqualTo(policy);
        assertThat(mappingContext.containsRelationship(
            new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), Person.class, Policy.class))).isFalse();
    }

    /**
     * @see Issue #96
     */
    @Test
    public void clearOneEqualToAnother() {

        Person jim = new Person("jim");
        jim.setId(1L);

        Person another = new Person("jim"); //jim.equals(another)=true
        another.setId(3L);

        Policy policy = new Policy("healthcare");
        policy.setId(2L);

        mappingContext.addNodeEntity(jim);
        mappingContext.addNodeEntity(another);
        mappingContext.addNodeEntity(policy);
        mappingContext.addRelationship(
            new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), Person.class, Policy.class));
        mappingContext.removeEntity(jim);

        assertThat(mappingContext.getNodeEntity(jim.getId())).isEqualTo(null);
        assertThat(mappingContext.getNodeEntity(policy.getId())).isEqualTo(policy);
        assertThat(mappingContext.getNodeEntity(another.getId())).isEqualTo(another);
        assertThat(mappingContext.containsRelationship(
            new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), Person.class, Policy.class))).isFalse();
    }

    @Test
    public void clearType() {
        Person jim = new Person("jim");
        jim.setId(1L);

        Policy healthcare = new Policy("healthcare");
        healthcare.setId(2L);

        Policy immigration = new Policy("immigration");
        immigration.setId(3L);

        Person rik = new Person("rik");
        rik.setId(4L);

        mappingContext.addNodeEntity(jim);
        mappingContext.addNodeEntity(rik);
        mappingContext.addNodeEntity(healthcare);
        mappingContext.addNodeEntity(immigration);

        mappingContext.addRelationship(
            new MappedRelationship(jim.getId(), "INFLUENCES", healthcare.getId(), Person.class, Policy.class));
        mappingContext.addRelationship(
            new MappedRelationship(jim.getId(), "INFLUENCES", immigration.getId(), Person.class, Policy.class));
        mappingContext.addRelationship(
            new MappedRelationship(jim.getId(), "WORKS_WITH", rik.getId(), Person.class, Person.class));

        mappingContext.removeType(Policy.class);

        assertThat(mappingContext.getEntities(Policy.class)).isEmpty();
        assertThat(mappingContext.getNodeEntity(healthcare.getId())).isEqualTo(null);
        assertThat(mappingContext.getNodeEntity(immigration.getId())).isEqualTo(null);

        assertThat(mappingContext.getNodeEntity(jim.getId())).isEqualTo(jim);
        assertThat(mappingContext.getNodeEntity(rik.getId())).isEqualTo(rik);
        assertThat(mappingContext.getRelationships()).hasSize(1);
    }

    @Test
    public void areObjectsReportedAsDirtyCorrectly() {
        Person jim = new Person("jim");
        jim.setId(1L);

        Policy healthcare = new Policy("healthcare");
        healthcare.setId(2L);

        Policy immigration = new Policy("immigration");
        immigration.setId(3L);

        Person rik = new Person("rik");
        rik.setId(4L);

        mappingContext.addNodeEntity(jim);
        mappingContext.addNodeEntity(rik);
        mappingContext.addNodeEntity(healthcare);
        mappingContext.addNodeEntity(immigration);

        rik.setName("newRik");

        assertThat(mappingContext.isDirty(jim)).isFalse();
        assertThat(mappingContext.isDirty(rik)).isTrue();
        assertThat(mappingContext.isDirty(healthcare)).isFalse();
        assertThat(mappingContext.isDirty(immigration)).isFalse();
    }

    /**
     * @see Issue #467
     */
    @Test
    public void nativeIdsAreMappedWithoutPrimaryIdConversion() {
        UuidAndGenerationType entity = new UuidAndGenerationType();

        mappingContext.nativeId(entity);

        assertThat(mappingContext.containsNativeId(metaData.classInfo(entity), entity.identifier)).isTrue();
        assertThat(mappingContext.containsNativeId(metaData.classInfo(entity), new UuidStringConverter().toGraphProperty(entity.identifier))).isFalse();
    }

    /**
     * @see Issue #467
     */
    @Test
    public void nodeEntitiesAreReplacedWithoutPrimaryIdConversion() {
        UuidAndGenerationType entity = new UuidAndGenerationType();

        mappingContext.addNodeEntity(entity);
        Long initialNativeId = mappingContext.nativeId(entity);

        mappingContext.replaceNodeEntity(entity, 999L);

        assertThat(mappingContext.containsNativeId(metaData.classInfo(entity), entity.identifier)).isTrue();
        assertThat(mappingContext.containsNativeId(metaData.classInfo(entity), new UuidStringConverter().toGraphProperty(entity.identifier))).isFalse();

        assertThat(mappingContext.getNodeEntity(999L)).isSameAs(entity);
        assertThat(mappingContext.getNodeEntity(initialNativeId)).isNull();
    }
}
