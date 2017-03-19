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
package org.neo4j.ogm.metadata;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Vince Bickers
 * @see DATAGRAPH-590 - Metadata resolves to an abstract class for an interface
 */
public class DomainInfoTest {

    private DomainInfo domainInfo;

    @Before
    public void setUp() {
        domainInfo = DomainInfo.create("org.neo4j.ogm.domain.forum");
    }


    @Test
    public void testInterfaceClassIMembership() {

        ClassInfo classInfo = domainInfo.getClassSimpleName("IMembership");

        assertNotNull(classInfo);

        for (ClassInfo implementingClass : classInfo.directImplementingClasses()) {
            System.out.println(implementingClass.name());
        }
        assertEquals(4, classInfo.directImplementingClasses().size());
    }

    @Test
    public void testAbstractClassMembership() {

        ClassInfo classInfo = domainInfo.getClassSimpleName("Membership");
        assertNotNull(classInfo);
        assertEquals(1, classInfo.directInterfaces().size());
    }

    @Test
    public void testConcreteClassSilverMembership() {

        ClassInfo classInfo = domainInfo.getClassSimpleName("SilverMembership");
        assertNotNull(classInfo);
        assertEquals(1, classInfo.interfacesInfo().list().size());
    }
}
