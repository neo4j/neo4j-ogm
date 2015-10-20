/*
 * Copyright (c)  [2011-2015] "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with separate copyright notices and license terms. Your use of the source code for these subcomponents is subject to the terms and conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 *
 */

package org.neo4j.ogm.core.session;

import static org.junit.Assert.*;

import org.junit.Test;
import org.neo4j.ogm.session.Utils;

/**
 * @author Luanne Misquitta
 */
public class UtilsTest {

	/**
	 * @see Issue #68
	 */
	@Test
	public void nullInputObjectsShouldReturnNull() {
		assertNull(Utils.coerceTypes(Integer.class,null));
		assertNull(Utils.coerceTypes(Float.class,null));
		assertNull(Utils.coerceTypes(Byte.class,null));
		assertNull(Utils.coerceTypes(Double.class,null));
		assertNull(Utils.coerceTypes(Long.class,null));
	}

	/**
	 * @see Issue #69
	 */
	@Test
	public void nullInputPrimitivesShouldReturnDefaults() {
		assertEquals(0,Utils.coerceTypes(int.class,null));
		assertEquals(0f,Utils.coerceTypes(float.class,null));
		assertEquals(0,Utils.coerceTypes(byte.class,null));
		assertEquals(0.0d,Utils.coerceTypes(double.class,null));
		assertEquals(0l,Utils.coerceTypes(long.class,null));
		assertEquals(0, Utils.coerceTypes(short.class,null));
	}


}
