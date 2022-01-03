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
package org.neo4j.ogm.metadata.reflect;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.DomainInfo;
import org.neo4j.ogm.metadata.FieldInfo;

/**
 * @author Luanne Misquitta
 * @author Vince Bickers
 */
public class AnnotatedFieldAndNonAnnotatedSetterTest {

    private DomainInfo domainInfo = DomainInfo.create("org.neo4j.ogm.metadata.reflect");

    @Test
    public void shouldPreferAnnotatedFieldWithNonAnnotatedSetterForRelationshipEntity() {

        ClassInfo classInfo = this.domainInfo.getClass(End.class.getName());

        RelEntity relEntity = new RelEntity();
        Set<RelEntity> parameter = new HashSet();
        parameter.addAll(Arrays.asList(relEntity));

        FieldInfo objectAccess = EntityAccessManager
            .getRelationalWriter(classInfo, "REL_ENTITY_TYPE", Relationship.Direction.INCOMING, relEntity);

        assertThat(objectAccess).as("The resultant object accessor shouldn't be null").isNotNull();
        assertThat(objectAccess instanceof FieldInfo).as("The access mechanism should be via the field").isTrue();
        End end = new End();
        objectAccess.write(end, parameter);
        assertThat(parameter).isEqualTo(end.getRelEntities());
    }

    @RelationshipEntity(type = "REL_ENTITY_TYPE")
    public static class RelEntity {

        Long id;
        @StartNode
        Start start;
        @EndNode
        End end;

        public RelEntity() {
        }

        public End getEnd() {
            return end;
        }

        public void setEnd(End end) {
            this.end = end;
        }

        public Start getStart() {
            return start;
        }

        public void setStart(Start start) {
            this.start = start;
        }
    }

    public static class Start {

        Long id;
        String name;
        @Relationship(type = "REL_ENTITY_TYPE", direction = Relationship.Direction.OUTGOING)
        Set<RelEntity> relEntities;

        public Start() {
        }
    }

    public static class End {

        Long id;
        String name;
        @Relationship(type = "REL_ENTITY_TYPE", direction = Relationship.Direction.INCOMING)
        Set<RelEntity> relEntities;

        public End() {
        }

        public Set<RelEntity> getRelEntities() {
            return relEntities;
        }

        public void setRelEntities(Set<RelEntity> relEntities) {
            this.relEntities = relEntities;
        }
    }
}
