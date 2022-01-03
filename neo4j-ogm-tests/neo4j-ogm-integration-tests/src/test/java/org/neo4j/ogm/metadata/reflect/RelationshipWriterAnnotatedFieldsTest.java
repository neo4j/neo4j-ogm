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
package org.neo4j.ogm.metadata.reflect;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.DomainInfo;
import org.neo4j.ogm.metadata.FieldInfo;

/**
 * @author Vince Bickers
 * @author Michael J. Simons
 */
public class RelationshipWriterAnnotatedFieldsTest {

    private DomainInfo domainInfo = DomainInfo.create(this.getClass().getPackage().getName());

    @Test
    public void shouldFindWriterForCollection() {

        ClassInfo classInfo = this.domainInfo.getClass(S.class.getName());

        FieldInfo objectAccess = EntityAccessManager
            .getRelationalWriter(classInfo, "LIST", Relationship.Direction.OUTGOING, new T());
        assertThat(objectAccess).as("The resultant object accessor shouldn't be null").isNotNull();
        assertThat(objectAccess instanceof FieldInfo).as("The access mechanism should be via the field").isTrue();
        assertThat(objectAccess.relationship()).isEqualTo("LIST");
        assertThat(objectAccess.type()).isEqualTo(List.class);
    }

    @Test
    public void shouldFindWriterForScalar() {

        ClassInfo classInfo = this.domainInfo.getClass(S.class.getName());

        FieldInfo objectAccess = EntityAccessManager
            .getRelationalWriter(classInfo, "SCALAR", Relationship.Direction.OUTGOING, new T());
        assertThat(objectAccess).as("The resultant object accessor shouldn't be null").isNotNull();
        assertThat(objectAccess instanceof FieldInfo).as("The access mechanism should be via the field").isTrue();
        assertThat(objectAccess.relationship()).isEqualTo("SCALAR");
        assertThat(objectAccess.type()).isEqualTo(T.class);
    }

    @Test
    public void shouldFindWriterForArray() {

        ClassInfo classInfo = this.domainInfo.getClass(S.class.getName());

        FieldInfo objectAccess = EntityAccessManager
            .getRelationalWriter(classInfo, "ARRAY", Relationship.Direction.OUTGOING, new T());
        assertThat(objectAccess).as("The resultant object accessor shouldn't be null").isNotNull();
        assertThat(objectAccess instanceof FieldInfo).as("The access mechanism should be via the field").isTrue();
        assertThat(objectAccess.relationship()).isEqualTo("ARRAY");
        assertThat(objectAccess.type()).isEqualTo(T[].class);
    }

    private Class getGenericType(Collection<?> collection) {

        // if we have an object in the collection, use that to determine the type
        if (!collection.isEmpty()) {
            return collection.iterator().next().getClass();
        }

        // otherwise, see if the collection is an anonymous class wrapper
        // new List<T>(){}
        // which does not remove runtime type information

        Class klazz = collection.getClass();

        // obtain anonymous , if any, class for 'this' instance
        final Type superclass = klazz.getGenericSuperclass();

        // obtain Runtime type info of first parameter
        try {
            ParameterizedType parameterizedType = (ParameterizedType) superclass;
            Type[] types = parameterizedType.getActualTypeArguments();
            return (Class) types[0];
        } catch (Exception e) {
            // we can't handle this collection type.
            return null;
        }
    }

    static class S {

        Long id;

        @Relationship(type = "LIST", direction = Relationship.Direction.OUTGOING)
        List<T> list;

        @Relationship(type = "ARRAY", direction = Relationship.Direction.OUTGOING)
        T[] array;

        @Relationship(type = "SCALAR", direction = Relationship.Direction.OUTGOING)
        T scalar;
    }

    static class T {

        Long id;
    }
}
