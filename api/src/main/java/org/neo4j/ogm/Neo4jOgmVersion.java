/*
 * Copyright (c) 2002-2023 "Neo4j,"
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
package org.neo4j.ogm;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.neo4j.ogm.driver.Driver;

/**
 * Utility class to retrieve the version of the core module aka the product version.
 *
 * @author Michael J. Simons
 */
public final class Neo4jOgmVersion {

	private static volatile String value;

	public static String resolve() {

		String computedVersion = value;
		if (computedVersion == null) {
			synchronized (Neo4jOgmVersion.class) {
				computedVersion = value;
				if (computedVersion == null) {
					value = getVersionImpl();
					computedVersion = value;
				}
			}
		}
		return computedVersion;
	}

	private static String getVersionImpl() {
		try {
			Enumeration<URL> resources = Driver.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
			while (resources.hasMoreElements()) {
				URL url = resources.nextElement();
				Manifest manifest = new Manifest(url.openStream());
				if (isApplicableManifest(manifest)) {
					Attributes attr = manifest.getMainAttributes();
					return get(attr, "Implementation-Version").toString();
				}
			}
		} catch (IOException ex) {
			throw new UncheckedIOException("Unable to read from neo4j-ogm-core manifest.", ex);
		}

		return "unknown";
	}

	private static boolean isApplicableManifest(Manifest manifest) {
		Attributes attributes = manifest.getMainAttributes();
		return "Neo4j-OGM Api".equals(get(attributes, "Implementation-Title"));
	}

	private static Object get(Attributes attributes, String key) {
		return attributes.get(new Attributes.Name(key));
	}

	private Neo4jOgmVersion() {
	}
}
