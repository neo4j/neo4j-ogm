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

package org.neo4j.ogm.utils;

import static org.junit.Assert.*;

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
        assertEquals(Date.class, ClassUtils.getType("java.util.Date"));
        assertEquals(boolean.class, ClassUtils.getType("boolean"));
        assertEquals(byte.class, ClassUtils.getType("byte"));
        assertEquals(char.class, ClassUtils.getType("char"));
        assertEquals(double.class, ClassUtils.getType("double"));
        assertEquals(float.class, ClassUtils.getType("float"));
        assertEquals(int.class, ClassUtils.getType("int"));
        assertEquals(long.class, ClassUtils.getType("long"));
        assertEquals(short.class, ClassUtils.getType("short"));
        assertEquals(School.class, ClassUtils.getType("org.neo4j.ogm.domain.education.School"));
        assertEquals(Education.class, ClassUtils.getType("org.neo4j.ogm.domain.convertible.enums.Education"));
        assertEquals(Education.class, ClassUtils.getType("org.neo4j.ogm.domain.convertible.enums.Education[]"));
        assertEquals(String.class, ClassUtils.getType("java.lang.String[]"));
    }

    @Test
    public void shouldReturnNullWhenClassCannotBeLoaded() {
        assertEquals(null, ClassUtils.getType("org.mozilla.javascript.xml.impl.xmlbeans.XML$XScriptAnnotation"));
    }
}
