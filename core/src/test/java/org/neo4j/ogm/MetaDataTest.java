/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

package org.neo4j.ogm;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.exception.AmbiguousBaseClassException;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.MetaData;

/**
 * @author Vince Bickers
 */
public class MetaDataTest {

    private MetaData metaData;

    @Before
    public void setUp() {
        metaData = new MetaData("org.neo4j.ogm.domain.forum", "org.neo4j.ogm.domain.pizza", "org.neo4j.ogm.metadata", "org.neo4j.ogm.domain.canonical", "org.neo4j.ogm.domain.hierarchy.domain");
    }

    /**
     * A class can be found if its simple name is unique in the domain
     */
    @Test
    public void testClassInfo() {
        assertEquals("org.neo4j.ogm.domain.forum.Topic", metaData.classInfo("Topic").name());
    }

    /**
     * A class can be found via its annotated label
     */
    @Test
    public void testAnnotatedClassInfo() {
        assertEquals("org.neo4j.ogm.domain.forum.Member", metaData.classInfo("User").name());
        assertEquals("org.neo4j.ogm.domain.forum.BronzeMembership", metaData.classInfo("Bronze").name());
    }

    @Test
    public void testCanResolveRelationshipEntityFromRelationshipType() {
        ClassInfo classInfo = metaData.resolve("MEMBER_OF");
        assertNotNull("The resolved class info shouldn't be null", classInfo);
        assertEquals("org.neo4j.ogm.domain.canonical.ArbitraryRelationshipEntity", classInfo.name());
    }

    @Test
    public void testCanResolveClassHierarchies() {
        ClassInfo classInfo = metaData.resolve("Login", "User");
        assertEquals("org.neo4j.ogm.domain.forum.Member", classInfo.name());
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
        assertEquals(null, metaData.resolve("IMembership"));
    }

    @Test
    /**
     * Taxa corresponding to interfaces with a single implementor can be resolved
     *
     * @see DATAGRAPH-577
     */
    public void testInterfaceWithSingleImplTaxa() {
        ClassInfo classInfo = metaData.resolve("AnnotatedInterfaceWithSingleImpl");
        assertNotNull(classInfo);
        assertEquals("org.neo4j.ogm.domain.hierarchy.domain.annotated.AnnotatedChildWithAnnotatedInterface", classInfo.name());
    }

    @Test
    /**
     * Taxa corresponding to abstract classes can't be resolved
     */
    public void testAbstractClassTaxa() {
        assertEquals(null, metaData.resolve("Membership"));
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
        assertEquals("org.neo4j.ogm.domain.forum.BronzeMembership", metaData.resolve("Bronze", "Membership", "IMembership").name());
        assertEquals("org.neo4j.ogm.domain.forum.BronzeMembership", metaData.resolve("Bronze", "IMembership", "Membership").name());
        assertEquals("org.neo4j.ogm.domain.forum.BronzeMembership", metaData.resolve("Membership", "IMembership", "Bronze").name());
        assertEquals("org.neo4j.ogm.domain.forum.BronzeMembership", metaData.resolve("Membership", "Bronze", "IMembership").name());
        assertEquals("org.neo4j.ogm.domain.forum.BronzeMembership", metaData.resolve("IMembership", "Bronze", "Membership").name());
        assertEquals("org.neo4j.ogm.domain.forum.BronzeMembership", metaData.resolve("IMembership", "Membership", "Bronze").name());
    }

    /**
     * @see DATAGRAPH-634
     */
    @Test
    public void testLiskovSubstitutionPrinciple() {
        assertEquals("org.neo4j.ogm.domain.forum.Member", metaData.resolve("Member").name());
        assertEquals("org.neo4j.ogm.domain.forum.Member", metaData.resolve("Login", "Member").name());
        assertEquals("org.neo4j.ogm.domain.forum.Member", metaData.resolve("Login", "Member").name());
        assertEquals("org.neo4j.ogm.domain.forum.Member", metaData.resolve("Member", "Login").name());
    }

    @Test
    /**
     * Taxa not in the domain will be ignored.
     */
    public void testAllNonMemberTaxa() {
        assertEquals(null, metaData.resolve("Knight", "Baronet"));
    }

    @Test
    /**
     * Mixing domain and non-domain taxa is permitted.
     */
    public void testNonMemberAndMemberTaxa() {
        assertEquals("org.neo4j.ogm.domain.forum.SilverMembership", metaData.resolve("Silver", "Pewter", "Tin").name());
    }
}
