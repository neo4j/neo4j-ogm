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
package org.neo4j.ogm.utils;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * @author Mark Angrish
 */
public class RelationshipUtilsTest {

    @Test
    void testFieldNameInferenceFromRelationshipType() {
        expect("writesPolicy", RelationshipUtils.inferFieldName("WRITES_POLICY"));
    }

    @Test
    void testGetterNameInference() {
        expect("getWritesPolicy", RelationshipUtils.inferGetterName("WRITES_POLICY"));
    }

    @Test
    void testSetterNameInference() {
        expect("setWritesPolicy", RelationshipUtils.inferSetterName("WRITES_POLICY"));
    }

    //
    @Test
    void testRelationshipTypeInferenceFromFieldName() {
        expect("WRITES_POLICY", RelationshipUtils.inferRelationshipType("writesPolicy"));
    }

    @Test
    void testRelationshipTypeInferenceFromGetterName() {
        expect("WRITES_POLICY", RelationshipUtils.inferRelationshipType("getWritesPolicy"));
    }

    @Test
    void testRelationshipTypeInferenceSetterName() {
        expect("WRITES_POLICY", RelationshipUtils.inferRelationshipType("setWritesPolicy"));
    }

    //
    @Test
    void testSimpleFieldNameInferenceFromRelationshipType() {
        expect("policy", RelationshipUtils.inferFieldName("POLICY"));
    }

    @Test
    void testSimpleGetterNameInference() {
        expect("getPolicy", RelationshipUtils.inferGetterName("POLICY"));
    }

    @Test
    void testSimpleSetterNameInference() {
        expect("setPolicy", RelationshipUtils.inferSetterName("POLICY"));
    }

    //
    @Test
    void testSimpleRelationshipTypeInferenceFromFieldName() {
        expect("POLICY", RelationshipUtils.inferRelationshipType("policy"));
    }

    @Test
    void testSimpleRelationshipTypeInferenceFromGetterName() {
        expect("POLICY", RelationshipUtils.inferRelationshipType("getPolicy"));
    }

    @Test
    void testSimpleRelationshipTypeInferenceSetterName() {
        expect("POLICY", RelationshipUtils.inferRelationshipType("setPolicy"));
    }

    private void expect(String expected, String actual) {
        assertThat(actual).isEqualTo(expected);
    }
}
