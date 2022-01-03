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
package org.neo4j.ogm.domain.locking;

import java.util.Date;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.annotation.Version;

/**
 * Relationship entity with @Version on field named other than `version`
 * @author Frantisek Hartman
 */
@RelationshipEntity(type = "ENEMY_OF")
public class EnemyOf {

    private Long id;

    @StartNode
    private User from;

    @EndNode
    private User to;

    @Version
    private Long customVersion;

    private Date since;

    public EnemyOf() {
    }

    public EnemyOf(User from, User to, Date since) {
        this.from = from;
        this.to = to;
        this.since = since;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public User getTo() {
        return to;
    }

    public void setTo(User to) {
        this.to = to;
    }

    public Long getCustomVersion() {
        return customVersion;
    }

    public void setCustomVersion(Long customVersion) {
        this.customVersion = customVersion;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    @Override
    public String toString() {
        return "EnemyOf{" +
            "id=" + id +
            ", from=" + from +
            ", to=" + to +
            ", customVersion=" + customVersion +
            '}';
    }
}
