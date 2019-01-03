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
package org.neo4j.ogm.metadata;

import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.exception.core.AmbiguousBaseClassException;

/**
 * @author Vince Bickers
 */
public class MetaDataTest {

    private MetaData metaData;

    @Before
    public void setUp() {
        metaData = new MetaData("org.neo4j.ogm.domain.forum", "org.neo4j.ogm.domain.pizza", "org.neo4j.ogm.metadata",
            "org.neo4j.ogm.domain.canonical", "org.neo4j.ogm.domain.hierarchy.domain");
    }

    /**
     * A class can be found if its simple name is unique in the domain
     */
    @Test
    public void testClassInfo() {
        assertThat(metaData.classInfo("Topic").name()).isEqualTo("org.neo4j.ogm.domain.forum.Topic");
    }

    /**
     * A class can be found via its annotated label
     */
    @Test
    public void testAnnotatedClassInfo() {
        assertThat(metaData.classInfo("User").name()).isEqualTo("org.neo4j.ogm.domain.forum.Member");
        assertThat(metaData.classInfo("Bronze").name()).isEqualTo("org.neo4j.ogm.domain.forum.BronzeMembership");
    }

    @Test
    public void testCanResolveRelationshipEntityFromRelationshipType() {
        ClassInfo classInfo = metaData.resolve("MEMBER_OF");
        assertThat(classInfo).as("The resolved class info shouldn't be null").isNotNull();
        assertThat(classInfo.name()).isEqualTo("org.neo4j.ogm.domain.canonical.ArbitraryRelationshipEntity");
    }

    @Test
    public void testCanResolveClassHierarchies() {
        ClassInfo classInfo = metaData.resolve("Login", "User");
        assertThat(classInfo.name()).isEqualTo("org.neo4j.ogm.domain.forum.Member");
    }

    @Test(expected = AmbiguousBaseClassException.class)
    public void testCannotResolveInconsistentClassHierarchies() {
        metaData.resolve("Login", "Topic");
    }

    @Test
    /**
     * Taxa corresponding to interfaces with multiple implementations can't be resolved
     */
    public void testInterfaceWithMultipleImplTaxa() {
        assertThat(metaData.resolve("IMembership")).isEqualTo(null);
    }

    @Test
    /**
     * Taxa corresponding to interfaces with a single implementor can be resolved
     *
     * @see DATAGRAPH-577
     */
    public void testInterfaceWithSingleImplTaxa() {
        ClassInfo classInfo = metaData.resolve("AnnotatedInterfaceWithSingleImpl");
        assertThat(classInfo).isNotNull();
        assertThat(classInfo.name())
            .isEqualTo("org.neo4j.ogm.domain.hierarchy.domain.annotated.AnnotatedChildWithAnnotatedInterface");
    }

    @Test
    /**
     * Taxa corresponding to abstract classes can't be resolved
     */
    public void testAbstractClassTaxa() {
        assertThat(metaData.resolve("Membership")).isEqualTo(null);
    }

    @Test(expected = AmbiguousBaseClassException.class)
    /**
     * Taxa not forming a class hierarchy cannot be resolved.
     */
    public void testNoCommonLeafInTaxa() {
        metaData.resolve("Topic", "Member");
    }

    @Test
    /**
     * The ordering of taxa is unimportant.
     */
    public void testOrderingOfTaxaIsUnimportant() {
        assertThat(metaData.resolve("Bronze", "Membership", "IMembership").name())
            .isEqualTo("org.neo4j.ogm.domain.forum.BronzeMembership");
        assertThat(metaData.resolve("Bronze", "IMembership", "Membership").name())
            .isEqualTo("org.neo4j.ogm.domain.forum.BronzeMembership");
        assertThat(metaData.resolve("Membership", "IMembership", "Bronze").name())
            .isEqualTo("org.neo4j.ogm.domain.forum.BronzeMembership");
        assertThat(metaData.resolve("Membership", "Bronze", "IMembership").name())
            .isEqualTo("org.neo4j.ogm.domain.forum.BronzeMembership");
        assertThat(metaData.resolve("IMembership", "Bronze", "Membership").name())
            .isEqualTo("org.neo4j.ogm.domain.forum.BronzeMembership");
        assertThat(metaData.resolve("IMembership", "Membership", "Bronze").name())
            .isEqualTo("org.neo4j.ogm.domain.forum.BronzeMembership");
    }

    /**
     * @see DATAGRAPH-634
     */
    @Test
    public void testLiskovSubstitutionPrinciple() {
        assertThat(metaData.resolve("Member").name()).isEqualTo("org.neo4j.ogm.domain.forum.Member");
        assertThat(metaData.resolve("Login", "Member").name()).isEqualTo("org.neo4j.ogm.domain.forum.Member");
        assertThat(metaData.resolve("Login", "Member").name()).isEqualTo("org.neo4j.ogm.domain.forum.Member");
        assertThat(metaData.resolve("Member", "Login").name()).isEqualTo("org.neo4j.ogm.domain.forum.Member");
    }

    @Test
    /**
     * Taxa not in the domain will be ignored.
     */
    public void testAllNonMemberTaxa() {
        assertThat(metaData.resolve("Knight", "Baronet")).isNull();
    }

    @Test
    /**
     * Mixing domain and non-domain taxa is permitted.
     */
    public void testNonMemberAndMemberTaxa() {
        assertThat(metaData.resolve("Silver", "Pewter", "Tin").name())
            .isEqualTo("org.neo4j.ogm.domain.forum.SilverMembership");
    }
}
