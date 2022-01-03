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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.domain.gh750;

import static java.util.stream.Collectors.*;

import java.util.List;

import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.typeconversion.AttributeConverter;

/**
 * @author Niels Oertel
 * @author Michael J. Simons
 */
public class ThingResult3 {

    public static class FooBar {

        private String value;

        public FooBar(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class FooBarConverter implements AttributeConverter<FooBar, String> {

        @Override
        public String toGraphProperty(FooBar foobar) {
            if (null == foobar) {
                return null;
            } else {
                return foobar.getValue();
            }
        }

        @Override
        public FooBar toEntityAttribute(String value) {
            return value == null ? null : new FooBar(value);
        }
    }

    public static class FooBarListConverter implements AttributeConverter<List<FooBar>, List<String>> {

        @Override
        public List<String> toGraphProperty(List<FooBar> foobar) {
            return foobar == null ? null : foobar.stream().map(FooBar::getValue).collect(toList());
        }

        @Override
        public List<FooBar> toEntityAttribute(List<String> value) {
            return value == null ? null : value.stream().map(FooBar::new).collect(toList());
        }
    }

    @Convert(FooBarConverter.class)
    private FooBar foobar;

    @Convert(FooBarListConverter.class)
    private List<FooBar> foobars;

    public FooBar getFoobar() {
        return foobar;
    }

    public void setFoobar(FooBar foobar) {
        this.foobar = foobar;
    }

    public List<FooBar> getFoobars() {
        return foobars;
    }

    public void setFoobars(List<FooBar> foobars) {
        this.foobars = foobars;
    }
}
