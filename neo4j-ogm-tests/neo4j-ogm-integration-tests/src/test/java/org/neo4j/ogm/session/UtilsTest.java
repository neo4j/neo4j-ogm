/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
package org.neo4j.ogm.session;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

/**
 * @author Luanne Misquitta
 */
public class UtilsTest {

    @Test // GH-68
    public void nullInputObjectsShouldReturnNull() {
        assertThat(Utils.coerceTypes(Integer.class, null)).isNull();
        assertThat(Utils.coerceTypes(Float.class, null)).isNull();
        assertThat(Utils.coerceTypes(Byte.class, null)).isNull();
        assertThat(Utils.coerceTypes(Double.class, null)).isNull();
        assertThat(Utils.coerceTypes(Long.class, null)).isNull();
    }

    @Test // GH-69
    public void nullInputPrimitivesShouldReturnDefaults() {
        assertThat(Utils.coerceTypes(int.class, null)).isEqualTo(0);
        assertThat(Utils.coerceTypes(float.class, null)).isEqualTo(0f);
        assertThat(Utils.coerceTypes(byte.class, null)).isEqualTo(0);
        assertThat(Utils.coerceTypes(double.class, null)).isEqualTo(0.0d);
        assertThat(Utils.coerceTypes(long.class, null)).isEqualTo(0L);
        assertThat(Utils.coerceTypes(short.class, null)).isEqualTo(0);
    }
}
