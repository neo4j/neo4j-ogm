/*
 * Copyright (c) 2002-2020 "Neo4j,"
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
import org.neo4j.ogm.domain.gh750.ThingResult3.FooBar;
import org.neo4j.ogm.typeconversion.AttributeConverter;

/**
 * @author Niels Oertel
 * @author Michael J. Simons
 */
public class ThingResult4 {

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

    @Convert(FooBarListConverter.class)
    private List<FooBar> foobar;

    public List<FooBar> getFoobar() {
        return foobar;
    }

    public void setFoobar(List<FooBar> foobar) {
        this.foobar = foobar;
    }
}
