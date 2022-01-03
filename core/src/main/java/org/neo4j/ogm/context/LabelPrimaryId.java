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

import java.util.Objects;

import org.neo4j.ogm.metadata.ClassInfo;

/**
 * Pair of label and primary id to use for lookups by primary key in MappingContext and CypherContext
 * The label is needed because primary id is unique for given label. There might be 1 primary id pointing to
 * two different entities having different label.
 * Label is either Node label or relationship type.
 *
 * @author Frantisek Hartman
 * @author Jonathan D'Orleans
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
class LabelPrimaryId {

    private final String label;
    private final Object id;

    /**
     * Create LabelPrimaryId
     *
     * @param classInfo class info containing the primary id
     * @param id        the value of the id
     */
    static LabelPrimaryId of(ClassInfo classInfo, Object id) {

        Objects.requireNonNull(classInfo);
        Objects.requireNonNull(id);
        return new LabelPrimaryId(classInfo, id);
    }

    private LabelPrimaryId(ClassInfo classInfo, Object id) {
        this.label = classInfo.neo4jName();
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LabelPrimaryId that = (LabelPrimaryId) o;
        return Objects.equals(label, that.label) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, id);
    }

    @Override
    public String toString() {
        return String.format("LabelPrimaryId{label='%s', id=%s}", label, id);
    }
}
