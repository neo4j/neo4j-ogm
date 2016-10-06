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
        assertEquals(Date.class, ClassUtils.getType("(Ljava/util/Date;)V"));
        assertEquals(String[].class, ClassUtils.getType("([Ljava/lang/String;)V"));
        assertEquals(boolean.class, ClassUtils.getType("(Z)V"));
        assertEquals(byte.class, ClassUtils.getType("(B)V"));
        assertEquals(char.class, ClassUtils.getType("(C)V"));
        assertEquals(double.class, ClassUtils.getType("(D)V"));
        assertEquals(float.class, ClassUtils.getType("(F)V"));
        assertEquals(int.class, ClassUtils.getType("(I)V"));
        assertEquals(long.class, ClassUtils.getType("(J)V"));
        assertEquals(short.class, ClassUtils.getType("(S)V"));
        assertEquals(School.class, ClassUtils.getType("()Lorg/neo4j/ogm/domain/education/School;"));
        assertEquals(Education.class, ClassUtils.getType("()[Lorg/neo4j/ogm/domain/convertible/enums/Education;"));
        assertEquals(Education[].class, ClassUtils.getType("([Lorg/neo4j/ogm/domain/convertible/enums/Education;)V"));
    }

    @Test
    public void shouldReturnNullWhenClassCannotBeLoaded() {
        assertEquals(null, ClassUtils.getType("Lorg/mozilla/javascript/xml/impl/xmlbeans/XML$XScriptAnnotation;"));
    }
}
