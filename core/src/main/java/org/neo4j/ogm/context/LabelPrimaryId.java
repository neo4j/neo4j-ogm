/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

package org.neo4j.ogm.context;

import static java.util.Objects.*;

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
    LabelPrimaryId(ClassInfo classInfo, Object id) {
        this.label = classInfo.neo4jName();
        this.id = requireNonNull(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
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
