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

package org.neo4j.ogm.metadata.scanner;


import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.DomainInfo;
import org.neo4j.ogm.metadata.builder.DomainInfoBuilder;
import org.reflections.util.ClasspathHelper;

/**
 * @author Luanne Misquitta
 */
@Ignore
public class ClassPathScannerTest {

    @Test
    public void directoryShouldBeScanned() {
        final DomainInfo domainInfo = DomainInfoBuilder.create("org.neo4j.ogm.domain.bike");

        assertEquals(5, domainInfo.getClassInfoMap().size());

        Set<String> classNames = domainInfo.getClassInfoMap().keySet();
        assertTrue(classNames.contains("org.neo4j.ogm.domain.bike.Bike"));
        assertTrue(classNames.contains("org.neo4j.ogm.domain.bike.Frame"));
        assertTrue(classNames.contains("org.neo4j.ogm.domain.bike.Saddle"));
        assertTrue(classNames.contains("org.neo4j.ogm.domain.bike.Wheel"));
        assertTrue(classNames.contains("org.neo4j.ogm.domain.bike.WheelWithUUID"));
    }

    @Test
    public void nestedDirectoryShouldBeScanned() {
        final DomainInfo domainInfo = DomainInfoBuilder.create("org.neo4j.ogm.domain.convertible");

        assertEquals(18, domainInfo.getClassInfoMap().size());

        Set<String> classNames = domainInfo.getClassInfoMap().keySet();
        assertTrue(classNames.contains("org.neo4j.ogm.domain.convertible.bytes.Photo"));
        assertTrue(classNames.contains("org.neo4j.ogm.domain.convertible.bytes.PhotoWrapper"));
        assertTrue(classNames.contains("org.neo4j.ogm.domain.convertible.date.DateNumericStringConverter"));
        assertTrue(classNames.contains("org.neo4j.ogm.domain.convertible.date.Memo"));
        assertTrue(classNames.contains("org.neo4j.ogm.domain.convertible.enums.Algebra"));
        assertTrue(classNames.contains("org.neo4j.ogm.domain.convertible.enums.Education"));
        assertTrue(classNames.contains("org.neo4j.ogm.domain.convertible.enums.Gender"));
        assertTrue(classNames.contains("org.neo4j.ogm.domain.convertible.enums.NumberSystem"));
        assertTrue(classNames.contains("org.neo4j.ogm.domain.convertible.enums.Person"));
        assertTrue(classNames.contains("org.neo4j.ogm.domain.convertible.enums.NumberSystemDomainConverter"));
        assertTrue(classNames.contains("org.neo4j.ogm.domain.convertible.enums.Tag"));
        assertTrue(classNames.contains("org.neo4j.ogm.domain.convertible.enums.TagEntity"));
        assertTrue(classNames.contains("org.neo4j.ogm.domain.convertible.enums.TagModel"));
        assertTrue(classNames.contains("org.neo4j.ogm.domain.convertible.numbers.Account"));
        assertTrue(classNames.contains("org.neo4j.ogm.domain.convertible.parametrized.StringMapEntity"));
        assertTrue(classNames.contains("org.neo4j.ogm.domain.convertible.parametrized.StringMapConverter"));
    }


    @Test
    public void zipFileWithDomainClassesShouldBeScanned() throws IOException {
        final DomainInfo domainInfo = DomainInfoBuilder.create("concert.domain");
        assertEquals(2, domainInfo.getClassInfoMap().size());

        Set<String> classNames = domainInfo.getClassInfoMap().keySet();
        assertTrue(classNames.contains("concert.domain.Concert"));
        assertTrue(classNames.contains("concert.domain.Fan"));
    }

    @Test
    public void domainClassesInNestedZipShouldBeScanned() {
        final DomainInfo domainInfo = DomainInfoBuilder.create("radio.domain");
        assertEquals(2, domainInfo.getClassInfoMap().size());

        Set<String> classNames = domainInfo.getClassInfoMap().keySet();
        assertTrue(classNames.contains("radio.domain.Station"));
        assertTrue(classNames.contains("radio.domain.Channel"));
    }

    @Test
    public void domainClassesInDirectoryInNestedZipShouldBeScanned() {
        final DomainInfo domainInfo = DomainInfoBuilder.create("event.domain");
        assertEquals(1, domainInfo.getClassInfoMap().size());

        Set<String> classNames = domainInfo.getClassInfoMap().keySet();
        assertTrue(classNames.contains("event.domain.Show"));
    }


    private List<String> extractClassNames(List<ClassInfo> classInfos) {
        List<String> classnames = new ArrayList<>();
        for (ClassInfo classInfo : classInfos) {
            classnames.add(classInfo.name());
        }
        return classnames;
    }
}
