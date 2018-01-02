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
