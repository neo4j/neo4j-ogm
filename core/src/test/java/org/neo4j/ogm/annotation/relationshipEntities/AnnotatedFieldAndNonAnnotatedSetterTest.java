/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

package org.neo4j.ogm.annotation.relationshipEntities;

import static org.junit.Assert.*;

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
import org.neo4j.ogm.metadata.reflect.EntityAccessManager;
import org.neo4j.ogm.metadata.reflect.FieldWriter;
import org.neo4j.ogm.metadata.reflect.RelationalWriter;
import org.neo4j.ogm.metadata.reflections.DomainInfoBuilder;

/**
 * @author Luanne Misquitta
 * @author Vince Bickers
 */
public class AnnotatedFieldAndNonAnnotatedSetterTest {

    private EntityAccessManager entityAccessStrategy = new EntityAccessManager();
    private DomainInfo domainInfo = DomainInfoBuilder.create("org.neo4j.ogm.annotation.relationshipEntities");


    @Test
    public void shouldPreferAnnotatedFieldWithNonAnnotatedSetterForRelationshipEntity() {

        ClassInfo classInfo = this.domainInfo.getClass(End.class.getName());

        RelEntity relEntity = new RelEntity();
        Set<RelEntity> parameter = new HashSet();
        parameter.addAll(Arrays.asList(relEntity));

        RelationalWriter objectAccess = this.entityAccessStrategy.getRelationalWriter(classInfo, "REL_ENTITY_TYPE", Relationship.INCOMING, relEntity);

        assertNotNull("The resultant object accessor shouldn't be null", objectAccess);
        assertTrue("The access mechanism should be via the field", objectAccess instanceof FieldWriter);
        End end = new End();
        objectAccess.write(end, parameter);
        assertEquals(end.getRelEntities(), parameter);
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

    public static class End {

        Long id;
        String name;
        @Relationship(type = "REL_ENTITY_TYPE", direction = "INCOMING")
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
