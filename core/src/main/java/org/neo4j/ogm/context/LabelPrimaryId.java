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

package org.neo4j.ogm.context;

import static java.util.Objects.*;

import org.neo4j.ogm.metadata.ClassInfo;

/**
 * Pair of label and primary id to use for lookups by primary key in MappingContext and CypherContext
 * The label is needed because primary id is unique for given label. There might be 1 primary id pointing to
 * two different entities having different label.
 * Label is either Node label or relationship type.
 *
 * @author Frantisek Hartman
 */
class LabelPrimaryId {

    private final String label;
    private final Object id;

    /**
     * Create LabelPrimaryId
     *
     * @param classInfo class info containign the primary id
     * @param id        the value of the id
     */
    public LabelPrimaryId(ClassInfo classInfo, Object id) {
        this.label = classInfo.primaryIndexField().containingClassInfo().neo4jName();
        this.id = requireNonNull(id);
    }

    public String getLabel() {
        return label;
    }

    public Object getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        LabelPrimaryId that = (LabelPrimaryId) o;

        if (!label.equals(that.label))
            return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        int result = label.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "LabelPrimaryId{" +
            "label='" + label + '\'' +
            ", id=" + id +
            '}';
    }
}
