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
package org.neo4j.ogm.context;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.annotations.ids.ValidAnnotations.UuidAndGenerationType;
import org.neo4j.ogm.domain.policy.Person;
import org.neo4j.ogm.domain.policy.Policy;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.typeconversion.UuidStringConverter;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 * @author Michael J. Simons
 */
public class MappingContextTest {

    private static Field PRIMARY_ID_TO_NATIVE_ID_ACCESSOR;

    @BeforeClass
    public static void setUpPrimaryIdToNativeIdAccessor() {
        // I wanted to have the test from the original PR but not exposing any new API.
        // This is our stuff, I think it's ok to do this.
        try {
            PRIMARY_ID_TO_NATIVE_ID_ACCESSOR = MappingContext.class.getDeclaredField("primaryIdToNativeId");
            PRIMARY_ID_TO_NATIVE_ID_ACCESSOR.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private MappingContext mappingContext;
    private MetaData metaData;

    @Before
    public void setUp() {

        this.metaData = new MetaData("org.neo4j.ogm.domain.policy", "org.neo4j.ogm.context",
            "org.neo4j.ogm.domain.annotations.ids");
        this.mappingContext = new MappingContext(metaData);
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
            new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), null, Person.class, Policy.class));

        assertThat(mappingContext.getNodeEntity(jim.getId())).isEqualTo(jim);
        assertThat(mappingContext.getNodeEntity(policy.getId())).isEqualTo(policy);
        assertThat(mappingContext.containsRelationship(
            new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), null, Person.class, Policy.class))).isTrue();
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
            new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), null, Person.class, Policy.class));
        mappingContext.removeEntity(jim);

        assertThat(mappingContext.getNodeEntity(jim.getId())).isEqualTo(null);
        assertThat(mappingContext.getNodeEntity(policy.getId())).isEqualTo(policy);
        assertThat(mappingContext.containsRelationship(
            new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), null, Person.class, Policy.class))).isFalse();
    }

    @Test // #96
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
            new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), null, Person.class, Policy.class));
        mappingContext.removeEntity(jim);

        assertThat(mappingContext.getNodeEntity(jim.getId())).isEqualTo(null);
        assertThat(mappingContext.getNodeEntity(policy.getId())).isEqualTo(policy);
        assertThat(mappingContext.getNodeEntity(another.getId())).isEqualTo(another);
        assertThat(mappingContext.containsRelationship(
            new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), null, Person.class, Policy.class))).isFalse();
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
            new MappedRelationship(jim.getId(), "INFLUENCES", healthcare.getId(), null, Person.class, Policy.class));
        mappingContext.addRelationship(
            new MappedRelationship(jim.getId(), "INFLUENCES", immigration.getId(), null, Person.class, Policy.class));
        mappingContext.addRelationship(
            new MappedRelationship(jim.getId(), "WORKS_WITH", rik.getId(), null, Person.class, Person.class));

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

    @Test // See #467
    public void nativeIdsAreMappedWithoutPrimaryIdConversion() {
        UuidAndGenerationType entity = new UuidAndGenerationType();

        mappingContext.nativeId(entity);

        assertThat(containsNativeId(metaData.classInfo(entity), entity.identifier)).isTrue();
        assertThat(
            containsNativeId(metaData.classInfo(entity), new UuidStringConverter().toGraphProperty(entity.identifier)))
            .isFalse();
    }

    @Test // See #467
    public void nodeEntitiesAreReplacedWithoutPrimaryIdConversion() {
        UuidAndGenerationType entity = new UuidAndGenerationType();

        mappingContext.addNodeEntity(entity);
        Long initialNativeId = mappingContext.nativeId(entity);

        mappingContext.replaceNodeEntity(entity, 999L);

        assertThat(containsNativeId(metaData.classInfo(entity), entity.identifier)).isTrue();
        assertThat(
            containsNativeId(metaData.classInfo(entity), new UuidStringConverter().toGraphProperty(entity.identifier)))
            .isFalse();

        assertThat(mappingContext.getNodeEntity(999L)).isSameAs(entity);
        assertThat(mappingContext.getNodeEntity(initialNativeId)).isNull();
    }

    /**
     * Check if the context contains the nativeId for an entity
     *
     * @param classInfo classInfo of the relationship entity (it is needed to because primary id may not be unique
     *                  across all relationship types)
     * @param id        primary id of the entity
     * @return True if nativeId is already in the context
     */
    private boolean containsNativeId(ClassInfo classInfo, Object id) {
        try {
            return ((Map) PRIMARY_ID_TO_NATIVE_ID_ACCESSOR.get(this.mappingContext))
                .containsKey(new LabelPrimaryId(classInfo, id));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
