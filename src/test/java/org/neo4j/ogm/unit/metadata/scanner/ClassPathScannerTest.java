/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.unit.metadata.scanner;


import org.junit.Test;
import org.neo4j.ogm.metadata.ClassPathScanner;
import org.neo4j.ogm.metadata.info.ClassInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Luanne Misquitta
 */
public class ClassPathScannerTest {

	@Test
        public void directoryShouldBeScanned() {
		ClassPathScanner classPathScanner = new ClassPathScanner();
		ClassScanProcessor processor = new ClassScanProcessor();

		classPathScanner.scan(Collections.singletonList("org/neo4j/ogm/domain/bike"), processor);
		assertEquals(5, processor.domainClassInfos.size());

		List<String> classNames = extractClassNames(processor.domainClassInfos);
		assertTrue(classNames.contains("org.neo4j.ogm.domain.bike.Bike"));
		assertTrue(classNames.contains("org.neo4j.ogm.domain.bike.Frame"));
		assertTrue(classNames.contains("org.neo4j.ogm.domain.bike.Saddle"));
		assertTrue(classNames.contains("org.neo4j.ogm.domain.bike.Wheel"));
		assertTrue(classNames.contains("org.neo4j.ogm.domain.bike.WheelWithUUID"));
	}

	@Test
        public void nestedDirectoryShouldBeScanned() {
		ClassPathScanner classPathScanner = new ClassPathScanner();
		ClassScanProcessor processor = new ClassScanProcessor();

		classPathScanner.scan(Collections.singletonList("org/neo4j/ogm/domain/convertible"), processor);
		assertEquals(16, processor.domainClassInfos.size());

		List<String> classNames = extractClassNames(processor.domainClassInfos);
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
	}


	@Test
        public void zipFileWithDomainClassesShouldBeScanned() throws IOException {
		ClassPathScanner classPathScanner = new FileClassPathScanner();
		ClassScanProcessor processor = new ClassScanProcessor();

		classPathScanner.scan(Collections.singletonList("concert/domain"), processor);
		assertEquals(2, processor.domainClassInfos.size());

		List<String> classNames = extractClassNames(processor.domainClassInfos);
		assertTrue(classNames.contains("concert.domain.Concert"));
		assertTrue(classNames.contains("concert.domain.Fan"));

	}

	@Test
        public void domainClassesInNestedZipShouldBeScanned() {
		ClassPathScanner classPathScanner = new FileClassPathScanner();
		ClassScanProcessor processor = new ClassScanProcessor();

		classPathScanner.scan(Collections.singletonList("radio/domain"), processor);
		assertEquals(2, processor.domainClassInfos.size());

		List<String> classNames = extractClassNames(processor.domainClassInfos);
		assertTrue(classNames.contains("radio.domain.Station"));
		assertTrue(classNames.contains("radio.domain.Channel"));

	}

	@Test
	public void domainClassesInDirectoryInNestedZipShouldBeScanned() {
		ClassPathScanner classPathScanner = new FileClassPathScanner();
		ClassScanProcessor processor = new ClassScanProcessor();

		classPathScanner.scan(Collections.singletonList("event/domain"), processor);
		assertEquals(1, processor.domainClassInfos.size());

		List<String> classNames = extractClassNames(processor.domainClassInfos);
		assertTrue(classNames.contains("event.domain.Show"));

	}


	private List<String> extractClassNames(List<ClassInfo> classInfos) {
		List<String> classnames = new ArrayList<>();
		for(ClassInfo classInfo : classInfos) {
			classnames.add(classInfo.name());
		}
		return classnames;
	}
}
