/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
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
public class AnnotatedFieldWithNoSetterAndNonSetterTest {

    private DomainInfo domainInfo = DomainInfo.create("org.neo4j.ogm.metadata.reflect");

    @Test
    public void shouldPreferAnnotatedFieldOverNonSetterInAbsenceOfSetterForRelationshipEntity() {
        ClassInfo classInfo = this.domainInfo.getClass(End.class.getName());

        RelEntity relEntity = new RelEntity();
        Set<RelEntity> parameter = new HashSet();
        parameter.addAll(Arrays.asList(relEntity));

        FieldInfo objectAccess = EntityAccessManager
            .getRelationalWriter(classInfo, "REL_ENTITY_TYPE", Relationship.INCOMING, relEntity);
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
        @Relationship(type = "REL_ENTITY_TYPE", direction = "OUTGOING")
        Set<RelEntity> relEntities;

        public Start() {
        }
    }

    static class End {

        Long id;
        String name;
        @Relationship(type = "REL_ENTITY_TYPE", direction = "INCOMING")
        Set<RelEntity> relEntities = new HashSet<>();

        End() {
        }

        Set<RelEntity> getRelEntities() {
            return relEntities;
        }

    }
}
