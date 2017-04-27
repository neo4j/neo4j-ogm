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

package org.neo4j.ogm.domain.annotations.ids;

import java.util.UUID;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.typeconversion.UuidStringConverter;

public class ValidAnnotations {

	public static class WithoutId {
		public String identifier;
	}

	public static class Basic {
		public Long id;
		@Id public String identifier;
	}

	public static class IdAndGenerationType {
		public Long id;
		@Id @GeneratedValue(strategy = org.neo4j.ogm.annotation.GenerationType.UUID)
		public String identifier;
	}

	public static class UuidIdAndGenerationType {
		public Long id;
		@Id @GeneratedValue(strategy = org.neo4j.ogm.annotation.GenerationType.UUID)
		@Convert(UuidStringConverter.class)
		public UUID identifier;
	}

	public static class BasicChild extends Basic {
	}
}
