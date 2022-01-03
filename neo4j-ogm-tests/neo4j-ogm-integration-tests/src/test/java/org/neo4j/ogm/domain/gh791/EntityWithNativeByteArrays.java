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
package org.neo4j.ogm.domain.gh791;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.typeconversion.AttributeConverter;
import org.neo4j.ogm.typeconversion.NoOpByteArrayConverter;
import org.neo4j.ogm.typeconversion.NoOpWrappedByteArrayConverter;

/**
 * @author Michael J. Simons
 */
@NodeEntity
public class EntityWithNativeByteArrays {

    @Id @GeneratedValue Long id;

    @Convert(NoOpByteArrayConverter.class)
    private byte[] primitive;

    @Convert(NoOpWrappedByteArrayConverter.class)
    private Byte[] wrapped;

    @Convert(SomeTupleConverter.class)
    private SomeTuple someTuple;

    public Long getId() {
        return id;
    }

    public byte[] getPrimitive() {
        return primitive;
    }

    public void setPrimitive(byte[] primitive) {
        this.primitive = primitive;
    }

    public Byte[] getWrapped() {
        return wrapped;
    }

    public void setWrapped(Byte[] wrapped) {
        this.wrapped = wrapped;
    }

    public SomeTuple getSomeTuple() {
        return someTuple;
    }

    public void setSomeTuple(SomeTuple someTuple) {
        this.someTuple = someTuple;
    }

    public static class SomeTuple {

        private final String value1;
        private final String value2;

        public SomeTuple(String value1, String value2) {
            this.value1 = value1;
            this.value2 = value2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SomeTuple someTuple = (SomeTuple) o;
            return value1.equals(someTuple.value1) &&
                value2.equals(someTuple.value2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value1, value2);
        }
    }

    public static class SomeTupleConverter implements AttributeConverter<SomeTuple, byte[]> {

        @Override
        public byte[] toGraphProperty(SomeTuple value) {
            if (value == null) {
                return null;
            }
            return (value.value1 + "@" + value.value2).getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public SomeTuple toEntityAttribute(byte[] value) {
            if (value == null) {
                return null;
            }

            String[] tmp = new String(value, StandardCharsets.UTF_8).split("@");

            return new SomeTuple(tmp[0], tmp[1]);
        }
    }
}
