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

import org.junit.Test;

/**
 * @author vince
 */
public class PrimitiveFieldsTest extends TestMetaDataTypeResolution {

	@Test
	public void testPrimitiveShortArray() {
		checkField("pss", "[S", short.class);
	}

	@Test
	public void testPrimitiveCharArray() {
		checkField("pcc", "[C", char.class);
	}

	@Test
	public void testPrimitiveByteArray() {
		checkField("pbb", "[B", byte.class);
	}

	@Test
	public void testPrimitiveLongArray() {
		checkField("pll", "[J", long.class);
	}

	@Test
	public void testPrimitiveDoubleArray() {
		checkField("pdd", "[D", double.class);
	}

	@Test
	public void testPrimitiveFloatArray() {
		checkField("pff", "[F", float.class);
	}

	@Test
	public void testPrimitiveBooleanArray() {
		checkField("pzz", "[Z", boolean.class);
	}

	@Test
	public void testPrimitiveIntegerArray() {
		checkField("pii", "[I", int.class);
	}

	@Test
	public void testPrimitiveShort() {
		checkField("ps", "S", short.class);
	}

	@Test
	public void testPrimitiveChar() {
		checkField("pc", "C", char.class);
	}

	@Test
	public void testPrimitiveByte() {
		checkField("pb", "B", byte.class);
	}

	@Test
	public void testPrimitiveLong() {
		checkField("pl", "J", long.class);
	}

	@Test
	public void testPrimitiveDouble() {
		checkField("pd", "D", double.class);
	}

	@Test
	public void testPrimitiveFloat() {
		checkField("pf", "F", float.class);
	}

	@Test
	public void testPrimitiveBoolean() {
		checkField("pz", "Z", boolean.class);
	}

	@Test
	public void testPrimitiveInteger() {
		checkField("pi", "I", int.class);
	}
}
