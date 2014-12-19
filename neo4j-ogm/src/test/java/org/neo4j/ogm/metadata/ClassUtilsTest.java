package org.neo4j.ogm.metadata;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

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
    }

}
