/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.unit.metadata;

import org.junit.Test;
import org.neo4j.ogm.metadata.RelationshipUtils;

import static org.junit.Assert.assertEquals;

public class RelationshipUtilsTest {

    @Test
    public void testFieldNameInferenceFromRelationshipType() {
        expect("writesPolicy", RelationshipUtils.inferFieldName("WRITES_POLICY"));
    }

    @Test
    public void testGetterNameInference() {
        expect("getWritesPolicy", RelationshipUtils.inferGetterName("WRITES_POLICY"));
    }

    @Test
    public void testSetterNameInference() {
        expect("setWritesPolicy", RelationshipUtils.inferSetterName("WRITES_POLICY"));
    }

    //
    @Test
    public void testRelationshipTypeInferenceFromFieldName() {
        expect("WRITES_POLICY", RelationshipUtils.inferRelationshipType("writesPolicy"));
    }

    @Test
    public void testRelationshipTypeInferenceFromGetterName() {
        expect("WRITES_POLICY", RelationshipUtils.inferRelationshipType("getWritesPolicy"));
    }

    @Test
    public void testRelationshipTypeInferenceSetterName() {
        expect("WRITES_POLICY", RelationshipUtils.inferRelationshipType("setWritesPolicy"));
    }

    //
    @Test
    public void testSimpleFieldNameInferenceFromRelationshipType() {
        expect("policy", RelationshipUtils.inferFieldName("POLICY"));
    }

    @Test
    public void testSimpleGetterNameInference() {
        expect("getPolicy", RelationshipUtils.inferGetterName("POLICY"));
    }

    @Test
    public void testSimpleSetterNameInference() {
        expect("setPolicy", RelationshipUtils.inferSetterName("POLICY"));
    }

    //
    @Test
    public void testSimpleRelationshipTypeInferenceFromFieldName() {
        expect("POLICY", RelationshipUtils.inferRelationshipType("policy"));
    }

    @Test
    public void testSimpleRelationshipTypeInferenceFromGetterName() {
        expect("POLICY", RelationshipUtils.inferRelationshipType("getPolicy"));
    }

    @Test
    public void testSimpleRelationshipTypeInferenceSetterName() {
        expect("POLICY", RelationshipUtils.inferRelationshipType("setPolicy"));
    }

    private void expect(String expected, String actual) {
        assertEquals(expected, actual);
    }
}
