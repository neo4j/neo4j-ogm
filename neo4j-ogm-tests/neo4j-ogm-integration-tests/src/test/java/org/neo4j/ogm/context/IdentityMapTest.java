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
package org.neo4j.ogm.context;

import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.education.School;
import org.neo4j.ogm.domain.education.Teacher;
import org.neo4j.ogm.domain.education.TeachesAt;
import org.neo4j.ogm.metadata.MetaData;

/**
 * @author Vince Bickers
 * @author Michael J. Simons
 */
public class IdentityMapTest {

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.education");
    private static final MappingContext mappingContext = new MappingContext(metaData);

    @Before
    public void setUp() {
        mappingContext.clear();
    }

    @Test
    public void testUnchangedObjectDetected() {
        Teacher mrsJones = new Teacher();

        mrsJones.setId(115L); // the id field must not be part of the memoised property list
        mappingContext.addNodeEntity(mrsJones);
        assertThat(mappingContext.isDirty(mrsJones)).isFalse();
    }

    @Test
    public void testChangedPropertyDetected() {
        Teacher teacher = new Teacher("Miss White");

        teacher.setId(115L); // the id field must not be part of the memoised property list
        mappingContext.addNodeEntity(teacher);

        teacher.setName("Mrs Jones"); // the teacher's name property has changed.
        assertThat(mappingContext.isDirty(teacher)).isTrue();
    }

    @Test
    public void testRelatedObjectChangeDoesNotAffectNodeMemoisation() {
        Teacher teacher = new Teacher("Miss White");

        teacher.setId(115L); // the id field must not be part of the memoised property list
        mappingContext.addNodeEntity(teacher);

        teacher.setSchool(new School("Roedean")); // a related object does not affect the property list.

        assertThat(mappingContext.isDirty(teacher)).isFalse();
    }

    @Test // GH-684
    public void testNodeAndRelationshipWithSameId() {

        // Create a Node and set the Id.
        Teacher mrsJones = new Teacher();
        mrsJones.setId(1L);

        // Remember the entity.
        mappingContext.addNodeEntity(mrsJones);

        // Create a Relationship with the same Id.
        TeachesAt teachesAtRelationship = new TeachesAt();
        teachesAtRelationship.setId(1L);

        IdentityMap identityMap = new IdentityMap(metaData);
        identityMap.remember(mrsJones, mrsJones.getId());

        assertThat(identityMap.remembered(mrsJones, mrsJones.getId())).isTrue();
        assertThat(identityMap.remembered(teachesAtRelationship, teachesAtRelationship.getId())).isFalse();

        identityMap.remember(teachesAtRelationship, teachesAtRelationship.getId());

        assertThat(identityMap.remembered(teachesAtRelationship, teachesAtRelationship.getId())).isTrue();
    }
}
