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

package org.neo4j.ogm.context;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.domain.policy.Person;
import org.neo4j.ogm.domain.policy.Policy;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
public class MappingContextTest {

    private MappingContext mappingContext;

    @Before
    public void setUp() {
        mappingContext = new MappingContext(new MetaData("org.neo4j.ogm.domain.policy", "org.neo4j.ogm.context"));
    }

    @Test
    public void testPath() {

        Person jim = new Person("jim");
        jim.setId(1L);

        Policy policy = new Policy("healthcare");
        policy.setId(2L);

        mappingContext.addNodeEntity(jim, jim.getId());
        mappingContext.addNodeEntity(policy, policy.getId());
        mappingContext.addRelationship(new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), Person.class, Policy.class));

        assertEquals(jim, mappingContext.getNodeEntity(jim.getId()));
        assertEquals(policy, mappingContext.getNodeEntity(policy.getId()));
        assertTrue(mappingContext.containsRelationship(new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), Person.class, Policy.class)));
    }

    @Test
    public void clearOne() {

        Person jim = new Person("jim");
        jim.setId(1L);

        Policy policy = new Policy("healthcare");
        policy.setId(2L);

        mappingContext.addNodeEntity(jim, jim.getId());
        mappingContext.addNodeEntity(policy, policy.getId());
        mappingContext.addRelationship(new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), Person.class, Policy.class));
        mappingContext.removeEntity(jim);

        assertEquals(null, mappingContext.getNodeEntity(jim.getId()));
        assertEquals(policy, mappingContext.getNodeEntity(policy.getId()));
        assertFalse(mappingContext.containsRelationship(new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), Person.class, Policy.class)));
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

        mappingContext.addNodeEntity(jim, jim.getId());
        mappingContext.addNodeEntity(another, another.getId());
        mappingContext.addNodeEntity(policy, policy.getId());
        mappingContext.addRelationship(new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), Person.class, Policy.class));
        mappingContext.removeEntity(jim);

        assertEquals(null, mappingContext.getNodeEntity(jim.getId()));
        assertEquals(policy, mappingContext.getNodeEntity(policy.getId()));
        assertEquals(another, mappingContext.getNodeEntity(another.getId()));
        assertFalse(mappingContext.containsRelationship(new MappedRelationship(jim.getId(), "INFLUENCES", policy.getId(), Person.class, Policy.class)));
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

        mappingContext.addNodeEntity(jim, jim.getId());
        mappingContext.addNodeEntity(rik, rik.getId());
        mappingContext.addNodeEntity(healthcare, healthcare.getId());
        mappingContext.addNodeEntity(immigration, immigration.getId());

        mappingContext.addRelationship(new MappedRelationship(jim.getId(), "INFLUENCES", healthcare.getId(), Person.class, Policy.class));
        mappingContext.addRelationship(new MappedRelationship(jim.getId(), "INFLUENCES", immigration.getId(), Person.class, Policy.class));
        mappingContext.addRelationship(new MappedRelationship(jim.getId(), "WORKS_WITH", rik.getId(), Person.class, Person.class));

        mappingContext.removeType(Policy.class);

        assertEquals(0, mappingContext.getEntities(Policy.class).size());
        assertEquals(null, mappingContext.getNodeEntity(healthcare.getId()));
        assertEquals(null, mappingContext.getNodeEntity(immigration.getId()));

        assertEquals(jim, mappingContext.getNodeEntity(jim.getId()));
        assertEquals(rik, mappingContext.getNodeEntity(rik.getId()));
        assertEquals(1, mappingContext.getRelationships().size());
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

        mappingContext.addNodeEntity(jim, jim.getId());
        mappingContext.addNodeEntity(rik, rik.getId());
        mappingContext.addNodeEntity(healthcare, healthcare.getId());
        mappingContext.addNodeEntity(immigration, immigration.getId());

        rik.setName("newRik");

        assertFalse(mappingContext.isDirty(jim));
        assertTrue(mappingContext.isDirty(rik));
        assertFalse(mappingContext.isDirty(healthcare));
        assertFalse(mappingContext.isDirty(immigration));
    }

}
