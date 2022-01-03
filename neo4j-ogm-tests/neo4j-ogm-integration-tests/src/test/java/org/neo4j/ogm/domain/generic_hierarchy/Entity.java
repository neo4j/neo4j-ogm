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
package org.neo4j.ogm.domain.generic_hierarchy;

import static java.util.UUID.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Labels;
import org.neo4j.ogm.annotation.PostLoad;

/**
 * @author Jonathan D'Orleans
 * @author Michael J. Simons
 */
public abstract class Entity {

    @Id
    protected String uuid;

    @Labels
    protected Set<String> labels = new HashSet<>();

    public Entity() {
        uuid = randomUUID().toString();
    }

    public Entity(String uuid) {
        this.uuid = uuid;
    }

    @PostLoad
    public void postLoad() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Entity entity = (Entity) o;
        return Objects.equals(uuid, entity.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }

    public void addLabel(Object... additionalLabels) {
        for (Object label : additionalLabels) {
            this.labels.add(label.toString());
        }
    }

    public void removeLabel(Object... labelsToRemove) {
        for (Object label : labelsToRemove) {
            this.labels.remove(label.toString());
        }
    }
}
