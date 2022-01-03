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
package org.neo4j.ogm.metadata;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

/**
 * @author Michael J. Simons
 */
public class FieldInfoTest {

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.convertible.numbers");
    private static final ClassInfo accountInfo = metaData.classInfo("Account");

    @Test // GH-845
    public void shouldBeAbleToDetermineConvertedTypeWithDirectImplementation() {

        assertThat(accountInfo.getFieldInfo("foobar").convertedType()).isEqualTo(String.class);
    }

    @Test // GH-845
    public void shouldBeAbleToDetermineConvertedTypeWithDirectInheritance() {

        assertThat(accountInfo.getFieldInfo("listOfFoobars").convertedType()).isEqualTo(String.class);
    }

    @Test // GH-845
    public void shouldBeAbleToDetermineConvertedTypeWithDirectInheritanceInnerClass() {

        assertThat(accountInfo.getFieldInfo("anotherListOfFoobars").convertedType()).isEqualTo(String.class);
    }

    @Test // GH-845
    public void shouldBeAbleToDetermineConvertedTypeWithIndirectInheritance() {

        assertThat(accountInfo.getFieldInfo("valueA").convertedType()).isEqualTo(String.class);
    }

    @Test // GH-845
    public void shouldBeAbleToDetermineConvertedTypeWithIndirectInheritanceInnerClass() {

        assertThat(accountInfo.getFieldInfo("valueB").convertedType()).isEqualTo(String.class);
    }

    @Test // GH-845
    public void shouldNotFailWithoutConverter() {

        assertThat(accountInfo.getFieldInfo("notConverter").convertedType()).isNull();
    }
}
