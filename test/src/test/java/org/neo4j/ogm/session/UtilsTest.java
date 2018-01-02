/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

package org.neo4j.ogm.session;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

/**
 * @author Luanne Misquitta
 */
public class UtilsTest {

    /**
     * @see Issue #68
     */
    @Test
    public void nullInputObjectsShouldReturnNull() {
        assertThat(Utils.coerceTypes(Integer.class, null)).isNull();
        assertThat(Utils.coerceTypes(Float.class, null)).isNull();
        assertThat(Utils.coerceTypes(Byte.class, null)).isNull();
        assertThat(Utils.coerceTypes(Double.class, null)).isNull();
        assertThat(Utils.coerceTypes(Long.class, null)).isNull();
    }

    /**
     * @see Issue #69
     */
    @Test
    public void nullInputPrimitivesShouldReturnDefaults() {
        assertThat(Utils.coerceTypes(int.class, null)).isEqualTo(0);
        assertThat(Utils.coerceTypes(float.class, null)).isEqualTo(0f);
        assertThat(Utils.coerceTypes(byte.class, null)).isEqualTo(0);
        assertThat(Utils.coerceTypes(double.class, null)).isEqualTo(0.0d);
        assertThat(Utils.coerceTypes(long.class, null)).isEqualTo(0l);
        assertThat(Utils.coerceTypes(short.class, null)).isEqualTo(0);
    }
}
