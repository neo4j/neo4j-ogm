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
package org.neo4j.ogm.domain.convertible.numbers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.neo4j.ogm.typeconversion.AttributeConverter;

/**
 * An abstract base class for converters converting list of things to a string.
 *
 * @param <T> The type of one thing
 * @author Michael J. Simons
 */
abstract class AbstractListConverter<T> implements AttributeConverter<List<T>, String> {

    private final Function<T, String> writeFunction;
    private final Function<String, T> readFunction;

    AbstractListConverter(Function<T, String> writeFunction,
        Function<String, T> readFunction) {
        this.writeFunction = writeFunction;
        this.readFunction = readFunction;
    }

    @Override
    public String toGraphProperty(List<T> value) {
        return value == null ? null : value.stream().map(writeFunction).collect(Collectors.joining(","));
    }

    @Override public List<T> toEntityAttribute(String value) {
        return value == null ? null : Arrays.stream(value.split(",")).map(readFunction).collect(Collectors.toList());
    }

    protected static abstract class AbstractIntegerListConverter extends AbstractListConverter<Integer> {

        AbstractIntegerListConverter(int radix) {
            super(
                i -> Integer.toString(i, radix),
                s -> Integer.valueOf(s, radix)
            );
        }
    }

    public static class Base36NumberConverter extends AbstractIntegerListConverter {

        public Base36NumberConverter() {
            super(36);
        }
    }

    public static class FoobarListConverter extends AbstractListConverter<Foobar> {

        public FoobarListConverter() {
            super(Foobar::getValue, Foobar::new);
        }
    }
}

