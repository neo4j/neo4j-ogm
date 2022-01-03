/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
import org.neo4j.ogm.domain.gh809.package1.TestEntity;

/**
 * see DATAGRAPH-590 - Metadata resolves to an abstract class for an interface
 *
 * @author Vince Bickers
 * @author Michael J. Simons
 */
public class DomainInfoTest {

    private DomainInfo domainInfo;

    @Before
    public void setUp() {
        domainInfo = DomainInfo.create(
            "org.neo4j.ogm.domain.forum",
            "org.neo4j.ogm.domain.gh806",
            "org.neo4j.ogm.domain.gh809");
    }

    @Test
    public void testInterfaceClassIMembership() {

        ClassInfo classInfo = domainInfo.getClassSimpleName("IMembership");

        assertThat(classInfo).isNotNull();
        assertThat(classInfo.directImplementingClasses()).hasSize(5);
    }

    @Test // GH-806
    public void shouldFindAllSubclasses() {

        ClassInfo classInfo = domainInfo.getClassSimpleName("Element");

        assertThat(classInfo.allSubclasses())
            .containsExactlyInAnyOrder(
                domainInfo.getClassSimpleName("ConcreteElement"),
                domainInfo.getClassSimpleName("VeryConcreteElementA"),
                domainInfo.getClassSimpleName("VeryConcreteElementB"),
                domainInfo.getClassSimpleName("EvenMoreConcreteElement")
            );
    }

    @Test // GH-806
    public void shouldFindAllImplementingClasses() {

        ClassInfo classInfo = domainInfo.getClassSimpleName("IElement");

        assertThat(classInfo.allSubclasses())
            .containsExactlyInAnyOrder(
                domainInfo.getClassSimpleName("IElementImpl1"),
                domainInfo.getClassSimpleName("IElementImpl1A")
            );
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

    @Test
    public void shouldCorrectlyFindMultipleClassesWithSimpleNameIfAnnotated() {
        ClassInfo classInfo = domainInfo.getClassSimpleName("TestEntity");
        assertThat(classInfo.getUnderlyingClass()).isSameAs(TestEntity.class);
    }
}
