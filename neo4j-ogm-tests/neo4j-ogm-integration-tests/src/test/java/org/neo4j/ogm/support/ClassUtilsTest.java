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
package org.neo4j.ogm.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * @author Michael J. Simons
 */
public class ClassUtilsTest {

    enum YourFriendlyEnumMostPeopleUse {
        THING1, THING2
    }

    enum YesEnumsCanBeMuchMore {
        THING1, THING2 {
            @Override
            public void something() {
            }
        };

        @SuppressWarnings({ "unused" })
        void something() {
        }
    }

    interface SomeInterface {
    }

    enum Singleton implements SomeInterface {
        THE_INSTANCE
    }

    @Test
    public void shouldWorkWithPlainEnum() {
        assertThat(ClassUtils.isEnum(YourFriendlyEnumMostPeopleUse.class)).isTrue();
        assertThat(ClassUtils.isEnum(YourFriendlyEnumMostPeopleUse.THING1)).isTrue();
        assertThat(ClassUtils.isEnum(YourFriendlyEnumMostPeopleUse.THING2)).isTrue();
    }

    @Test
    public void shouldWorkWithExtendedEnum() {
        assertThat(ClassUtils.isEnum(YesEnumsCanBeMuchMore.class)).isTrue();
        assertThat(ClassUtils.isEnum(YesEnumsCanBeMuchMore.THING1)).isTrue();
        assertThat(ClassUtils.isEnum(YesEnumsCanBeMuchMore.THING2)).isTrue();
    }

    @Test
    public void shouldWorkWithInterfaceEnum() {
        assertThat(ClassUtils.isEnum(Singleton.class)).isTrue();
        assertThat(ClassUtils.isEnum(Singleton.THE_INSTANCE)).isTrue();
    }

    @Test // GH-899
    public void upcastingWorksAsExpected() {
        Enum castedToEnum = YourFriendlyEnumMostPeopleUse.THING2;
        assertThat(ClassUtils.isEnum(castedToEnum)).isTrue();

        castedToEnum = Singleton.THE_INSTANCE;
        assertThat(ClassUtils.isEnum(castedToEnum)).isTrue();
    }
}
