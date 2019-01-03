/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.ogm.utils;

import static org.assertj.core.api.Assertions.*;

import java.util.Date;

import org.junit.Test;
import org.neo4j.ogm.domain.convertible.enums.Education;
import org.neo4j.ogm.domain.education.School;

/**
 * @author Adam George
 * @author Luanne Misquitta
 */
public class ClassUtilsTest {

    @Test
    public void shouldResolveParameterTypeForSetterMethodFromSignatureString() {
        assertThat(ClassUtils.getType("java.util.Date")).isEqualTo(Date.class);
        assertThat(ClassUtils.getType("boolean")).isEqualTo(boolean.class);
        assertThat(ClassUtils.getType("byte")).isEqualTo(byte.class);
        assertThat(ClassUtils.getType("char")).isEqualTo(char.class);
        assertThat(ClassUtils.getType("double")).isEqualTo(double.class);
        assertThat(ClassUtils.getType("float")).isEqualTo(float.class);
        assertThat(ClassUtils.getType("int")).isEqualTo(int.class);
        assertThat(ClassUtils.getType("long")).isEqualTo(long.class);
        assertThat(ClassUtils.getType("short")).isEqualTo(short.class);
        assertThat(ClassUtils.getType("org.neo4j.ogm.domain.education.School")).isEqualTo(School.class);
        assertThat(ClassUtils.getType("org.neo4j.ogm.domain.convertible.enums.Education")).isEqualTo(Education.class);
        assertThat(ClassUtils.getType("org.neo4j.ogm.domain.convertible.enums.Education[]")).isEqualTo(Education.class);
        assertThat(ClassUtils.getType("java.lang.String[]")).isEqualTo(String.class);
    }

    @Test
    public void shouldReturnNullWhenClassCannotBeLoaded() {
        assertThat(ClassUtils.getType("org.mozilla.javascript.xml.impl.xmlbeans.XML$XScriptAnnotation"))
            .isEqualTo(null);
    }
}
