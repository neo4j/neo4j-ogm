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

import org.junit.Test;

/**
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class ClassPathScannerTest {

    @Test
    public void directoryShouldBeScanned() {

        final DomainInfo domainInfo = DomainInfo.create("org.neo4j.ogm.domain.bike");
        assertThat(domainInfo.getClassInfoMap()).containsOnlyKeys(
            "org.neo4j.ogm.domain.bike.Bike",
            "org.neo4j.ogm.domain.bike.Frame",
            "org.neo4j.ogm.domain.bike.Saddle",
            "org.neo4j.ogm.domain.bike.Wheel",
            "org.neo4j.ogm.domain.bike.WheelWithUUID");
    }

    @Test
    public void nestedDirectoryShouldBeScanned() {

        final DomainInfo domainInfo = DomainInfo.create("org.neo4j.ogm.domain.convertible");
        assertThat(domainInfo.getClassInfoMap()).containsOnlyKeys(
            "org.neo4j.ogm.domain.convertible.bytes.Photo",
            "org.neo4j.ogm.domain.convertible.bytes.PhotoWrapper",
            "org.neo4j.ogm.domain.convertible.date.DateNumericStringConverter",
            "org.neo4j.ogm.domain.convertible.date.Memo",
            "org.neo4j.ogm.domain.convertible.date.Java8DatesMemo",
            "org.neo4j.ogm.domain.convertible.enums.Algebra",
            "org.neo4j.ogm.domain.convertible.enums.Education",
            "org.neo4j.ogm.domain.convertible.enums.Gender",
            "org.neo4j.ogm.domain.convertible.enums.NumberSystem",
            "org.neo4j.ogm.domain.convertible.enums.NumberSystemDomainConverter",
            "org.neo4j.ogm.domain.convertible.enums.Operation",
            "org.neo4j.ogm.domain.convertible.enums.Person",
            "org.neo4j.ogm.domain.convertible.enums.Tag",
            "org.neo4j.ogm.domain.convertible.enums.TagEntity",
            "org.neo4j.ogm.domain.convertible.enums.TagModel",
            "org.neo4j.ogm.domain.convertible.numbers.Account",
            "org.neo4j.ogm.domain.convertible.parametrized.JsonNode",
            "org.neo4j.ogm.domain.convertible.parametrized.MapJson",
            "org.neo4j.ogm.domain.convertible.parametrized.StringMapEntity",
            "org.neo4j.ogm.domain.convertible.parametrized.StringMapConverter",
            "org.neo4j.ogm.domain.convertible.numbers.AbstractListConverter",
            "org.neo4j.ogm.domain.convertible.numbers.AbstractListConverter$AbstractIntegerListConverter",
            "org.neo4j.ogm.domain.convertible.numbers.AbstractListConverter$Base36NumberConverter",
            "org.neo4j.ogm.domain.convertible.numbers.AbstractListConverter$FoobarListConverter",
            "org.neo4j.ogm.domain.convertible.numbers.Foobar",
            "org.neo4j.ogm.domain.convertible.numbers.FoobarListConverter",
            "org.neo4j.ogm.domain.convertible.numbers.FoobarConverter",
            "org.neo4j.ogm.domain.convertible.numbers.HexadecimalNumberConverter");
    }

    @Test
    public void zipFileWithDomainClassesShouldBeScanned() {

        final DomainInfo domainInfo = DomainInfo.create("concert.domain");
        assertThat(domainInfo.getClassInfoMap()).containsOnlyKeys(
            "concert.domain.Concert",
            "concert.domain.Fan");
    }
}
