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
package org.neo4j.ogm.metadata;

import static org.assertj.core.api.Assertions.*;

import java.util.Date;

import org.junit.Test;
import org.neo4j.ogm.domain.convertible.enums.Education;
import org.neo4j.ogm.domain.education.School;

/**
 * @author Adam George
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class DescriptorMappingsTest {

    @Test
    public void shouldResolveParameterTypeForSetterMethodFromSignatureString() {
        assertThat(DescriptorMappings.getType("java.util.Date")).isEqualTo(Date.class);
        assertThat(DescriptorMappings.getType("boolean")).isEqualTo(boolean.class);
        assertThat(DescriptorMappings.getType("byte")).isEqualTo(byte.class);
        assertThat(DescriptorMappings.getType("char")).isEqualTo(char.class);
        assertThat(DescriptorMappings.getType("double")).isEqualTo(double.class);
        assertThat(DescriptorMappings.getType("float")).isEqualTo(float.class);
        assertThat(DescriptorMappings.getType("int")).isEqualTo(int.class);
        assertThat(DescriptorMappings.getType("long")).isEqualTo(long.class);
        assertThat(DescriptorMappings.getType("short")).isEqualTo(short.class);
        assertThat(DescriptorMappings.getType("org.neo4j.ogm.domain.education.School")).isEqualTo(School.class);
        assertThat(DescriptorMappings.getType("org.neo4j.ogm.domain.convertible.enums.Education")).isEqualTo(Education.class);
        assertThat(DescriptorMappings.getType("org.neo4j.ogm.domain.convertible.enums.Education[]"))
            .isEqualTo(Education.class);
        assertThat(DescriptorMappings.getType("java.lang.String[]")).isEqualTo(String.class);
    }

    @Test
    public void shouldReturnNullWhenClassCannotBeLoaded() {
        assertThat(DescriptorMappings.getType("org.mozilla.javascript.xml.impl.xmlbeans.XML$XScriptAnnotation"))
            .isEqualTo(null);
    }

    @Test
    public void shouldIdentifyKnownTypes() {
        assertThat(DescriptorMappings.describesPrimitve("char")).isTrue();
        assertThat(DescriptorMappings.describesWrapper("char")).isFalse();
        assertThat(DescriptorMappings.describesPrimitve("java.lang.Character")).isFalse();
        assertThat(DescriptorMappings.describesWrapper("java.lang.Character")).isTrue();
        assertThat(DescriptorMappings.describesPrimitve("java.lang.Object")).isFalse();
        assertThat(DescriptorMappings.describesPrimitve("Object")).isFalse();
        assertThat(DescriptorMappings.describesWrapper("java.lang.Object")).isTrue();
    }
}
