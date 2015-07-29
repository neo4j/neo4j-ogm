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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import org.neo4j.ogm.metadata.ClassPathScanner;

/**
 * For the purpose of testing classpath scanning within jars and to bypass the classload mechanism in tests.
 * @author Luanne Misquitta
 */
public class FileClassPathScanner extends ClassPathScanner {

	@Override
	protected Set<File> getUniqueClasspathElements(List<String> classPaths)  {
		Set<File> jars = new HashSet<>();
		URL concert =FileClassPathScanner.class.getClassLoader().getResource("concert.jar");
		URL radio =FileClassPathScanner.class.getClassLoader().getResource("radio.jar");
		URL event =FileClassPathScanner.class.getClassLoader().getResource("event.jar");

		try {
			addFile(concert,jars);
			addFile(radio,jars);
			addFile(event,jars);
			/*jars.add(new File(concert.toURI()));
			jars.add(new File(radio.toURI()));
			jars.add(new File(event.toURI()));*/
		} catch (URISyntaxException e) {
			throw new RuntimeException("Failed to read test jars", e);
		}
		return jars;
	}

	private void addFile(URL url, Set<File> jars) throws URISyntaxException {
		if(url.getProtocol().equals("file")) {
			jars.add(new File(url.toURI()));
		}
		else if(url.getProtocol().equals("jar")) {
			String jarPath = url.getPath().substring(5, url.getPath().indexOf("!"));  //Strip out the jar protocol
			jars.add(new File(jarPath));

		}
	}
}
