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

package org.neo4j.ogm.metadata.types;

import java.io.Serializable;
import java.util.Map;

import org.junit.Test;

/**
 * @author vince
 */
public class GenericsFieldsTest extends TestMetaDataTypeResolution {

	@Test
	public void testUnboundedGeneric() {
		checkField("genericObject", "Ljava/lang/Object;", Object.class);
	}

	@Test
	public void testGenericComparable() { // from java.lang
		checkField("genericComparable", "Ljava/lang/Comparable;", Comparable.class);
	}

	@Test
	public void testGenericSerializable() { // from java.io
		checkField("genericSerializable", "Ljava/io/Serializable;", Serializable.class);
	}

	@Test
	public void testGenericSelfReference() {
		checkField("next", "Lorg/neo4j/ogm/metadata/types/POJO;", POJO.class);
	}

	@Test // List<S>
	public void testCollectionWithUnboundGenericParameter() {
		checkField("elements", "TS;", Object.class);
	}

	@Test // List<POJO<S, T, U>> neighbours;
	public void testCollectionWithConcreteParameterizedType() {
		checkField("neighbours", "Lorg/neo4j/ogm/metadata/types/POJO<TS;TT;TU;", POJO.class);
	}

	@Test // List<? extends Integer> superIntegers
	public void testCollectionWithExtendedConcreteParameterizedType() {
		checkField("superIntegers", "+Ljava/lang/Integer;", Object.class);
	}

	@Test    // List<? super Integer> subIntegers;
	public void testCollectionWithReducedConcreteParameterizedType() {
		checkField("subIntegers", "-Ljava/lang/Integer;", Object.class);
	}


	@Test    // List<? extends S> superS;
	public void testCollectionOfWildcardExtendingGenericType() {
		checkField("superS", "+TS;", Object.class);
	}

	@Test    // List<? super S> subS;
	public void testCollectionOfWildcardReducingGenericType() {
		checkField("subS", "-TS;", Object.class);
	}

	@Test    // List<?>;
	public void testListGenericWildcard() {
		checkField("listOfAnything", "*", Object.class);
	}

	@Test    // Vector<?>;
	public void testVectorGenericWildcard() {
		checkField("vectorOfAnything", "*", Object.class);
	}

	@Test    // Set<?>;
	public void testSetGenericWildcard() {
		checkField("setOfAnything", "*", Object.class);
	}

	@Test    // Iterable<Map<Class<S>, POJO<S, T, U>>> iterable;
	public void testIterableOfMapOfParameterizedClasses() {
		checkField("iterable", "Ljava/util/Map<Ljava/lang/Class<TS;", Map.class);
	}


}
