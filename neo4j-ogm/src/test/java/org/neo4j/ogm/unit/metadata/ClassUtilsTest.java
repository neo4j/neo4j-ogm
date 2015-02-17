/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.unit.metadata;

import org.junit.Assert;
import org.junit.Test;
import org.neo4j.ogm.metadata.ClassUtils;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class ClassUtilsTest {

    @Test
    public void shouldResolveParameterTypeForSetterMethodFromSignatureString() {
        Assert.assertEquals(Date.class, ClassUtils.getType("(Ljava/util/Date;)V"));
        assertEquals(String[].class, ClassUtils.getType("([Ljava/lang/String;)V"));
        assertEquals(boolean.class, ClassUtils.getType("(Z)V"));
        assertEquals(byte.class, ClassUtils.getType("(B)V"));
        assertEquals(char.class, ClassUtils.getType("(C)V"));
        assertEquals(double.class, ClassUtils.getType("(D)V"));
        assertEquals(float.class, ClassUtils.getType("(F)V"));
        assertEquals(int.class, ClassUtils.getType("(I)V"));
        assertEquals(long.class, ClassUtils.getType("(J)V"));
        assertEquals(short.class, ClassUtils.getType("(S)V"));
    }

}
