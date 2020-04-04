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

import java.lang.annotation.Annotation;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

public class AnnotationInfoTest {

    private AnnotationInfo testSubject;

    @Before
    public void setUp() throws NoSuchFieldException {
        Annotation residentsAnnotation = House.class.getField("residents")
            .getAnnotation(Relationship.class);

        this.testSubject = new AnnotationInfo(residentsAnnotation);
    }

    @Test
    public void getEnumWithDefaultValueReturnsExistingValueWhenPresent() {
        // given
        String givenKey = Relationship.DIRECTION;
        Relationship.Direction defaultValue = Relationship.Direction.OUTGOING;
        Relationship.Direction expectedValue = Relationship.Direction.INCOMING;

        // when
        Relationship.Direction actualValue = testSubject.getEnum(givenKey, defaultValue);

        // then
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    public void getEnumWithDefaultValueReturnsDefaultValueWhenNoValueAlreadyPresent() {
        // given
        String givenKey = "non-defined-key";
        Relationship.Direction defaultValue = Relationship.Direction.OUTGOING;

        // when
        Relationship.Direction actualValue = testSubject.getEnum(givenKey, defaultValue);

        // then
        assertThat(actualValue).isEqualTo(defaultValue);
    }

    @Test
    public void getEnumReturnsNullWhenNoValuePresent() {
        // given
        String givenKey = "non-defined-key";

        // when
        Relationship.Direction actualValue = testSubject.getEnum(Relationship.Direction.class, givenKey);

        // then
        assertThat(actualValue).isNull();
    }

    @Test
    public void getEnumReturnsValueWhenValuePresent() {
        // given
        String givenKey = Relationship.DIRECTION;
        Relationship.Direction expectedValue = Relationship.Direction.INCOMING;

        // when
        Relationship.Direction actualValue = testSubject.getEnum(Relationship.Direction.class, givenKey);

        // then
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @NodeEntity
    public static class House {

        @Relationship(type = "LIVES_IN", direction = Relationship.Direction.INCOMING)
        public List<Resident> residents;

        @NodeEntity
        public static class Resident {

            String name;

            House house;

        }

    }

}
