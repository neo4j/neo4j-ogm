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
public class RelationshipWriterPlainFieldsTest {

    private DomainInfo domainInfo = DomainInfo.create(this.getClass().getPackage().getName());

    @Test
    public void shouldFindWriterForCollection() {

        ClassInfo classInfo = this.domainInfo.getClass(S.class.getName());

        FieldInfo objectAccess = EntityAccessManager
            .getRelationalWriter(classInfo, "LIST", Relationship.Direction.OUTGOING, new T());
        assertThat(objectAccess).as("The resultant object accessor shouldn't be null").isNotNull();
        assertThat(objectAccess instanceof FieldInfo).as("The access mechanism should be via the field").isTrue();
        assertThat(objectAccess.relationship()).isEqualTo("LIST");
    }

    @Test
    public void shouldFindWriterForScalar() {

        ClassInfo classInfo = this.domainInfo.getClass(S.class.getName());

        FieldInfo objectAccess = EntityAccessManager
            .getRelationalWriter(classInfo, "SCALAR", Relationship.Direction.OUTGOING, new T());
        assertThat(objectAccess).as("The resultant object accessor shouldn't be null").isNotNull();
        assertThat(objectAccess instanceof FieldInfo).as("The access mechanism should be via the field").isTrue();
        assertThat(objectAccess.relationship()).isEqualTo("SCALAR");
    }

    @Test
    public void shouldFindWriterForArray() {

        ClassInfo classInfo = this.domainInfo.getClass(S.class.getName());

        FieldInfo objectAccess = EntityAccessManager
            .getRelationalWriter(classInfo, "ARRAY", Relationship.Direction.OUTGOING, new T());
        assertThat(objectAccess).as("The resultant object accessor shouldn't be null").isNotNull();
        assertThat(objectAccess instanceof FieldInfo).as("The access mechanism should be via the field").isTrue();
        assertThat(objectAccess.relationship()).isEqualTo("ARRAY");
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
