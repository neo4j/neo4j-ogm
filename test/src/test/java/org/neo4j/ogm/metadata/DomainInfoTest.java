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
package org.neo4j.ogm.metadata;

import static org.assertj.core.api.Assertions.*;

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

        assertThat(classInfo).isNotNull();
        assertThat(classInfo.directImplementingClasses()).hasSize(4);
    }

    @Test
    public void testAbstractClassMembership() {

        ClassInfo classInfo = domainInfo.getClassSimpleName("Membership");
        assertThat(classInfo).isNotNull();
        assertThat(classInfo.directInterfaces()).hasSize(1);
    }

    @Test
    public void testConcreteClassSilverMembership() {

        ClassInfo classInfo = domainInfo.getClassSimpleName("SilverMembership");
        assertThat(classInfo).isNotNull();
        assertThat(classInfo.interfacesInfo().list()).hasSize(1);
    }
}
