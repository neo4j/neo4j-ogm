/*
 * Copyright (c) 2002-2023 "Neo4j,"
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
package org.neo4j.ogm.domain.gh932;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.typeconversion.CompositeAttributeConverter;

/**
 * @author Christian Banse
 */
@NodeEntity
public class EntityWithCompositeConverter {

    @Id
    @GeneratedValue
    private Long id;

    @Convert(value = Converter.class)
    private Name name;

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public Long getId() {
        return this.id;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EntityWithCompositeConverter that = (EntityWithCompositeConverter) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    public static class Name {
        private String partialName1;
        private String partialName2;

        public String getPartialName1() {
            return partialName1;
        }

        public String getPartialName2() {
            return partialName2;
        }

        public void setPartialName1(String partialName1) {
            this.partialName1 = partialName1;
        }

        public void setPartialName2(String partialName2) {
            this.partialName2 = partialName2;
        }

        @Override
        public String toString() {
            return partialName1 + "." + partialName2;
        }

        @Override public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Name name = (Name) o;
            return Objects.equals(partialName1, name.partialName1) && Objects.equals(partialName2, name.partialName2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(partialName1, partialName2);
        }
    }

    public static class Converter implements CompositeAttributeConverter<Name> {

        @Override
        public Map<String, ?> toGraphProperties(Name value) {
            // We want to persist the partial properties of the name as well as a "toString" representation of the whole name as "name" (which "conflicts" with the "name" field on the class).
            var map = new HashMap<String, String>();
            map.put("name", value.toString());
            map.put("partialName1", value.partialName1);
            map.put("partialName2", value.partialName2);

            return map;
        }

        @Override
        public Name toEntityAttribute(Map<String, ?> value) {
            var name = new Name();
            name.partialName1 = String.valueOf(value.get("partialName1"));
            name.partialName2 = String.valueOf(value.get("partialName2"));

            return name;
        }
    }
}
