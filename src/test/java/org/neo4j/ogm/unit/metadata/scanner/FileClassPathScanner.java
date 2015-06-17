/*
 * Copyright (c)  [2011-2015] "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */
package org.neo4j.ogm.unit.metadata.scanner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.metadata.ClassPathScanner;

/**
 * For the purpose of testing classpath scanning within jars and to bypass the classload mechanism in tests.
 *
 * @author Luanne Misquitta
 */
public class FileClassPathScanner extends ClassPathScanner {

	@Override
	protected List<File> getUniqueClasspathElements(List<String> classPaths) {
		List<File> jars = new ArrayList<>();
		jars.add(new File(Thread.currentThread().getContextClassLoader().getResource("concert.jar").getFile()));
		jars.add(new File(Thread.currentThread().getContextClassLoader().getResource("radio.jar").getFile()));
		jars.add(new File(Thread.currentThread().getContextClassLoader().getResource("event.jar").getFile()));
		return jars;
	}
}
