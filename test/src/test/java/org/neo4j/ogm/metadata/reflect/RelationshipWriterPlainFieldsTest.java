/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.metadata.reflect;


import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.Test;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.DomainInfo;
import org.neo4j.ogm.metadata.FieldInfo;

/**
 * @author Vince Bickers
 */
public class RelationshipWriterPlainFieldsTest {

    private DomainInfo domainInfo = DomainInfo.create(this.getClass().getPackage().getName());

    @Test
    public void shouldFindWriterForCollection() {

        ClassInfo classInfo = this.domainInfo.getClass(S.class.getName());

        FieldInfo objectAccess = EntityAccessManager.getRelationalWriter(classInfo, "LIST", Relationship.OUTGOING, new T());
        assertThat(objectAccess).as("The resultant object accessor shouldn't be null").isNotNull();
        assertThat(objectAccess instanceof FieldInfo).as("The access mechanism should be via the field").isTrue();
        assertThat(objectAccess.relationshipName()).isEqualTo("LIST");
    }


    @Test
    public void shouldFindWriterForScalar() {

        ClassInfo classInfo = this.domainInfo.getClass(S.class.getName());

        FieldInfo objectAccess = EntityAccessManager.getRelationalWriter(classInfo, "SCALAR", Relationship.OUTGOING, new T());
        assertThat(objectAccess).as("The resultant object accessor shouldn't be null").isNotNull();
        assertThat(objectAccess instanceof FieldInfo).as("The access mechanism should be via the field").isTrue();
        assertThat(objectAccess.relationshipName()).isEqualTo("SCALAR");
    }


    @Test
    public void shouldFindWriterForArray() {

        ClassInfo classInfo = this.domainInfo.getClass(S.class.getName());

        FieldInfo objectAccess = EntityAccessManager.getRelationalWriter(classInfo, "ARRAY", Relationship.OUTGOING, new T());
        assertThat(objectAccess).as("The resultant object accessor shouldn't be null").isNotNull();
        assertThat(objectAccess instanceof FieldInfo).as("The access mechanism should be via the field").isTrue();
        assertThat(objectAccess.relationshipName()).isEqualTo("ARRAY");
    }

    static class S {

        Long id;

        List<T> list;

        T[] array;

        T scalar;
    }

    static class T {

        Long id;
    }
}
