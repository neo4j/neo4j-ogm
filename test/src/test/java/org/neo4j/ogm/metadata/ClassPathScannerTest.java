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

import java.io.IOException;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Luanne Misquitta
 */
public class ClassPathScannerTest {

    @Test
    public void directoryShouldBeScanned() {
        final DomainInfo domainInfo = DomainInfo.create("org.neo4j.ogm.domain.bike");

        assertThat(domainInfo.getClassInfoMap()).hasSize(5);

        Set<String> classNames = domainInfo.getClassInfoMap().keySet();
        assertThat(classNames.contains("org.neo4j.ogm.domain.bike.Bike")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.bike.Frame")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.bike.Saddle")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.bike.Wheel")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.bike.WheelWithUUID")).isTrue();
    }

    @Test
    public void nestedDirectoryShouldBeScanned() {
        final DomainInfo domainInfo = DomainInfo.create("org.neo4j.ogm.domain.convertible");

        assertThat(domainInfo.getClassInfoMap()).hasSize(21);

        Set<String> classNames = domainInfo.getClassInfoMap().keySet();
        assertThat(classNames.contains("org.neo4j.ogm.domain.convertible.bytes.Photo")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.convertible.bytes.PhotoWrapper")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.convertible.date.DateNumericStringConverter")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.convertible.date.Memo")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.convertible.date.Java8DatesMemo")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.convertible.enums.Algebra")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.convertible.enums.Education")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.convertible.enums.Gender")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.convertible.enums.NumberSystem")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.convertible.enums.NumberSystemDomainConverter")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.convertible.enums.Operation")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.convertible.enums.Person")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.convertible.enums.Tag")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.convertible.enums.TagEntity")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.convertible.enums.TagModel")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.convertible.numbers.Account")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.convertible.parametrized.JsonNode")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.convertible.parametrized.MapJson")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.convertible.parametrized.StringMapEntity")).isTrue();
        assertThat(classNames.contains("org.neo4j.ogm.domain.convertible.parametrized.StringMapConverter")).isTrue();
    }

    @Test
    public void zipFileWithDomainClassesShouldBeScanned() throws IOException {
        final DomainInfo domainInfo = DomainInfo.create("concert.domain");
        assertThat(domainInfo.getClassInfoMap()).hasSize(2);

        Set<String> classNames = domainInfo.getClassInfoMap().keySet();
        assertThat(classNames.contains("concert.domain.Concert")).isTrue();
        assertThat(classNames.contains("concert.domain.Fan")).isTrue();
    }

    @Test
    @Ignore("Work with Luke to see what needs to happen here.")
    public void domainClassesInNestedZipShouldBeScanned() {
        final DomainInfo domainInfo = DomainInfo.create("radio.domain");
        assertThat(domainInfo.getClassInfoMap()).hasSize(2);

        Set<String> classNames = domainInfo.getClassInfoMap().keySet();
        assertThat(classNames.contains("radio.domain.Station")).isTrue();
        assertThat(classNames.contains("radio.domain.Channel")).isTrue();
    }

    @Test
    @Ignore("Work with Luke to see what needs to happen here.")
    public void domainClassesInDirectoryInNestedZipShouldBeScanned() {
        final DomainInfo domainInfo = DomainInfo.create("event.domain");
        assertThat(domainInfo.getClassInfoMap()).hasSize(1);

        Set<String> classNames = domainInfo.getClassInfoMap().keySet();
        assertThat(classNames.contains("event.domain.Show")).isTrue();
    }
}
