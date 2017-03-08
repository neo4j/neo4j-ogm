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
public class AutoboxedFieldsTest extends TestMetaDataTypeResolution {

	@Test
	public void testObjectArray() {
		checkField("oo", "java.lang.Object[]", Object.class);
	}

	@Test
	public void testStringArray () {
		checkField("xx", "java.lang.String[]", String.class);
	}

	@Test
	public void testShortArray() {
		checkField("ss", "java.lang.Short[]", Short.class);
	}

	@Test
	public void testCharacterArray() {
		checkField("cc", "java.lang.Character[]", Character.class);
	}

	@Test
	public void testByteArray() {
		checkField("bb", "java.lang.Byte[]", Byte.class);
	}

	@Test
	public void testLongArray() {
		checkField("ll", "java.lang.Long[]", Long.class);
	}

	@Test
	public void testDoubleArray() {
		checkField("dd", "java.lang.Double[]", Double.class);
	}

	@Test
	public void testFloatArray() {
		checkField("ff", "java.lang.Float[]", Float.class);
	}

	@Test
	public void testBooleanArray() {
		checkField("zz", "java.lang.Boolean[]", Boolean.class);
	}

	@Test
	public void testIntegerArray() {
		checkField("ii", "java.lang.Integer[]", Integer.class);
	}

	@Test
	public void testObject() {
		checkField("o", "java.lang.Object", Object.class);
	}

	@Test
	public void testString() {
		checkField("x", "java.lang.String", String.class);
	}

	@Test
	public void testShort() {
		checkField("s", "java.lang.Short", Short.class);
	}

	@Test
	public void testCharacter() {
		checkField("c", "java.lang.Character", Character.class);
	}

	@Test
	public void testByte() {
		checkField("b", "java.lang.Byte", Byte.class);
	}

	@Test
	public void testLong() {
		checkField("l", "java.lang.Long", Long.class);
	}

	@Test
	public void testDouble() {
		checkField("d", "java.lang.Double", Double.class);
	}

	@Test
	public void testFloat() {
		checkField("f", "java.lang.Float", Float.class);
	}

	@Test
	public void testBoolean() {
		checkField("z", "java.lang.Boolean", Boolean.class);
	}

	@Test
	public void testInteger() {
		checkField("i", "java.lang.Integer", Integer.class);
	}

}
